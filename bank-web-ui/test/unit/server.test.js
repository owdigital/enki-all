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

// eslint-disable-next-line no-unused-vars
var Promise = require('bluebird');
var chai = require('chai');
var proxyquire = require('proxyquire');
var sinon = require('sinon');
var expect = chai.expect;

describe('the application startup script', function () {
  var mockApp;
  var mockRouter;
  var mockExpress;
  var mockHost = 'mockHost';

  var mockConfig;
  var mockLogger;

  var server;

  var startApp = function(done){
    server = proxyquire('../../server', {
      './config': mockConfig,
      express: mockExpress,
      './server/setup/server': () => {},
      './server/lib/logger': mockLogger
    });

    setImmediate(done);
  };

  beforeEach(function (done) {
    // Mock Express dependencies
    mockApp = {
      set: sinon.spy(),
      engine: sinon.spy(),
      use: sinon.spy(),
      listen: sinon.spy(function () {
        return {
          address: function () {
            return { port: mockConfig.port, address: mockHost };
          }
        };
      })
    };
    mockRouter = {};
    mockExpress = sinon.stub().returns(mockApp);
    mockExpress.Router = sinon.stub().returns(mockRouter);


    // Mock dependencies
    mockLogger = {
      info: sinon.spy(),
    };
    mockConfig = {
      port: 'mockHttpPort',
      bankName: 'unit_test_db',
      enkiCallbackUrl: 'bla',
      handlebars: {},
    };
    startApp(done);
  });

  it('should return the application', function () {
    expect(server).to.equal(mockApp);
  });

  describe('basic application startup', function () {

    it('should set the application listening on the configured port', function () {
      sinon.assert.calledOnce(mockApp.listen);
      sinon.assert.calledWith(mockApp.listen, mockConfig.port);
    });

    it('should log on successful application start', function () {
      var listenCallback = mockApp.listen.firstCall.args[1];
      expect(listenCallback).to.be.a('function');

      listenCallback();

      sinon.assert.calledWith(mockLogger.info, `Express server listening at http://${mockHost}:${mockConfig.port}`);
    });
  });

});
