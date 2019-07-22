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

import Vue from 'vue';
import VueResource from 'vue-resource';
import chai from 'chai';
import sinon from 'sinon';
const expect = chai.expect;

describe('the bank component', () => {

  var sandbox;
  var mockItem;

  before(() => {
    Vue.use(VueResource);
  });

  beforeEach(() => {
    sandbox = sinon.createSandbox();
    mockItem = {name: 'Bank XYZ'};

    var mockResponse = {body: mockItem};
    sandbox.stub(Vue.http, 'get').withArgs('/api/bank/getName').returns(mockResponse);
  });

  afterEach(() => {
    sandbox.restore();
  });

  it('should load the bank name from the server', done => {
    setImmediate(() => {
      expect(mockItem.name).to.equal('Bank XYZ');
      done();
    });
  });
});
