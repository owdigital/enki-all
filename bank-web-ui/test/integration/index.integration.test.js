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

var request = require('supertest');
var chai = require('chai');
var expect = chai.expect;
var bootstrapServerPromise = require('./server.bootstrap');

describe('The index route', function () {
  var server;

  before(function(done){
    bootstrapServerPromise.then((initializedServer) => {
      server = initializedServer;
    })
      .then(() => done());
  });

  it('Renders the index view', function (done) {
    request(server).get('/')
      .expect('Content-Type', /html/)
      .expect(function(res) {
        expect(res.text).to.include('<router-view></router-view>');
      })
      .expect(200, done);
  });
});
