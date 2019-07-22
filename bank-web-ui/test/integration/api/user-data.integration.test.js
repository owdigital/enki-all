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

import util from 'util';
import request from 'supertest';
import diff from 'deep-diff';
import chai from 'chai';
const expect = chai.expect;

import logger from '../../../server/lib/logger';
import bootstrapServerPromise from '../server.bootstrap';
import populate from '../db.populate';


describe('The /api/user-data API:', () => {
  let server;

  // diff filter that skips ids as they're not stable
  var filterId = function(path, key) {
    return key === 'id';
  };

  var checkForEmptyDiff = function(expected, actual) {
    var changes = diff.diff(expected, actual, filterId);
    expect(util.inspect(changes)).to.be.equal('undefined'); // i.e. no changes
  };

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

  describe('GET /', () => {
    it('should respond with an array of users', () => {
      const expectedUsers = [
        {'id':1,'username':'testuser','password':'testPassWord','isInEnki':false, 'piiReceived': {}, 'piiData':{'id':3,'firstName':'testor','lastName':'','email':'','streetName':'','phoneNumber':'','streetNumber':'','city':'','country':'','birthdate':'','phone':'','nationality':'','docno':'','documentType':'','zipCode':'','addressNumber':'','residenceAddress':'','province':'','gender':'','birthPlace':'','thirdCountries':'','activities':'','acceptTerms':''}},
        {'id':2,'username':'testuser2','password':'testPassWord2','isInEnki':false, 'piiReceived': {}, 'piiData':{'id':1,'firstName':'testor2','lastName':'','email':'','streetName':'','phoneNumber':'','streetNumber':'','city':'','country':'','birthdate':'','phone':'','nationality':'','docno':'','documentType':'','zipCode':'','addressNumber':'','residenceAddress':'','province':'','gender':'','birthPlace':'','thirdCountries':'','activities':'','acceptTerms':''}},
        {'id':3,'username':'testuser3','password':'testPassWord3','isInEnki':false, 'piiReceived': {}, 'piiData':{'id':2,'firstName':'testor3','lastName':'','email':'','streetName':'','phoneNumber':'','streetNumber':'','city':'','country':'','birthdate':'','phone':'','nationality':'','docno':'','documentType':'','zipCode':'','addressNumber':'','residenceAddress':'','province':'','gender':'','birthPlace':'','thirdCountries':'','activities':'','acceptTerms':''}}
      ];

      return request(server)
        .get('/api/user-data')
        .expect('Content-Type', /json/)
        .expect(200)
        .expect((res) => {
          expect(res.body).to.have.lengthOf(3);
          var sortedBody = res.body;
          sortedBody.forEach((x) => {
            x.piiReceived = JSON.parse(x.piiReceived);
          });
          sortedBody.sort(function(a, b) {
            return a.id - b.id;
          });
          checkForEmptyDiff(expectedUsers, sortedBody);
        });
    });

    it('should respond the user with id #2', () => {
      const expectedUser = {'id':2,'username':'testuser2','password':'testPassWord2', 'piiReceived': {}, 'isInEnki':false,'piiData':{'id':2,'firstName':'testor2','lastName':'','email':'','streetName':'','phoneNumber':'','streetNumber':'','city':'','country':'','birthdate':'','phone':'','nationality':'','docno':'','documentType':'','zipCode':'','addressNumber':'','residenceAddress':'','province':'','gender':'','birthPlace':'','thirdCountries':'','activities':'','acceptTerms':''}};

      return request(server)
        .get('/api/user-data/2')
        .expect('Content-Type', /json/)
        .expect(200)
        .expect((res) => {
          expect(res.body).to.have.any.keys('id', 'username', 'password');
          res.body.piiReceived = JSON.parse(res.body.piiReceived);
          checkForEmptyDiff(expectedUser, res.body);
        });
    });

    it('should remove the user with id #3 from data store', () => {
      return request(server)
        .delete('/api/user-data/3')
        .expect(res => expect(res.body).to.not.have.any.keys('id', 'username', 'password'))
        .expect(204);
    });

    it('should NOT respond a user with id #3, should be deleted', () => {
      return request(server)
        .get('/api/user-data/3')
        .expect(404)
        .expect(res => expect(res.body).to.be.empty);
    });
  });

  let user_id;

  describe('POST /', () => {
    const expectedUser = {'id':4,'username':'testuser4','password':'testPassWord3', 'piiReceived':{},'isInEnki':false,'piiData':{'firstName':'testor3','lastName':'testor il last','email':'','streetName':'','phoneNumber':'','streetNumber':'','city':'Berlin','country':'','birthdate':'','phone':'','nationality':'','docno':'','documentType':'','zipCode':'','addressNumber':'','residenceAddress':'','province':'','gender':'','birthPlace':'','thirdCountries':'','activities':'','acceptTerms':''}};
    const requestBody = JSON.stringify(expectedUser);

    it('should insert an user locally and respond the user data', () => {

      return request(server)
        .post('/api/user-data/local')
        .set('Content-Type', 'application/json')
        .send(requestBody)
        .expect('Content-Type', /json/)
        .expect(200)
        .expect((res) => {
          expect(res.body).to.be.an('object').that.is.not.empty;
          res.body.piiReceived = JSON.parse(res.body.piiReceived);
          checkForEmptyDiff(expectedUser, res.body);
          user_id = res.body.id;
        });
    }).timeout(10000);

    it('should respond a user, the newly created one', () => {
      return request(server)
        .get('/api/user-data/' + user_id)
        .expect(200)
        .expect(res => {
          expect(res.body).to.be.an('object').that.is.not.empty;
          res.body.piiReceived = JSON.parse(res.body.piiReceived);
          checkForEmptyDiff(expectedUser, res.body);
        });
    });

    it('should return error if we try to re-insert an existing user', () => {

      return request(server)
        .post('/api/user-data/local')
        .set('Content-Type', 'application/json')
        .send(requestBody)
        .expect(400);
    });
  });

  describe('PUT /', () => {
    const expectedUser = {'id':user_id,'username':'testuser4','password':'testPassWord4', 'isInEnki':false,'piiData':{'firstName':'testor4','lastName':'testor il last','email':'','streetName':'','phoneNumber':'','streetNumber':'','city':'Berlin','country':'','birthdate':'','phone':'','nationality':'','docno':'','documentType':'','zipCode':'','addressNumber':'','residenceAddress':'','province':'','gender':'','birthPlace':'','thirdCountries':'','activities':'','acceptTerms':''}};

    const requestBody = '{"username":"testuser4","password":"testPassWord4","piiData":{"firstName":"testor4","lastName":"testor il last","email":"","streetName":"","phoneNumber":"","streetNumber":"","city":"Berlin","country":""} }';

    it('should update an existing user and respond the user data', () => {
      return request(server)
        .put('/api/user-data/' + user_id)
        .set('Content-Type', 'application/json')
        .send(requestBody)
        .expect('Content-Type', /json/)
        .expect(200)
        .expect((res) => {
          expect(res.body).to.be.an('object').that.is.not.empty;
          expect(res.body.piiReceived).to.be.equal('{}');
          delete res.body.piiReceived;
          checkForEmptyDiff(expectedUser, res.body);
        });
    });

    it('should with the newly created user', () => {
      return request(server)
        .get('/api/user-data/' + user_id)
        .expect(200)
        .expect((res) => {
          expect(res.body).to.be.an('object').that.is.not.empty;
          expect(res.body.piiReceived).to.be.equal('{}');
          delete res.body.piiReceived;
          checkForEmptyDiff(expectedUser, res.body);
        });
    });

    it('should not update an unknown user', () => {
      return request(server)
        .put('/api/user-data/999')
        .set('Content-Type', 'application/json')
        .send(requestBody)
        .expect(500)
        .expect(res => expect(res.body).to.be.empty);
    });
  });
});
