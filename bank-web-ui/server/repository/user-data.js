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

import Promise from 'bluebird';
import {getRepository} from 'typeorm';
import {User} from '../entity/User';
import {PII_Data} from '../entity/PII_Data';

function getUsers() {
  return getRepository(User).find({
    join: {
      alias: 'user',
      innerJoinAndSelect: {
        'piiData': 'user.piiData'
      }
    }
  });
}

function getUser(id) {
  return getRepository(User).findOne(id, {
    join: {
      alias: 'user',
      innerJoinAndSelect: {
        'piiData': 'user.piiData'
      }
    }
  });
}

function getUserByUsername(username) {
  return getRepository(User).findOne({ username: username }, {
    join: {
      alias: 'user',
      innerJoinAndSelect: {
        'piiData': 'user.piiData'
      }
    }
  }).then(user => {
    if (!user) return Promise.reject('user with username ' + username + ' not found');
    return user;
  });
}

function doesUserByUsernameExist(username) {
  return getRepository(User).findOne({ username: username }, {
    join: {
      alias: 'user',
      innerJoinAndSelect: {
        'piiData': 'user.piiData'
      }
    }
  }).then(user => {
    if (user) {
      return true;
    } else {
      return false;
    }
  });
}

function insertUser(userData) {
  const newUser = new User(userData.username, userData.password, userData.piiData);
  newUser.piiReceived = JSON.stringify(userData.piiReceived);
  return getRepository(User).save(newUser);
}

function insertUsers(userList) {
  return getRepository(User).save(userList);
}

function updateUser(userId, userData) {
  let userRepo = getRepository(User);
  return userRepo.findOne(userId, {
    join: {
      alias: 'user',
      innerJoinAndSelect: {
        'piiData': 'user.piiData'
      }}
  })
    .then(user => {
      if (!user) {
        // FIXME: shouldn't we return Promise.resolve() to trigger 404 ?
        return Promise.reject('update failed, user with id ' + userId + ' not found');
      }
      user.username = userData.username;
      user.password = userData.password;
      user.piiData.fromJson(userData.piiData);
      return userRepo.save(user);
    });
}

function markAsInEnki(userId) {
  let userRepo = getRepository(User);
  return getRepository(User).findOne(userId)
    .then(user => {
      if (!user) {
        // FIXME: shouldn't we return Promise.resolve() to trigger 404 ?
        return Promise.reject('update failed, user with id ' + userId + ' not found');
      }
      user.isInEnki = true;
      return userRepo.save(user);
    });
}

function deleteUser(removeId) {
  return getRepository(User).delete(removeId);
}

function clearUsers() {
  return Promise.all([
    getRepository(User).clear(),
    getRepository(PII_Data).clear() // cascadeRemove buggy
  ]);
}

export default {
  getUsers,
  getUser,
  getUserByUsername,
  insertUser,
  insertUsers,
  updateUser,
  deleteUser,
  clearUsers,
  markAsInEnki,
  doesUserByUsernameExist
};
