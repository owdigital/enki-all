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

// Libraries
import context from './context';
import Vue from 'vue';
import VueRouter from 'vue-router';
import VueResource from 'vue-resource';
import VueGoodTable from 'vue-good-table';
import VueJSModal from 'vue-js-modal';
import BootstrapVue from 'bootstrap-vue';
import path from 'path';

// From: https://vuejs.org/v2/guide/components-registration.html#Automatic-Global-Registration-of-Base-Components
const requireComponent = require.context('components', true, /.vue$/);
requireComponent.keys().forEach(fileName => {
  // Get component config
  const componentConfig = requireComponent(fileName);
  const componentName = path.basename(fileName, '.vue');

  // Register component globally
  Vue.component(
    componentName,
    componentConfig.default || componentConfig
  );
});
// End From

// Bootstrap JS files import
import 'bootstrap';

// Styles
import '../style/main.scss';
import 'font-awesome/css/font-awesome.min.css';
import 'bootstrap/dist/css/bootstrap.css';
import 'bootstrap-vue/dist/bootstrap-vue.css';

// External exports
//  It should be possible to use module.exports and configure webpack to export this as a var in the global object
//  As in http://stackoverflow.com/questions/34357489/calling-webpacked-code-from-outside-html-script-tag
//  However when using webpack-hot-middleware, the exports are replaced with hot reload functions...
//  So directly adding to window instead, if we decide to ignore the hot-middleware we could just use module exports!
const app = window.app = window.app || {};

// The app.context can be used from server side to add data that should be available in the client side code, like csrf token
app.context = context;

const routeFilesReq = require.context("../routes/", true, /\.js$/);
let routes = [];
const routeFiles = routeFilesReq.keys();
routeFiles.forEach(jsFile => {
  let thisFileRoutes = routeFilesReq(jsFile);
  if (thisFileRoutes.default) thisFileRoutes = thisFileRoutes.default;

  routes = routes.concat(thisFileRoutes);
});

const router = new VueRouter({
  routes: routes,
  linkActiveClass: 'active'
});

Vue.use(VueRouter);
Vue.use(VueResource);
Vue.use(VueGoodTable);
Vue.use(BootstrapVue);
Vue.use(VueJSModal, { dialog: true });
Vue.http.interceptors.push((request, next) => {
  request.headers.set('x-csrf-token', app.context.csrfToken);
  next();
});

export default new Vue({
  router: router,
  el: '#vueholder'
});
