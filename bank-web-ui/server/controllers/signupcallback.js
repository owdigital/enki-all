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

import Hydra from 'hydra-js';
import request from 'request-promise-native';
import url from 'url';
import uuidv4 from 'uuid/v4';

import config from '../../config';
import logger from '../lib/logger';

// todo: link this possibly with the pre-defined scopes from the schema-generator
const possibleScopes = ['firstName', 'lastName', 'email',
  'streetName', 'phoneNumber', 'streetNumber',
  'city', 'country', 'birthdate', 'nationality',
  'docno', 'documentType', 'zipCode', 'addressNumber',
  'residenceAddress', 'province', 'gender', 'birthPlace'];

const hydraConfig = {
  client: {
    id: config.enkiOauthClientId,
    secret: config.enkiOauthClientSecret,
  },
  auth: {
    tokenHost: config.enkiHydraUrl,
  },
};

const hydra = new Hydra(hydraConfig);
const agentDataURL = config.agentDataURL;

// This endpoint is at the end of the OAuth signup workflow.
// Hydra sends it three query params - `state`, `scopes`, `code`.
// Scopes is the scopes agreed by the user. State is a fraud check.
// Code is the authorisation code in OAuth2 workflow.
// The OAuth2 consumer can now use the code to get an OAuth2
// token which includes the information about metadata assertion IDs
// associated with the scopes.
function signupcallback(req, res) {
  hydra.authenticate().then((token) => {
    logger.debug(token);
    const state = req.query.states;
    const scopes = req.query.scopes;

    // TODO: verify that the state is the same as the state when initiating the OAuth2 request.
    logger.debug('State: ' + state);
    logger.debug('Scopes: ' + scopes);
    const code = req.query.code;
    const tokenConfig = {code: code, redirect_uri: config.enkiOauthCallback};

    // Get the OAuth token given auth code
    const tokenResultPromise = hydra.oauth2.authorizationCode.getToken(tokenConfig);

    tokenResultPromise
      .then((resp) => {
        const accessToken = resp.access_token;
        const enki_url = url.resolve(config.enkiBackchannelUrl || config.enkiUrl || '', 'api/sharelocations');
        return request.get(enki_url, {'auth': {'bearer': accessToken}});
      })
      .then((resp) => {
        const decodedData = {};
        JSON.parse(resp).forEach((x) => {
          decodedData[x.pii] = x.location;
        });
        const metadataAssertionIDs = {};
        possibleScopes.forEach((key) => {
          if (decodedData.hasOwnProperty(key)) {
            metadataAssertionIDs[key] = decodedData[key];
          }
        });
        // TODO: Create Share Assertion IDs given these metadataAssertionIDs
        const piiDataPromises = [];
        for (var id in metadataAssertionIDs) {
          piiDataPromises.push(request.get({
            uri: agentDataURL,
            qs: {file: metadataAssertionIDs[id]},
            json: true}));
        }

        return Promise.all(piiDataPromises);
      })
      .then((responses) => {

        const piiData = {};
        responses.forEach((response) => {
          piiData[response.data.piiType] = response.data.value;
        });

        // Redirect to signup page filled in with the data
        const piiDataId = uuidv4();
        if (!req.session.metaIds) {
          req.session.metaIds = {};
        }

        req.session.metaIds[piiDataId] = piiData;

        res.redirect('/#/signup/piiDataId/' + piiDataId);
      }).catch((error) => {
        logger.error(error);
        res.status(500).send('Something went wrong!');
      });
  }).catch((error) => {
    logger.error(error);
    res.status(500).send('Unable to connect to OAuth2 server!');
  });
}

export default {
  signupcallback: signupcallback
};
