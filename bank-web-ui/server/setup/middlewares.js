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

import logger from '../lib/logger';

import helmet from '../middleware/helmet';
import cookieParser from '../middleware/cookieParser';
import bodyParser from '../middleware/bodyParser';
import csurf from '../middleware/csurf';
import session from '../middleware/session';
import webpack from '../middleware/webpack';
import templateData from '../middleware/templateData';
import staticContent from '../middleware/staticContent';
import robots from '../middleware/robots';

export default (app, config) => {
  const templateDataMiddleware = templateData(config);
  // Wire the default "defines" object that the main layout will expose to client js files
  templateDataMiddleware.addContextProperty('csrfToken', req => req.csrfToken());

  // Mount middleware
  app.use(webpack(config));
  app.use(helmet(config));
  app.use(staticContent(config));
  app.use(cookieParser(config));
  app.use(bodyParser(config));
  app.use(session(config));
  app.use(csurf(config));
  app.use(templateDataMiddleware);
  app.use(robots(config));
  app.use(function(req, res, next) {
    logger.info(`${req.method} ${req.originalUrl}`);
    next();
  });
};
