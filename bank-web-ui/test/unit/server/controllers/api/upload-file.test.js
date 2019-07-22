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
import fs from 'fs';
import MemoryStream from 'memorystream';
import controller from '../../../../../server/controllers/api/upload-file';

describe('the upload-file controller', () => {
  let mockRequest;
  let mockResponse;

  beforeEach(() => {
    mockRequest = {
      headers: {},
      params: {},
      body: {},
      pipe: sinon.spy()
    };
    mockResponse = {
      status: sinon.stub().returnsThis(),
      send: sinon.spy(),
      writeHead: sinon.spy(),
      end: sinon.spy()
    };
  });

  describe('upload', () => {
    it('should return a req.pipe', (done) => {
      let service = controller.service;
      let pipeStub = sinon.stub();
      let streamStub = sinon.stub(fs, 'createWriteStream').callsFake(() => {
        return new MemoryStream();
      });
      let fakeBusboy = {
        on: (event, handler) => {
          if (event === 'file'){
            let file = {
              pipe: pipeStub
            };
            handler('fieldName', file, 'fileName');
          } else if (event === 'finish') {
            handler();
          }
        }
      };

      let createStub = sinon.stub(service, 'createBusboy').callsFake(() => {
        return fakeBusboy;
      });

      controller.upload(mockRequest, mockResponse);

      setImmediate(function(){
        sinon.assert.calledWith(createStub, mockRequest.headers);
        sinon.assert.calledOnce(pipeStub);
        sinon.assert.calledOnce(streamStub);
        sinon.assert.calledWith(mockResponse.writeHead, 200);
        sinon.assert.calledOnce(mockResponse.end);
        sinon.assert.calledWith(mockRequest.pipe, fakeBusboy);
        done();
      });
    });
  });
});
