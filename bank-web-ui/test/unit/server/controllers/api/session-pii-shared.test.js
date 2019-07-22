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

const sinon = require('sinon');
const proxyquire = require('proxyquire');

describe('the session-pii-shared controller', () => {
  let mockRequest;
  let mockResponse;
  let controller;

  beforeEach(function(){
    mockRequest = {
      params: {},
      body: {},
      session: {}
    };
    mockResponse = {
      status: sinon.stub().returnsThis(),
      send: sinon.spy()
    };

    // Create mocks here using sinon.stub and sinon.spy
    // Use proxyquire below to inject them in the component

    controller = proxyquire('../../../../../server/controllers/api/session-pii-shared.js', {
      //mocks are injected here
    });
  });

  describe('#getSessionPiiShared empty session', () => {
    it('should return a no data shared error', (done) => {
      controller.getSessionPiiShared(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(mockResponse.send, { error: 'no data shared' });
        done();
      });
    });

    it('should return a no data shared error empty id parameter', (done) => {
      mockRequest.session = { metaIds: []};
      controller.getSessionPiiShared(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(mockResponse.send, { error: 'no data shared' });
        done();
      });
    });

    it('should return a no data shared error empty metaIds', (done) => {
      mockRequest.params.id = 12;
      mockRequest.session = {};
      controller.getSessionPiiShared(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(mockResponse.send, { error: 'no data shared' });
        done();
      });
    });

    it('should return an piiData object', (done) => {
      mockRequest.params.id = 'f30a5568-df14-4603-8b5d-eb8ed5df3842';
      mockRequest.session = { metaIds: { 'f30a5568-df14-4603-8b5d-eb8ed5df3842' : { pii: 'data' }}};
      controller.getSessionPiiShared(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(mockResponse.send, { pii: 'data' });
        done();
      });
    });

  });

});
