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

import Vue from 'vue';
import Constants from '../../shared/constant';

export default {
  state: {
    loggedIn: false,
    user: {}
  },

  getters: {
    loggedIn: state => state.loggedIn,
    user: state => state.user
  },

  mutations: {
    setLoggedin: (state) => {
      state.loggedIn = true;
    },
    setLoggedout: (state) => {
      state.loggedIn = false;
    },
    setUser: (state, user) => {
      state.user = user;
    }
  },

  actions: {
    login ({ commit }, user) {
      return new Promise((resolve, reject) => {
        Vue.http.post('api/login', user).then((res) => {
          if (res.body.state === Constants.LOGGED_IN){
            commit('setLoggedin');
            resolve();
            this.dispatch('checkLoggedIn');
          } else {
            commit('setLoggedout');
            reject(res.body.state);
          }
        }, (failure) => {
          commit('setLoggedout');
          console.log('Error: ');
          console.log(failure);
          reject(failure);
        });
      });
    },

    // this check should only be used once by a initial component load!
    checkLoggedIn({commit}) {
      Vue.http.get('api/login').then(res => {
        if (res.body.state === Constants.LOGGED_IN){
          commit('setLoggedin');
          commit('setUser', res.body.user);
        } else {
          commit('setLoggedout');
          commit('setUser');
        }
      }, () => {
        commit('setLoggedout');
        commit('setUser');
      });
    },

    logout({commit}) {
      return new Promise((resolve, reject) => {
        Vue.http.post('api/logout').then(() => {
          commit('setLoggedout');
          resolve();
        }, () => {
          reject();
        });
      });
    }

  }
};
