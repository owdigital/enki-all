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

/* eslint global-require: 0*/
import Promise from 'bluebird';
import logger from '../../server/lib/logger';

// Use this module to perform common initialization before server is started
// Return a promise that resolves with the server once initialization is done
// Common tasks:
//  - replace mongo-helper with mockgo in db.bootstrap.js
//  - creating test users
//  - adding data to the database
export default Promise.resolve()
  .then(() => {
    return require('../../server/db.bootstrap');
  })
  .then(() => {
    /* We want to make sure the db is bootstrapped before starting the
       app. Hence the Promise returned by db.bootstrap. The db is actually also
       bootstrapped in the startup scripts of server.js (but not executed
       twice). The problem is that although we would prefer to create the db
       connection in one place, in the server startup scripts, we have no means
       of wait for the server to complete its startup for us to use the db. */
    return require('../../server');
  })
  .catch(error => logger.error('Couldn\'t bootstrap server: '+error));
