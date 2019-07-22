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

import _ from 'lodash';
import Vue from 'vue';
import VueResource from 'vue-resource';
import VueRouter from 'vue-router';
import chai from 'chai';
import proxyquire from 'proxyquire';
import Vuex from 'vuex';
import sinon from 'sinon';
const expect = chai.expect;
import routes from '../../../../../client/routes/login';

import {LOGGED_IN, NOT_LOGGED_IN} from '../../../../../shared/constant';

describe('the login component', () => {
  let sandbox;
  let loginComponent;
  let viewModel;
  let mockStore;
  let router;

  before(() => {
    Vue.use(VueResource);
    Vue.use(VueRouter);
    Vue.use(Vuex);
  });

  beforeEach(() => {
    sandbox = sinon.createSandbox();
    loginComponent = proxyquire('../../../../../client/components/login/login.js', {
      // inject mocks here
    });

    sandbox.stub(Vue.http, 'get').withArgs('api/bank/getName').resolves({body: {name: 'test bank'}});

    mockStore = new Vuex.Store({
      returnPromise: Promise.resolve(),
      actions: { login () { return this.returnPromise; }}
    });

    router = new VueRouter({
      routes: routes,
      linkActiveClass: 'active'
    });

    viewModel = Vue.extend(_.extend({render: () => '', store: mockStore}, loginComponent));
  });

  afterEach(() => {
    sandbox.restore();
  });

  it('should be initialized with login failed', () => {
    let instance = new viewModel();

    expect(instance.loginFailed).to.eql(false);
  });

  describe('when mounted on a page', () => {
    let instance;

    beforeEach(() => {
      instance = new viewModel({
        router,
        propsData: { }
      }).$mount();
    });

    describe('login', () => {
      it('should login correctly', (done) => {
        const mockItem = {username: 'test', password: 'testpassword'};
        const mockResponse = {body: {state: LOGGED_IN, username: 'test'}, status: 200};
        sandbox.stub(Vue.http, 'post').withArgs('api/login', mockItem).resolves(mockResponse);

        instance.username = mockItem.username;
        instance.password = mockItem.password;
        instance.loginFailed = null;

        instance.login();
        setImmediate(() => {
          expect(instance.loginFailed).to.eql(false);
          done();
        });
      });

      it('should fail to login', (done) => {
        const mockItem = {username: 'test', password: 'testpassword'};
        const mockResponse = {status: 200, body: {state: NOT_LOGGED_IN}};
        sandbox.stub(Vue.http, 'post').withArgs('api/login', mockItem).resolves(mockResponse);
        mockStore.returnPromise = Promise.reject();

        instance.username = mockItem.username;
        instance.password = mockItem.password;
        instance.loginFailed = null;

        instance.login();

        setImmediate(() => {
          expect(instance.loginFailed).to.eql(true);
          done();
        });
      });
    });
  });

});
