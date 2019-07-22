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

import {User} from '../../server/entity/User';
import {insertUsers, clearUsers} from '../../server/repository/user-data';

function populateDb() {
  return clearUsers().then(() => insertUsers([
    new User('testuser', 'testPassWord', '{"firstName": "testor"}', 1),
    new User('testuser2', 'testPassWord2', '{"firstName": "testor2"}', 2),
    new User('testuser3', 'testPassWord3', '{"firstName": "testor3"}', 3)
  ]));
}

function cleanupDb() {
  return clearUsers();
}

export default {
  populateDb,
  cleanupDb
};
