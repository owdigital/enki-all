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

// NOTE: Only non-sensitive information goes in this file.
// Do not put any sensitive information in this file, use dev.json/latestCommit.json/staging.json/production.json/etc.

'use strict';

var _ = require('lodash');
var fs = require('fs');
var path = require('path');
var url = require('url');
var uuid = require('uuid');
var webpackBundle = fs.existsSync('./client/builds/build-manifest.json') ?
  require('./client/builds/build-manifest.json') : // eslint-disable-line global-require
  {};
var devEnvironments = ['dev', 'test', 'dockerdev'];
var isDev = _.includes(devEnvironments, process.env.NODE_ENV);

var config = {
  dev: isDev,
  secure: false,
  enableWebpackRebuild: false,
  errorDebug: isDev,
  cookieSecret: isDev ? 'slipperysomethingwhatevercience' : uuid.v4(),
  disableCrawlers: true,

  // Webpack includes hash in the produced names, so every time webpack is run we need to find out which file names where produced
  bundles: _.merge({
    'bundle.js': 'bundle.js',
    'vendor.js': 'vendor.js',
    'bundle.css': 'bundle.css',
  }, webpackBundle),
  // As webpack bundles contain hashed names, let browsers cache the files
  static: {
    baseDir: 'client',
    componentsDir: 'client/components',
    bindingsDir: 'client/bindings',
    cacheSettings: {
      etag: false,
      maxAge: '10h'
    }
  },
  handlebars: {
    defaultLayout: 'main',
    extname: '.hbs',
    viewsDir: 'server/views',
    layoutsDir: 'server/views/layouts',
    partialsDir: 'server/views/partials'
  },
  csurf: {
    whitelistedRoutes: [
      {path: '/login', method: 'POST'},
      {path: '/logoutCallback', method: 'POST'}, // Add other routes csuf can be ignored for
    ]
  },
  enableWebpackDevServer: false, // This gets set in the start:server in package.json, avoid changing here
  port: 80, // Adding port here allows it to be overriden with environment variables, which is how IISNode passes the port pipe
  bankName: 'Bank Default',
  bankTheme: 'bank-default',
  enkiCallbackUrl: 'http://localhost:3000/oidc-callback?uid=2&bank-id=2',
  enkiUrl: 'http://localhost:3000/',
  enkiBackchannelUrl: 'http://localhost:3000/',
  enkiOauthClientId: 'bank-a-client',
  enkiOauthClientSecret: 'bank-a-secret',
  enkiOauthCallback: 'http://localhost:9001/signupcallback',
  enkiHydraUrl: 'http://localhost:5444/',
  enkiExternalHydraUrl: process.env.ENKI_HYDRA_URL || 'http://localhost:5444/',
  enkiInfoUrl: '/#/about-enki',
  db: {
    type: 'sqlite',
    file: path.join(__dirname, '/server/db/main.sqlite'),
  },
  agentDataURL: url.resolve(process.env.AGENT_URL || 'http://localhost:3010', '/data'),
  sessionSecret: 'jmjqE7sPqA0yfy5SRBp3VzyUwr4JTz87',
};

var environments = {
  // App-specific, non-sensitive, environment-specific settings here.
  // This is keyed off "NODE_ENV", which will be set in web.config by deploy.ps1.

  dev: {
    enableWebpackRebuild: true, // disable if you want to use production webpack builds
    webpackDevBundles: {
      'bundle.js': 'bundle.js',
      'vendor.js': 'vendor.js',
      'bundle.css': 'bundle.css',
    },
    //ignore browser cache on dev
    static:{
      cacheSettings: {
        etag: false,
        maxAge: '0'
      }
    }
  },

  production: {
  },

  test: {
    port: 9002,
    csurf: {
      ignoreMethods: ['GET', 'PUT', 'POST', 'DELETE', 'PATCH']
    },
    //ignore browser cache on test
    static:{
      cacheSettings: {
        etag: false,
        maxAge: '0'
      }
    },
    bankName: 'Test Bank',
    db: {
      file: path.join(__dirname, '/server/db/test/main.sqlite'),
    }
  }
};

var configEnv = process.env.NODE_ENV || 'dev';

var overrides = environments[configEnv] || {};

// This is almost analogous to lodash's merge function, but we want to ensure that *all* properties are merged over,
// including ones whose values are undefined in the new source object.
function mergeComplete(destination, source) {
  _.forOwn(source, function (v, k) {
    if (_.isPlainObject(v)) {
      destination[k] = mergeComplete(destination[k] || {}, v);
    } else {
      destination[k] = v;
    }
  });

  return destination;
}

function replaceEnvironmentProperties(obj, prefix) {
  var keys = _.keys(obj);

  function getKey(key) {
    var snakeCasedKey = _.snakeCase(key).toUpperCase();
    return prefix ?
      [prefix, snakeCasedKey].join('__') :
      snakeCasedKey;
  }

  function evaluateEnvironmentVariable(key) {
    var stringValue = process.env[getKey(key)];
    try {
      return eval(stringValue);
    } catch (e) {
      return stringValue;
    }
  }

  _.forEach(keys, function (key) {
    if (!_.isPlainObject(obj[key])) {
      obj[key] = process.env.hasOwnProperty(getKey(key)) ? evaluateEnvironmentVariable(key) : obj[key];
    } else {
      replaceEnvironmentProperties(obj[key], getKey(key));
    }
  });
}

mergeComplete(config, overrides);
replaceEnvironmentProperties(config);

module.exports = config;
