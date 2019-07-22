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

import setupHandlebars from './handlebars';
import setupMiddlewares from './middlewares';
import setupRoutes from './routes';

import csrfErrorHandler from '../lib/csrfErrorHandler';
import defaultErrorHandler from '../lib/defaultErrorHandler';

export default (app, config) => {
  setupHandlebars(app, config);
  setupMiddlewares(app, config);
  setupRoutes(app);

  // Mount error handlers (must come last)
  app.use(csrfErrorHandler(config));
  app.use(defaultErrorHandler(config));
};