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

import sinon from 'sinon';
import proxyquire from 'proxyquire';

describe('the oidc controller', () => {
  let mockRequest;
  let mockResponse;
  let controller;
  let mockRepo;
  let mockConfig = {
    enkiCallbackUrl: 'bla'
  };

  beforeEach( () => {
    mockRequest = {
      params: {},
      body: {}
    };
    mockResponse = {
      status: sinon.stub().returnsThis(),
      redirect: sinon.spy()
    };

    mockRepo = {
      getUser: sinon.stub()
    };

    // Create mocks here using sinon.stub and sinon.spy
    // Use proxyquire below to inject them in the component

    controller = proxyquire('../../../../../server/controllers/api/oidc.js', {
      '../../repository/user-data' : mockRepo,
      '../../../config': mockConfig
    });
  });

  describe('#oidcMock', () => {
    it('should return a simple redirect to enki', function (done) {
      controller.oidcMock(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWithMatch(
          mockResponse.redirect,
          mockConfig.enkiCallbackUrl
        );
        done();
      });
    });
  });

});
