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


describe('the user-data controller', () => {
  let mockRequest;
  let mockResponse;
  let controller;
  let mockRepo;

  beforeEach(function(){
    mockRequest = {
      params: {},
      body: {}
    };

    mockResponse = {
      status: sinon.stub().returnsThis(),
      send: sinon.spy(),
      set: sinon.spy()
    };

    mockRepo = {
      getUsers: sinon.stub(),
      getUser: sinon.stub(),
      insertUser: sinon.stub(),
      updateUser: sinon.stub(),
      deleteUser: sinon.stub()
    };

    controller = proxyquire('../../../../../server/controllers/api/user-data.js', {
      '../../repository/user-data' : mockRepo
    });
  });


  describe('getUsers', function () {
    it('should return a json with the all Users', function (done) {
      let mockItems = [{an: 'item'}];
      mockRepo.getUsers.returns(Promise.resolve(mockItems));

      controller.getUsers(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(mockResponse.set, {'Content-type': 'application/json'});
        sinon.assert.calledWith(mockResponse.send, mockItems);
        done();
      });
    });
  });



});
