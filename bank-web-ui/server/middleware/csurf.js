/*
   This file is part of Enki.
  
   Copyright © 2016 - 2019 Oliver Wyman Ltd.
  
   Enki is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
  
   Enki is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
  
   You should have received a copy of the GNU General Public License
   along with Enki.  If not, see <https://www.gnu.org/licenses/>
*/

'use strict';

var csurf = require('csurf');
var _ = require('lodash');

function convertToLower(route){
  if (_.isString(route.path)){
    route.path = route.path.toLowerCase();
  }

  if (_.isString(route.method)){
    route.method = route.method.toLowerCase();
  }

  if (_.isArray(route.method)){
    route.method = _.map(route.method, _.method('toLowerCase'));
  }

  return route;
}

function matchesPath(path, req){
  var reqPath = _.toLower(req.originalUrl);
  if (_.isRegExp(path)){
    return path.test(reqPath);
  }

  return path === reqPath;
}

function matchesMethod(method, req){
  var reqMethod = _.toLower(req.method);
  if (_.isArray(method)){
    return _.includes(method, reqMethod);
  }

  return method === reqMethod;
}

// Some very specific urls might be called directly from external sites which have no means of getting the token. For example, a /login POST will be send from the SAML servers when using SAML authentication,
// and that request will not include the token!. But we cannot just ignore csurf for all
// requests to /login, as that would mean a GET /login wouldnt get the tokens generated and we wont be able to include them in a login html page when not using SAML!
function ignoreCsurfValidation(whitelistedRoutes, req){
  return _.some(whitelistedRoutes, function(route){
    return matchesPath(route.path, req) && matchesMethod(route.method, req);
  });
}

module.exports = function (config) {
  var csurfConfig = _.extend({whitelistedRoutes: []}, config.csurf);
  csurfConfig.whitelistedRoutes = _.map(csurfConfig.whitelistedRoutes, convertToLower);

  var csurfMiddleware = csurf(csurfConfig);

  // Return a middleware function that will either:
  //  - do nothing if request matches the whitelisted routes
  //  - apply the csurf validation
  return function(req, res, next){
    if(ignoreCsurfValidation(csurfConfig.whitelistedRoutes, req)) {
      return next();
    }
    csurfMiddleware(req, res, next);
  };
};
