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

import util from 'util';
import Vue from 'vue';

export default {
  data() {
    return {
      banks: [],
      linkedBanks: [],
      isLoggedIn: false,
    };
  },
  created() {
    setTimeout(() => {
      this.fetchBankData();
      this.isLoggedIn = window.app.context.loggedIn;
      const isAdmin = window.app.context.isAdmin;
      if (this.isLoggedIn && isAdmin) {
        this.$router.push({name: 'overview'});
      } else if (this.isLoggedIn && !isAdmin) {
        Vue.http.get('/userinfo', {params: {credentials: 'true'}}).then(response => {
          this.linkedBanks = response.body.banks;
        }, () => {
          console.error('Userinfo not fetched successfully');
        });
      }
    }, 100);
  },

  methods: {
    getFullLinkUrl(bankName) {
      return util.format("/linkacc/%s", encodeURI(bankName));
    },
    fetchBankData() {
      Vue.http.get('/api/banks').then(response => {
        this.banks = response.body;
      }, () => {
        console.error('Bank data not fetched successfully');
      });
    }
  }
};
