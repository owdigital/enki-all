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

import path from 'path';

var _ = require('lodash');

// Automatically add core page info to response.locals, so that it can be accessed by the rendering engine
function templateDataWrapper(config) {

  var additionalDataCallbacks = [];
  var contextProperties = [];

  function templateData(req, res, next) {

    // Override the render call so that we can append to the page model defined on the route, rather than pre-setting
    // locals which might be subsequently overridden
    var _render = res.render;
    res.render = function (view, data, callback) {
      if (typeof data === 'function') {
        callback = data;
        data = {};
      }

      // Merge values from every additional data callback
      var mergedData = _.reduce(additionalDataCallbacks, function (soFar, cb) {
        return _.merge(soFar, cb(req, res, soFar));
      }, {});

      // Ensure that the required data for render goes on last
      mergedData = _.merge(mergedData, data);

      // Add to the context properties array the value of every registered context property callback
      var evaluatedContextProperties = contextProperties.map(function (prop) {
        return { name: prop.name, value: prop.value(req, res, mergedData) };
      });
      mergedData.contextProperties = (mergedData.contextProperties || []).concat(evaluatedContextProperties);

      _render.call(res, view, mergedData, callback);
    };

    next();
  }

  templateData.defineAdditionalData = function (callback) {
    additionalDataCallbacks.push(callback);
  };

  templateData.addContextProperty = function (name, valueOrCallback) {
    var evaluationFn = _.isFunction(valueOrCallback) ? valueOrCallback: _.constant(valueOrCallback);
    contextProperties.push({ name: name, value: evaluationFn });
  };

  // Set the webpack base url for dev server
  var webpackBaseUrl;
  if(config.enableWebpackDevServer){
    // eslint-disable-next-line global-require
    var webpackConfig = require(path.resolve('./webpack.config.js'));
    webpackBaseUrl = 'http://localhost:'+webpackConfig.devServer.port;
  }

  // Wire bundles from manifest.json
  var webpackBundles =
    (config.enableWebpackRebuild || config.enableWebpackDevServer) && config.webpackDevBundles ?
      config.webpackDevBundles :
      config.bundles;

  templateData.defineAdditionalData(function (req) {
    return {
      csrfToken: req.csrfToken ? req.csrfToken() : null,
      bundles: webpackBundles,
      webpackBaseUrl: webpackBaseUrl,
    };
  });

  return templateData;
}

module.exports = templateDataWrapper;
