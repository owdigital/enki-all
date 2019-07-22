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
chai.use(require('chai-shallow-deep-equal'));
const expect = chai.expect;

import bootstrapServerPromise from '../server.bootstrap';
import path from 'path';

describe('The /api/upload-file API:', () => {
  let server;

  before((done) => {
    bootstrapServerPromise.then((initializedServer) => {
      server = initializedServer;
    })
      .then(() => done());
  });

  describe('POST /', () => {
    it('should upload a file successfully', () => {
      const p = path.join(__dirname, '/assets/', path.basename('cat123.jpg'));
      request(server)
        .post('/api/upload-file')
        .field('Content-Type', 'multipart/form-data')
        .attach('photos', p)
        .expect(200, (err, res) => {
          expect(res.body).to.eql({});
          expect(err).to.be.null;
        });
    });
  });
});
