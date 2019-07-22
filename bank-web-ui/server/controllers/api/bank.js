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

import url from 'url';

import config from '../../../config';

const authURL = url.resolve(config.enkiExternalHydraUrl || '', '/oauth2/auth');

const registrationURL = url.resolve(config.enkiUrl || '', '/register');

const configObject = {
  bankName: config.bankName,
  bankTheme: config.bankTheme,
  enkiInfoUrl: config.enkiInfoUrl,
  enkiRegistrationUrl: registrationURL,
  clientId: config.enkiOauthClientId,
  enkiHydraUrl: authURL,
  enkiOauthCallback: config.enkiOauthCallback
};

function load(req, res) {
  res.send({ message: 'Please choose an action!' });
}

function getClientConfig(req, res) {
  res.send({ config: configObject });
}

function getEnkiOAuthDetails(req, res) {
  res.send({ clientId: config.enkiOauthClientId, callback: config.enkiOauthCallback, hydra: authURL });
}

function getEnkiUrls(req, res) {
  res.send({ enkiInfoUrl: config.enkiInfoUrl, enkiRegistrationUrl: config.enkiRegistrationUrl });
}

export default {
  load,
  getEnkiOAuthDetails,
  getEnkiUrls,
  getClientConfig
};