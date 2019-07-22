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

const sinon = require('sinon');
const proxyquire = require('proxyquire');

describe('the bank controller', () => {
  let mockRequest;
  let mockResponse;
  let controller;

  beforeEach(function(){
    mockRequest = {
      params: {},
      body: {}
    };
    mockResponse = {
      status: sinon.stub().returnsThis(),
      send: sinon.spy()
    };

    // Create mocks here using sinon.stub and sinon.spy
    // Use proxyquire below to inject them in the component

    controller = proxyquire('../../../../../server/controllers/api/bank.js', {
      //mocks are injected here
    });
  });

  describe('#load', () => {
    it('should return a not-implemented message', (done) => {
      var expectedResult = {message: 'Please choose an action!'};

      controller.load(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(mockResponse.send, expectedResult);
        done();
      });
    });
  });

});
