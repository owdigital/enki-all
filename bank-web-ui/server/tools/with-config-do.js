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

import fs from 'fs';
import config from '../../config';


// Depends on NODE_ENV !
function dbDropFiles() {
  [
    config.db.file
  ].forEach(path => {
    fs.unlink(path, function(error) {
      if (error) {
        if (error.code !== 'ENOENT') {
          console.error(error.code);
        }
        return;
      }
      console.log(`db file ${path} removed`);
    });
  });
}


switch (process.argv[2]) {
case 'db:clean':
  dbDropFiles();
  break;
default:
  console.error('Unknown command');
}
