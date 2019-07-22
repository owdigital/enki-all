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
import config from '../../../config';
import logger from '../../../server/lib/logger';

describe('The /api/oidc API:', () => {
  let server;

  before((done) => {
    bootstrapServerPromise
      .then((initializedServer) => {
        server = initializedServer;
      })
      .then(() => done())
      .catch(error => logger.error('Couldn\'t setup test suite: '+error));
  });

  describe('GET /', () => {

    it('should always respond with Found', () => {
      return request(server)
        .get('/api/oidc/')
        .redirects(0)
        .expect(res => {
          expect(res.header.location).to.include(config.enkiCallbackUrl);
        })
        .expect(302);
    });

  });
});
