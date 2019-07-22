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
import bootstrapServerPromise from '../server.bootstrap';
import chai from 'chai';
const expect = chai.expect;
chai.use(require('chai-shallow-deep-equal'));


describe('The /api/session-pii-shared API:', () => {
  var server;

  before((done) => {
    bootstrapServerPromise.then((initializedServer) => {
      server = initializedServer;
    })
      .then(() => done());
  });

  describe('GET /', () => {
    it('should respond with a No Data Shared message for empty id', () => {
      return request(server)
        .get('/api/session-pii-shared')
        .expect(404)
        .expect('Content-Type', /json/)
        .then((res) => {
          expect(res.body).to.eql({ error: 'no data shared' });
        });
    });
  });

  it('should respond with a No Data Shared message for unknown id', () => {
    return request(server)
      .get('/api/session-pii-shared/99999')
      .expect(404)
      .expect('Content-Type', /json/)
      .then((res) => {
        expect(res.body).to.eql({ error: 'no data shared' });
      });
  });

});
