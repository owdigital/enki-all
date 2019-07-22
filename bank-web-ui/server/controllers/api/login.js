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

import repo from '../../repository/user-data';
import {LOGGED_IN, NOT_LOGGED_IN} from '../../../shared/constant';

function not_logged_in() {
  return JSON.stringify({state: NOT_LOGGED_IN});
}
function logged_in_as(user) {
  return JSON.stringify({state: LOGGED_IN, user: user});
}

function doLogin(req, res, next)
{
  if (!('username' in req.body)) return res.sendStatus(400);
  if (!('password' in req.body)) return res.sendStatus(400);
  res.set({'Content-type': 'application/json'});
  repo.getUserByUsername(req.body.username).then((item) => {
    if (item.password === req.body.password) {
      req.session.login_user = req.body.username;
      res.send(logged_in_as(req.session.login_user));
    }
    else {
      res.status(200).send(not_logged_in());
    }
  }, () => {
    res.status(200).send(not_logged_in());
  }).catch(next);
}

function checkLogin(req, res)
{
  res.set({'Content-type': 'application/json'});
  if (req.session.login_user !== undefined) {
    repo.getUserByUsername(req.session.login_user).then((user) => {
      res.send(logged_in_as(user));
    });
  }
  else {
    res.status(200)
      .send(not_logged_in());
  }
}

function logout(req, res)
{
  req.session.login_user = undefined;
  res.send('');
}

export default {
  doLogin,
  checkLogin,
  logout
};
