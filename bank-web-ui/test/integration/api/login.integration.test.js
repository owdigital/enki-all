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

import request from 'supertest';
import chai from 'chai';
const expect = chai.expect;
import bootstrapServerPromise from '../server.bootstrap';
import populate from '../db.populate';
import logger from '../../../server/lib/logger';

import {LOGGED_IN, NOT_LOGGED_IN} from '../../../shared/constant';

describe('The /api/login API:', () => {
  var server;
  var authCookie;

  before((done) => {
    bootstrapServerPromise
      .then((initializedServer) => {
        server = initializedServer;
        return populate.populateDb();
      })
      .then(() => done())
      .catch(error => logger.error('Couldn\'t setup test suite: '+error));
  });

  after((done) => {
    populate.cleanupDb()
      .then(() => done())
      .catch(error => logger.error('Couldn\'t cleanupDb: '+error));
  });

  describe('checking login', () => {
    it('should respond with an Unauthorized message', () => {
      return request(server)
        .get('/api/login')
        .expect(200)
        .expect('Content-Type', /application\/json/)
        .then(function (res) {
          expect(res.body.state).to.eql(NOT_LOGGED_IN);
        });
    });
  });

  describe('logging in', () => {
    it('should respond with an Bad request, because no content', () => {
      return request(server)
        .post('/api/login')
        .expect(400);
    });

    it('should respond with an Unauthorized, because bad login', () => {
      return request(server)
        .post('/api/login')
        .set('Content-Type', 'application/json')
        .send('{"username": "foo", "password": "bar"}')
        .expect(200)
        .then(function (res) {
          expect(res.body.state).to.eql(NOT_LOGGED_IN);
        });
    });

    it('should respond with an 200 on successful login', () => {
      return request(server)
        .post('/api/login')
        .set('Content-Type', 'application/json')
        .send('{"username": "testuser", "password": "testPassWord"}')
        .expect(200)
        .then(function (res) {
          expect(res.body.state).to.eql(LOGGED_IN);
          authCookie = res.headers['set-cookie'];
        });
    });

    it('should respond with an OK message, because now logged in', () => {
      return request(server)
        .get('/api/login')
        .set('cookie', authCookie)
        .expect(200)
        .expect('Content-Type', /json/)
        .then(function (res) {
          expect(res.body.state).to.eql(LOGGED_IN);
          expect(res.body.user.username).to.eql('testuser');
        });
    });
  });
});
