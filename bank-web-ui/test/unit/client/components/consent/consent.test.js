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
import Vuex from 'vuex';
import chai from 'chai';
import proxyquire from 'proxyquire';
import sinon from 'sinon';
const expect = chai.expect;
import routes from '../../../../../client/routes/consent';

describe('the consent component', function () {
  let sandbox;
  let consentComponent;
  let viewModel;
  let store;
  let router;
  let vm;

  before(() => {
    Vue.use(VueResource);
    Vue.use(VueRouter);
    Vue.use(Vuex);
  });

  beforeEach(function(){
    sandbox = sinon.createSandbox();
    consentComponent = proxyquire('../../../../../client/components/consent/consent.js', {
      // inject mocks here
    });

    sandbox.stub(Vue.http, 'get').callsFake(args => {
      if (args.startsWith('api/consent')) {
        return Promise.resolve({body: {csrfToken: 'surf01', challenge: {
          aud: 'consent-app',
          scopes: ['scope1', 'scope2'],
        }}});
      } else {
        return Promise.reject();
      }
    });

    store = new Vuex.Store({
      getters: {
        loggedIn: () => true,
        user: () => {return {username: 'test', password: 'testpassword'};}
      },
      returnPromise: Promise.resolve(),
      actions: { login () { return this.returnPromise; }}
    });

    router = new VueRouter({
      routes: routes,
      linkActiveClass: 'active'
    });

    viewModel = Vue.extend(_.extend({render: () => '', store: store}, consentComponent));

    vm = new viewModel({
      router,
      propsData: { }
    }); // shouldn't have to .$mount() because processing in created()
  });

  afterEach(function(){
    sandbox.restore();
  });

  describe('when created', function(){

    it('should display an error when passed an error parameter', done => {
      let err = {name: 'foo', message: 'bar'};
      vm.$router.push({
        path: 'consent',
        query:{error: err.name, error_description: err.message}
      });
      vm.$nextTick(() => {
        // Due to lifecycle issues, we need to call the methods manually.
        vm.processChallenge();
        expect(vm.error.name).to.eql(err.name);
        done();
      });
    });

    it('should display an error when missing challenge parameter', done => {
      expect(vm.error.name).to.eql('MissingParamChallenge');
      done();
    });

    it('should be initialized with given challenge data', done => {
      vm.$router.push({
        path: 'consent',
        query:{challenge: 'abc123'}
      });
      vm.$nextTick(() => {
        vm.processChallenge();
        setImmediate(() => {
          expect(vm.csrfToken).to.eql('surf01');
          done();
        });
      });
    });

  });
});
