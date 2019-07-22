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

/* eslint global-require: 0 */
'use strict';

import path from 'path';

module.exports = function(config){
  if (config.dev && config.enableWebpackRebuild && !config.enableWebpackDevServer) {
    // Avoid loading these when not called, as we wont have webpack and webpack-dev-middleware in production environments
    var webpack = require('webpack');
    var webpackConfig = require(path.resolve('./webpack.config.js'));
    var webpackDevMiddleware = require('webpack-dev-middleware');

    var compiler = webpack(webpackConfig);
    return webpackDevMiddleware(compiler, {
      stats: 'mimimal', // Set how much to log each time webpack builds
      publicPath: webpackConfig.output.publicPath,
      // lazy: false, // only with the lazy flag was this middleware able to detect file changes when running inside docker in win7
    });
  }
  return function(req, res, next) {next();};
};