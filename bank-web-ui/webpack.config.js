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
var MiniCssExtractPlugin = require('mini-css-extract-plugin');
var TerserPlugin = require('terser-webpack-plugin');
var manifestPlugin = require('webpack-manifest-plugin');
var VueLoaderPlugin = require('vue-loader/lib/plugin');
var nodeEnv = process.env.NODE_ENV || 'dev';

// Choosing the bank theme here, to test themes you can run `BANK_THEME=bank-b npm run webpack`
var bankTheme = process.env.BANK_THEME || 'bank-default';

// If you want to force using compressed/uglified files in development:
// run webpack with -p flag:"yarn run webpack -- -p" Alternatively, you can just force "isDev = false" below
// optionally disable reload in config file adding "enableWebpackRebuild : true," (Otherwise you will see annoying errors in the console)
var isDev = nodeEnv === 'dev' && !_.includes(process.argv, '-p');

// Common plugins
var plugins = [
  new VueLoaderPlugin(),
  // Extract styles to its own file we can manually include in the html (instead of being dynamically added to the page)
  new MiniCssExtractPlugin({
    // Options similar to the same options in webpackOptions.output
    // both options are optional
    filename: isDev ? '[name].css' : '[name]-[hash].css',
    chunkFilename: isDev ? '[id].css' : '[id]-[hash].css',
  }),
  // Vue.JS prod deployment. See: https://vuejs.org/v2/guide/deployment.html
  new webpack.DefinePlugin({
    'process.env': {
      NODE_ENV: '"' + nodeEnv + '"',
    }
  }),
  // make jquery globally available for bootstrap and other libraries
  new webpack.ProvidePlugin({
    $: 'jquery',
    jQuery: 'jquery'
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

var optimization = {
  splitChunks: {
    cacheGroups: {
      vendor: {
        chunks: 'initial',
        name: 'vendor',
        test: 'vendor',
        enforce: true
      },
    }
  }
};

// "Production" only plugins for optimization/minification, inspired by: https://blog.madewithlove.be/post/webpack-your-bags/
if (!isDev){
  optimization.minimizer = [
    // This plugin minifies all the Javascript code of the final bundle
    // More info: https://webpack.js.org/configuration/optimization/#optimizationminimizer
    new TerserPlugin({
      terserOptions: {
        mangle:   true,
        compress: {
          warnings: false, // Suppress uglification warnings
        },
      },
      sourceMap: true,
    })
  ];
  plugins = plugins.concat([
    // This plugin prevents Webpack from creating chunks that would be too small to be worth loading separately
    new webpack.optimize.MinChunkSizePlugin({
      minChunkSize: 51200, // ~50kb
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
  // entry: ['babel-polyfill', './client/js/app.js'],
  // Development options.
  devtool: isDev ? 'source-map' : false, //for rendering sourcemaps, see https://webpack.github.io/docs/configuration.html#devtool
  mode: isDev ? 'development': 'production',

  // The files that sit at the root of the dependency tree and webpack starts processing to get all files to get included
  entry: {
    // this will end as the bundle.js file
    bundle: './client/js/app.js',
    vendor: ['vue', 'vue-router', 'vue-resource', 'lodash', 'bootstrap'],
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
      '@': path.resolve('./client/')
    },
    modules: [path.resolve('./client'), 'node_modules']
  },
  module: {
    rules: [
      {
        test: /\.vue$/,
        loader: 'vue-loader', // Will detect from .babelrc and process all <script> blocks with babel
      },
      {
        test: /\.js$/,
        include: [
          path.resolve(__dirname, 'client/'),
          path.resolve(__dirname, 'server/')
        ], // Include any other node_modules that require transpiling here
        loader: 'babel-loader', // Put all babel configuration in .babelrc
      },
      {
        test: /\.html$/,
        use: {
          loader: 'html-loader',
          options: {
            minimize: true,
            removeComments: true //check additional settings in the source code: https://github.com/webpack/html-loader/blob/master/index.js#L101
          }
        }
      }, {
        test: /\.less$/,
        use: [{
          loader: MiniCssExtractPlugin.loader
        },
        'css-loader',
        {
          loader: 'less-loader',
          options: {
            modifyVars:
            {
              'bank-color-theme': '\'' + bankTheme + '\''
            }
          }
        }]
      }, {
        test: /\.css$/,
        use: [
          'vue-style-loader',
          'css-loader'
        ]
      }, {
      // inline fonts smaller than 10kb
      // for bigger files the file-loader is used by default, which copies file to the build folder and uses url to load it
        test: /\.woff($|\?)|\.woff2($|\?)|\.ttf($|\?)|\.eot($|\?)|\.svg($|\?)/,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 10000,
              outputPath: 'fonts/',
              name: '[name]-[hash].[ext]'
            }
          }]
      }, {
      // inline images less than 10kb
      // for bigger files the file-loader is used by default, which copies file to the build folder and uses url to load it
        test: /\.(png|gif|jpg|jpeg)$/i,
        use: [
          {
            loader: 'url-loader',
            options: {
              limit: 10000,
              outputPath: 'img/',
              name: '[name]-[hash].[ext]'
            }
          }]
      }]
  },

  optimization: optimization,
  plugins: plugins,
  devServer: {
    contentBase: path.resolve(__dirname, './client'),
    headers: {
      'Access-Control-Allow-Origin': '*'
    },
    lazy: false, // Set to true if you do not want the page to reload on file changes
    port: isDev ? 9000 : process.env.PORT
  },

  output: {
    path: path.join(__dirname, 'client/builds'),
    publicPath: isDev ? '/builds/' : '',
    filename: isDev ? '[name].js' : '[name]-' + bankTheme + '-[hash].js'
  }
};
