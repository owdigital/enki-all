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

import querystring from 'querystring';

import controllerIndex from '../controllers/index';
import controllerSignupcallback from '../controllers/signupcallback';
import controllerApiBank from '../controllers/api/bank';
import controllerApiConsent from '../controllers/api/consent.js';
import controllerApiLogin from '../controllers/api/login';
import controllerOidc from '../controllers/api/oidc.js';
import controllerSession from '../controllers/api/session-pii-shared.js';
import controllerUpload from '../controllers/api/upload-file.js';
import controllerApiUserData from '../controllers/api/user-data';

export default (app) => {

  /* This is the entry point of the consent flow issued by hydra on a oauth2
     call. We can't directly plug hydra to '/#/consent?challenge=bla' because it
     re-interprets the URL to '/?challenge=bla#/consent'. */
  app.get('/consent/', (req, res) => {
    const query = querystring.stringify(req.query);
    res.redirect('/#/consent?' + query);
  });

  app.get('/', controllerIndex.getIndexPage);
  app.get('/signupcallback', controllerSignupcallback.signupcallback);
  app.get('/health', controllerIndex.getHealthPage);

  app.get('/api/bank/', controllerApiBank.load);
  app.get('/api/bank/getEnkiOAuthDetails', controllerApiBank.getEnkiOAuthDetails);
  app.get('/api/bank/getEnkiUrls', controllerApiBank.getEnkiUrls);
  app.get('/api/bank/getClientConfig', controllerApiBank.getClientConfig);

  app.get('/api/consent/:challenge', controllerApiConsent.verifyConsent);
  app.post('/api/consent', controllerApiConsent.confirmConsent);

  app.get('/api/login', controllerApiLogin.checkLogin);
  app.post('/api/login', controllerApiLogin.doLogin);
  app.post('/api/logout', controllerApiLogin.logout);

  app.get('/api/oidc', controllerOidc.oidcMock);

  app.get('/api/session-pii-shared/:id', controllerSession.getSessionPiiShared);
  app.get('/api/session-pii-shared', controllerSession.getSessionPiiShared);

  app.post('/api/upload-file', controllerUpload.upload);

  app.get('/api/user-data', controllerApiUserData.getUsers);
  app.get('/api/user-data/:id', controllerApiUserData.getUser);
  app.get('/api/user-data/username/:username', controllerApiUserData.getUserByUsername);
  app.post('/api/user-data/local', controllerApiUserData.insertUserLocally);
  app.post('/api/user-data/agent', controllerApiUserData.insertUserIntoAgent);
  app.put('/api/user-data/:id', controllerApiUserData.updateUser);
  app.delete('/api/user-data/:id', controllerApiUserData.deleteUser);
};