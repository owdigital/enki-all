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

import Handlebars from 'handlebars';
import handlebars from 'express-handlebars';
import _ from 'lodash';

import csrfTokenHelper from '../lib/csrfTokenInput';

var defaultHelpers = {
  json: JSON.stringify,
  csrfTokenInput: csrfTokenHelper
};

export default (app, config) => {
  // Register custom handlebars
  Handlebars.registerHelper('bankTheme', () => config.bankTheme);

  Handlebars.registerHelper('bankName', () => config.bankName);

  const handlebarsConfig = _.merge({}, config.handlebars, { helpers: defaultHelpers });

  app.set('views', handlebarsConfig.viewsDir);
  app.engine('.hbs', handlebars(handlebarsConfig));
  app.set('view engine', '.hbs');
};
