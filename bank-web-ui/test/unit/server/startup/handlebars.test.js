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

var chai = require('chai');
var sinon = require('sinon');
var expect = chai.expect;
import proxyquire from 'proxyquire';

var handlebarsInitScript = require('../../../../server/setup/handlebars');

describe('the handlebars startup script', function(){
  var mockApp;
  var mockConfig;
  var mockTemplatedata;
  var middlewaresInitScript;
  var addContextProperty;

  beforeEach(function(){
    addContextProperty = sinon.spy();
    mockTemplatedata = () => {
      return {
        addContextProperty: addContextProperty
      };
    };

    middlewaresInitScript = proxyquire('../../../../server/setup/middlewares', {
      '../middleware/templateData': mockTemplatedata
    });

    mockApp = {
      set: () => {},
      use: () => {},
      engine: () => {},
    };

    mockConfig = {
      static: {
        baseDir: ''
      },
      sessionSecret: 1
    };

    handlebarsInitScript(mockApp, mockConfig);
    middlewaresInitScript(mockApp, mockConfig);
  });

  describe('sets additional data for the view template', function () {
    var mockRequest;

    beforeEach(function(){
      mockRequest = {
        csrfToken: sinon.stub()
      };
    });

    it('should add a csrfToken property including the CSRF token', function () {
      mockRequest.csrfToken.returns('fooToken');
      var propertyName = addContextProperty.firstCall.args[0];
      var propertyCallback = addContextProperty.firstCall.args[1];

      var value = propertyCallback(mockRequest);

      expect(propertyName).to.be.equal('csrfToken');
      expect(value).to.be.equal('fooToken');
    });
  });

});
