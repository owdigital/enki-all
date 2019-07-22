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

var path = require('path');
var _ = require('lodash');
var webpack = require('webpack');
const extractPlugin = require('extract-text-webpack-plugin');
const manifestPlugin = require('webpack-manifest-plugin');
const nodeEnv = process.env.NODE_ENV || 'dev';
var copyPlugin = require('copy-webpack-plugin');
const TerserPlugin = require('terser-webpack-plugin');
const VueLoaderPlugin = require('vue-loader/lib/plugin');

// If you want to force using compressed/uglified files in development:
// run webpack with -p flag:"npm run webpack -- -p" Alternatively, you can just force "isDev = false" below
// optionally disable reload in config file adding "enableWebpackRebuild : true," (Otherwise you will see annoying errors in the console)
var isDev = nodeEnv === 'dev' && !_.includes(process.argv, '-p');

// Common plugins
var plugins = [
  new VueLoaderPlugin(),
  // Extract styles to its own file we can manually include in the html (instead of being dynamically added to the page)
  new extractPlugin(isDev ? 'bundle.css' : 'bundle-[hash].css'),
  // make jquery globally available for bootstrap and other libraries
  new webpack.ProvidePlugin({
    $: "jquery",
    jQuery: "jquery"
  }),
  // Make sure momentjs doesn't load all locales and adds 900kb by itself: https://gist.github.com/xjamundx/b1c800e9282e16a6a18e#momentjs-and-the-ignore-plugin
  new webpack.IgnorePlugin(/^\.\/locale$/, /moment$/), // saves ~100k from build

  // Copy static assets, used for copying images to build/img folder that are being used in hbs files
  // Adding file by file to avoid overhead
  new copyPlugin([
    { from: 'resources/assets/einfo-logo.png', to: 'img/einfo-logo.png' },
    { from: 'resources/assets/sunset-945435_1920.jpg', to: 'img/sunset-945435_1920.jpg'},
    { from: 'resources/templates/login.css', to: 'login.css'},
  ],{
    copyUnmodified: true,
  })
];

// Development Dev plugins
if (isDev){
  plugins = plugins.concat([
    new webpack.NoEmitOnErrorsPlugin(),
    new webpack.LoaderOptionsPlugin({
      debug: true
    })
  ]);
}

// "Production" only plugins for optimization/minification, inspired by: https://blog.madewithlove.be/post/webpack-your-bags/
if (!isDev){
  plugins = plugins.concat([
    // This plugin prevents Webpack from creating chunks that would be too small to be worth loading separately
    new webpack.optimize.MinChunkSizePlugin({
      minChunkSize: 51200, // ~50kb
    }),
    // This plugin minifies all the Javascript code of the final bundle
    new TerserPlugin({
      parallel: true,
      terserOptions: {
        ecma: 6,
      },
    }),
    // When building in production, css/js file names will be hashed
    // This plugin creates a json file mapping "bundle.js" and "bundle.css" with their generated names
    // We can then load it on app start, and make it available so in the main.hbs template we can include the right style/script tags
    // More info: http://madole.xyz/asset-hashing-with-webpack/
    new manifestPlugin({
      fileName: 'build-manifest.json'
    })
  ]);
}

// The webpack configuration
module.exports = {
  mode: isDev ? "development": "production",
  // Development options.
  devtool: isDev ? 'source-map' : false, //for rendering sourcemaps, see https://webpack.js.org/configuration/devtool/

  // The files that sit at the root of the dependency tree and webpack starts processing to get all files to get included
  entry: {
    // this will end as the bundle.js file
    bundle: './src/client/js/app.js',
    vendor: ['jquery', 'vue', 'vue-router', 'vue-resource', 'lodash', 'bootstrap'],
    // For hot reload, the additional endpoint below would be required:
    //.concat(isDev ? ['webpack-hot-middleware/client'] : [] )
  },

  resolve: {
    extensions: ['*', '.js', '.vue'],
    // Create aliases to import modules more easily.
    //  - Allows require('vue') instead of require('vue/dist/vue.common.js')
    //  - Allows require('@/js/context') from anywhere instead of relative paths like require('../../../js/context')
    alias: {
      'vue$': 'vue/dist/vue.common.js',
      '@': path.resolve('./src/client/')
    },
    modules: [path.resolve('./src/client'), "node_modules"]
  },
  optimization: {
    minimize: !isDev,
    nodeEnv: nodeEnv,
    splitChunks: {
      name:      'vendor', // Move dependencies to our vendor file
      minChunks: Infinity, // This ensures that no other module goes into the vendor chunk
    }
  },
  module: {
      rules: [
        {
          test: /\.js$/,
          // Include any other node_modules that require transpiling here
          include: [
            path.resolve(__dirname, 'client/')
          ],
          use: 'babel-loader', // Put all babel configuration in .babelrc
        },
        {
          test: /\.vue$/,
          loader: 'vue-loader', // Will detect from .babelrc and process all <script> blocks with babel
          options: {
            loaders: {
              // vue-loader will try to use a "scss-loader" when finding <style lang="scss"></style> blocks
              // in order to use sass-loader with scss in vue files, we need to tell it exactly what to do
              scss: [
                'vue-style-loader',
                'css-loader',
                'sass-loader'
              ]
            }
          }
        },
        {
          test: /\.json$/,
          use: 'json-loader',
          include: [
              path.resolve(__dirname, 'assets/')
            ],
        },
        {
          test: /\.scss$/,
          use: extractPlugin.extract({
            fallback: 'style-loader',
            use: [{
              loader: 'css-loader', // translates CSS into CommonJS modules
            }, {
              loader: 'postcss-loader', // Run post css actions
              options: {
                plugins() { // post css plugins, can be exported to postcss.config.js
                  return [
                    require('precss'),
                    require('autoprefixer')
                  ];
                }
              }
            }, {
              loader: 'sass-loader' // compiles Sass to CSS
            }]
          })
        },
        {
          test: /\.css$/,
          use: extractPlugin.extract({
            'fallback': 'style-loader',
            'use': 'css-loader'
          })
        },
        {
          // inline fonts smaller than 10kb
          // for bigger files the file-loader is used by default, which copies file to the build folder and uses url to load it
          test: /\.woff($|\?)|\.woff2($|\?)|\.ttf($|\?)|\.eot($|\?)|\.svg($|\?)/,
          use: [{
            loader: 'url-loader',
            options: {
              limit: 10000,
              outputPath: 'fonts/',
              name: '[name]-[hash].[ext]',
            }
          }]
        },
        {
          // inline images less than 10kb
          // for bigger files the file-loader is used by default, which copies file to the build folder and uses url to load it
          test: /\.(png|gif|jpg|jpeg)$/i,
          use: [{
            loader: 'url-loader',
            options: {
              limit: 10000,
              outputPath: 'img/',
              name: '[name]-[hash].[ext]',
            }
          }]
        }]
    },
  plugins: plugins,

  output: {
    path:  path.join(__dirname, '/resources/public/build'),
    publicPath: '',
    filename: isDev ? '[name].js' : '[name]-[hash].js'
  }
};
