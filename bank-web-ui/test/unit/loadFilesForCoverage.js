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

var path = require('path');
var glob = require('glob');

it('loads all files for test coverage', function () {
  ['server'].forEach(function (directory) {
    glob.sync(path.resolve(__dirname, '../../', directory, '**/*.js')).forEach(function (filename) {
      require(filename); // eslint-disable-line global-require
    });
  });
}).timeout(20000); // because CI is often slow
