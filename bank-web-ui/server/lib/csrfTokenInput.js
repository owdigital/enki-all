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

var _ = require('lodash');
var Handlebars = require('handlebars');

module.exports = function(csrfToken){
  var templateContext = this;

  // The token might either be passed as direct input to the helper or be already available in the handlebars view context
  csrfToken = _.isString(csrfToken) ? csrfToken : templateContext.csrfToken;

  // Suppress output if no CSRF token found
  if(!csrfToken){
    return null;
  }

  // If there is a token, render a hidden element with the name expected by csurf for CSRF validation
  return new Handlebars.SafeString('<input id="_csrf" name="_csrf" type="hidden" value="' + csrfToken + '">');
};