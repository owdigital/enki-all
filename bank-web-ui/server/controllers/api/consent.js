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
import repo from '../../repository/user-data';
import logger from '../../lib/logger';

const hydra = new Hydra({
  client: {
    id: process.env.HYDRA_CLIENT_ID || 'consent-app',
    secret: process.env.HYDRA_CLIENT_SECRET || 'consent-secret',
  },
  auth: {
    tokenHost: process.env.HYDRA_URL || 'http://localhost:4444',
    authorizePath: '/oauth2/auth',
    tokenPath: '/oauth2/token',
  },
  scope: 'hydra.keys.get'
});

const internalHydraHost = process.env.HYDRA_URL || 'http://localhost:4444';
const externalHydraHost = process.env.HYDRA_EXTERNAL_URL || internalHydraHost;

const consentCheckCatcher = (res, req) => (error) => {
  logger.error('Consent confirmation failed: ' + error
    + ' from ' + req.url);
  res.status(500).send(error);
};

function verifyConsent(req, res) {
  if (!req.session.login_user) {
    res.sendStatus(403);
    return;
  }
  logger.info('Verifying consent: ' + JSON.stringify(hydra));
  try { // too bad verifyConsentChallenge() doesn't reject() properly
    hydra.verifyConsentChallenge(req.params.challenge).then(({ challenge }) => {
      res.send({challenge: challenge, csrfToken: req.csrfToken()});
    }).catch(consentCheckCatcher(res, req));
  } catch (err) {
    consentCheckCatcher(res, req)({
      error: {name: err.name, message: JSON.stringify(err.details)},
      message: 'SERVER ERROR'});
  }
}

function confirmConsent(req, res) {
  if (!req.session.login_user) {
    res.sendStatus(403);
  }

  let challenge = req.body.challenge;
  let scopes = req.body.allowed_scopes;

  // const { email, email_verified, user_id: subject, name, nickname } = user
  let subject = 'username';
  // We intentionally don't provide an onRejected callback, so all errors are
  // handled in the catch().
  repo.getUserByUsername(req.session.login_user)
    .then(user => {
      subject = user.username;

      const data = {};

      if (!Array.isArray(scopes)) { // single item
        scopes = [scopes];
      }

      // // This is the openid 'profile' scope which should include some user profile data. (optional)
      // if (scopes.indexOf('profile') >= 0) {
      //   data.name = name
      //   data.nickname = nickname
      // }

      // // This is to fulfill the openid 'email' scope which returns the user's email address. (optional)
      // if (scopes.indexOf('email') >= 0) {
      //   data.email = email
      //   data.email_verified = email_verified
      // }

      logger.info('Confirming consent: ' + JSON.stringify(hydra));
      // Make sure that the consent challenge is valid
      hydra.verifyConsentChallenge(challenge).then(({ challenge: decoded }) => {
        return hydra.generateConsentResponse(challenge, subject, scopes, {}, data)
          .then(({ consent }) => {
            decoded.redir = decoded.redir.replace(internalHydraHost, externalHydraHost); // naming convention can be different internally and externally, so correct it
            logger.info('Confirming consent (redirect): ' + JSON.stringify(decoded));
            res.redirect(decoded.redir + '&consent=' + consent);
          });
      });
    })

    .catch(consentCheckCatcher(res, req));
}

module.exports = {
  verifyConsent,
  confirmConsent
};
