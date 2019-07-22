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

var session = require('express-session');
var uuid = require('uuid');

// Session setup
// NOTE: By default, this will use in-memory sessions.  Each time node is restarted, all sessions will be lost.
// That means every update to the site will log users out (since their session are gone).
// See file- and mongodb- backed sessions for better use.
module.exports = function (config) {
  return session({
    secret: config.sessionSecret,
    genid: function () {
      return uuid.v4();
    },
    resave: false,
    saveUninitialized: true,
    cookie: {
      secure: config.secure, // Cookie will not be set over HTTP, only HTTPS, for hosted sites. See https://github.com/expressjs/session#cookie-options
      httpOnly: true // do not allow client-side script access to the cookie (this option is named rather confusingly; it's unrelated to HTTP vs. HTTPS)
    }
  });
};
