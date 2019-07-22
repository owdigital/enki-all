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

var sinon = require('sinon');
var controller = require('../../../../server/controllers/index');

describe('The index controller', function () {
  var mockRequest = {};
  var mockResponse = {};

  describe('getIndexPage', function () {
    it('should render the index page', function () {
      mockResponse = { render: sinon.spy() };

      controller.getIndexPage(mockRequest, mockResponse);

      sinon.assert.calledWith(mockResponse.render, 'index');
    });
  });

});
