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

import logger from './logger';
import errorDetails from './errorDetails';

module.exports = function (config) {
  return function defaultErrorHandler(err, req, res, next) {
    var details = errorDetails(err);
    logger.error('SERVER ERROR:');
    logger.error(details);

    res.status(err.status || 500);

    if (config.errorDebug) {
      // Debug error messages
      res.send(details);
    } else {
      // Prod error messages
      res.send('A server error has occurred.');
    }

    // call any further registered error handlers.
    next(err);
  };
};
