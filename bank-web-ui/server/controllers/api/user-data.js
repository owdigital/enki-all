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

import repo from '../../repository/user-data';
import requestpromise from 'request-promise';
import uuidv4 from 'uuid/v4';
import Promise from 'bluebird';
import winston from 'winston';

import config from '../../../config';

// list taken from https://github.com/lshift/enki-all/blob/develop/core/resources/db/migration/V20171025105600__DefaultData.sql#L4
const ALL_SCOPES = ['firstName', 'lastName', 'email', 'phoneNumber', 'documentType', 'docno', 'nationality', 'birthdate', 'birthPlace', 'province', 'gender', 'residenceAddress', 'zipCode', 'city', 'country'];

function getUsers(req, res, next) {
  repo.getUsers().then((items) => {
    res.set({'Content-type': 'application/json'});
    res.send(items);
  }, (error) => {
    res.set({'Content-type': 'application/json'});
    res.status(500).send(error);
  }).catch(next);
}

function getUser(req, res, next){
  repo.getUser(req.params.id).then((item) => {
    if(!item) {
      res.status(404).send();
    } else {
      res.set({'Content-type': 'application/json'});
      res.send(item);
    }
  }, (error) => {
    res.set({'Content-type': 'application/json'});
    res.status(500).send(error);
  }).catch(next);
}

function getUserByUsername(req, res, next){
  repo.getUserByUsername(req.params.username).then((item) => {
    if(!item) {
      res.status(404).send();
    } else {
      res.set({'Content-type': 'application/json'});
      res.send(item);
    }
  }, (error) => {
    if(error.startsWith('user with username')) {
      res.status(404).send(error);
    } else {
      res.set({'Content-type': 'application/json'});
      res.status(500).send(error);
    }
  }).catch(next);
}



function sendAgentMessage(message, method='GET'){
  return requestpromise({
    method: method,
    url: config.agentDataURL,
    body: message,
    json: true
  });
}

function dataSubjectMessage(subjectId, processorId){
  return {
    type: 'data-subject',
    id: subjectId,
    subjectType: 'user-bank-customer',
    processorId: processorId
  };
}

function piiDataMessage(subjectId, processorId, piiType, piiData){
  return {
    type: 'pii-data',
    id: uuidv4(),
    piiType: piiType,
    subjectId: subjectId,
    processorId: processorId,
    value: piiData
  };
}

function metaDataAssertionMessage(subjectId, processorId, piiType, location){
  return {
    type: 'metadata-assertion',
    id: uuidv4(),
    subjectId: subjectId,
    piiType: piiType,
    processorId: processorId,
    location: location,
    createdAt: (new Date()).toISOString()
  };
}

function shareAssertionMessage(metadataId, sharingProcessorId, purposeId){
  let endDate = new Date();
  endDate.setFullYear(endDate.getFullYear()+1); // 1 year as randomly selected
  return {
    type: 'share-assertion',
    id: uuidv4(),
    metadataId: metadataId,
    sharingProcessorId: sharingProcessorId,
    purposeId: purposeId,
    createdAt: (new Date()).toISOString(),
    consentStart: (new Date()).toISOString(),
    consentEnd: endDate
  };
}

function insertUserIntoAgent(req, res, next) {
  let bankname = config.bankName;
  let username = req.body.username;
  let purposeId = 'a4cd2c37-786d-4601-a0b5-83e32b1e0077'; // from https://github.com/lshift/enki-all/blob/develop/core/resources/db/migration/V20171025105600__DefaultData.sql#L26

  var subjectMsg = sendAgentMessage(dataSubjectMessage(username, bankname), 'POST');
  repo.getUser(req.body.id)
    .then((user) => {
      var piiReceived = {};
      if (user.piiReceived !== null && (typeof user.piiReceived === 'string' || user.piiReceived instanceof String)) {
        piiReceived = JSON.parse(user.piiReceived);
      }
      Promise.map(Object.keys(req.body.piiData), function(field) {
        if (ALL_SCOPES.includes(field) && !!req.body.piiData[field] && req.body.piiData[field] !== piiReceived[field]) {
          let metadata;
          return subjectMsg
            .then(function() {return sendAgentMessage(piiDataMessage(username, bankname, field, req.body.piiData[field]), 'POST');})
            .then(function(ret) {
              metadata = metaDataAssertionMessage(username, bankname, field, ret.location);
              return sendAgentMessage(metadata, 'POST');
            })
            .then(function() { return sendAgentMessage(shareAssertionMessage(metadata.id, bankname, purposeId), 'POST');});}
      });})
    .then(function(item) {
      repo.markAsInEnki(req.body.id);
      res.set({'Content-type': 'application/json'});
      res.send(item);
    })
    .catch(function(error) {
      res.set({'Content-type': 'application/json'});
      res.status(500).send(error);
    })
    .catch(next);
}

function insertUserLocally(req, res, next){
  var body = req.body;
  repo.doesUserByUsernameExist(body.username)
    .then((exists) => {
      if (exists) {
        return Promise.reject('User with username "' + body.username + '" already exists.');
      } else {
        return repo.insertUser(body);
      }
    })
    .then(function(item) {
      res.set({'Content-type': 'application/json'});
      res.send(item);
    })
    .catch(function(error) {
      if (typeof error === 'string' && error.startsWith('User with username')) {
        res.status(400).send(error);
      } else {
        res.set({'Content-type': 'application/json'});
        winston.log('info', 'Error while inserting user', {error: error.message, user: req.body});
        // Need to spell out fields as "message" doesn't turn up if we just hand in error
        res.status(500).send({message: error.message, errno: error.errno, code: error.code});
      }
    })
    .catch(next);
}

// This updated an user locally
function updateUser(req, res, next){
  repo.updateUser(req.params.id, req.body).then((item) => {
    if(!item) {
      res.status(404).send();
    } else {
      res.set({'Content-type': 'application/json'});
      res.status(200).send(item);
    }
  }, (error) => {
    res.status(500).send(error);
  }).catch(next);
}

// This deletes an user locally
function deleteUser(req, res, next){
  repo.deleteUser(req.params.id).then(() => {
    res.status(204).send();
  }, (error) => {
    res.status(500).send(error);
  }).catch(next);
}

export default {
  getUsers,
  getUser,
  getUserByUsername,
  insertUserIntoAgent,
  insertUserLocally,
  updateUser,
  deleteUser
};
