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

module.exports = {
  rules: {
    'no-unused-expressions': 0
  },
  globals: {
    before: false, // "logger" is our global winston instance.  "false" means code can't overwrite it.
    after: false,
    beforeEach: false,
    afterEach: false,
    describe: false,
    it: false
  },
  parser: "babel-eslint",
  parserOptions: {
    "sourceType": "module" // change to module once we move to ecmascript modules
  }
};
