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

import chai from 'chai';
import proxyquire from 'proxyquire';
import sinon from 'sinon';
const expect = chai.expect;
import Vue from 'vue';
import VueResource from 'vue-resource';
Vue.use(VueResource);

describe('the profile-view component', () => {
  let sandbox;
  let profileView;
  let req = null;
  const mockData = {
    shareAssertions: [],
    metadataAssertions: []
  };

  beforeEach(() => {
    sandbox = sinon.sandbox.create();
    profileView = proxyquire('../../../../src/client/components/profile-view/profile-view.js', {
      // inject mocks here
    });
  });

  afterEach(() => {
    sandbox.restore();
  });

  describe('formatDate', () => {
    it('should return formatted date for ISO date', () => {
      const res = profileView.filters.formatDate('2017-01-01T13:52:43Z');
      expect(res).to.equal('01 Jan 2017');
    });

    it('should return undefined for invalid date', () => {
      const res = profileView.filters.formatDate('');
      expect(res).to.equal(undefined);
    });
  });

  describe('isValidResponseData', () => {
    it('should identify valid response data', () => {
      const validData = {
        shareAssertions: [],
        metadataAssertions: []
      };

      const res = profileView.methods.isValidResponseData(validData);
      expect(res).to.be.true;
    });

    it('should identify invalid response data', () => {
      const invalidData = {
        someProperty: []
      };

      const res = profileView.methods.isValidResponseData(invalidData);
      expect(res).to.be.false;
    });
  });

  describe('beforeMount', () => {
    it('should have the correct initial data', () => {
      // correct initial status
      expect(profileView.data().shareData).to.be.empty;
      expect(profileView.data().metaData).to.be.empty;
    });

    it('should have correct params in request', (done) => {
      Vue.http.interceptors.unshift((request, next) => {
        req = request;
        next(request.respondWith(mockData, { status: 200 }));
      });

      profileView.methods.fetchData();

      setImmediate(() => {
        // correct params in request
        expect(req.params).is.eql({ "credentials": "true" });
        done();
      });
    });

    it('should assign assertions after successful response', (done) => {
      Vue.http.interceptors.unshift((request, next) => {
        req = request;
        next(request.respondWith(mockData, { status: 200 }));
      });

      profileView.methods.fetchData();

      setImmediate(() => {
        // correct response
        expect(profileView.data().metaData).eql(mockData.shareAssertions);
        expect(profileView.data().shareData).eql(mockData.metadataAssertions);
        done();
      });

      it('should leave assertions null after failing response', (laterDone) => {
        Vue.http.interceptors.unshift((request, next) => {
          req = request;
          next(request.respondWith({ status: 404 }));
        });

        profileView.methods.fetchData();

        setImmediate(() => {
          // correct response
          expect(profileView.data().metaData).eql(null);
          expect(profileView.data().shareData).eql(null);
          laterDone();
        });
      });
    });
  });
});