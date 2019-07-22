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
chai.use(require('chai-shallow-deep-equal'));
var expect = chai.expect;
var bootstrapServerPromise = require('../server.bootstrap');

describe('The /api/bank API:', function () {
  var server;

  before(function(done){
    // eslint-disable-next-line no-invalid-this
    this.timeout(10000);
    bootstrapServerPromise.then((initializedServer) => {
      server = initializedServer;
    })
      .then(() => done());
  });

  describe('GET /', function () {
    it('should respond with a Not Implemented message', () => {
      return request(server)
        .get('/api/bank')
        .expect(200)
        .expect('Content-Type', /json/)
        .then(function (res) {
          expect(res.body).to.eql({message: 'Please choose an action!'});
        });
    });
  });
});
