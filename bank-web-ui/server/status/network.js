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

// This file has been left in as an example file for now until a more useful one (e.g. checking ADFS is up) can be written
// It shouldn't be activated because ping doesn't have a -c option on windows, so it will always register as an error.

var exec = require('child_process').exec;

module.exports.name = 'Network';
module.exports.description = 'verifies a basic connection to the internet by pinging google.com';
module.exports.disabled = true;

module.exports.test = function (done) {
  exec('ping -c 1 www.google.com', { cwd: __dirname }, function (err, stdout) {
    return done(err, !!stdout.replace(/\s/g, '').match(/64bytes/));
  });
};

module.exports.timeout = 500;