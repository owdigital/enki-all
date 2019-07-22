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

import moment from 'moment';
import Vue from 'vue';
import util from 'util';
import uuid from 'uuid';

function formatDate(value) {
  if (value) {
    const val = String(value);
    const dateFormat = 'MM/DD/YYYY HH:mm';
    // using utc to prevent timezone offsets
    return moment(val).utc().format(dateFormat);
  }
}

export default {
  data() {
    return {
      banks: [],
      linkedBanks: [],
      services: [],
      isLoggedIn: false,
      shareData: [],
      metaData: [],
      editedItem: null
    };
  },
  created() {
    setTimeout(() => {
      this.fetchUserData();
    }, 100);
  },
  beforeMount(){
    this.fetchData();
  },
  methods: {
    fetchUserData() {
      this.fetchBankData();
      this.fetchServiceData();
      this.isLoggedIn = window.app.context.loggedIn;
      if (this.isLoggedIn) {
        Vue.http.get('/userinfo', {params: {credentials: 'true'}}).then(response => {
          this.linkedBanks = response.body.banks;
          this.services = response.body.services;
        }, () => {
          console.error('Userinfo not fetched successfully');
        });
      }
    },
    fetchData() {
      Vue.http.get('/assertions', {params: {credentials: 'true'}}).then(response => {
        if (this.isValidResponseData(response.body)) {
          this.shareData = response.body.shareAssertions;
          this.metaData = response.body.metadataAssertions;
        }
      }, () => {
        // Not successful
        console.log('Assertions not fetched successfully');
      });
    },
    getUUID() {
      return uuid.v4();
    },
    isValidResponseData(data) {
      let isValid = false;
      if (typeof data === 'object') {
        if (Array.isArray(data.shareAssertions) || Array.isArray(data.metadataAssertions)){
          isValid = true;
        }
      }
      return isValid;
    },
    getFullLinkUrl(bankName) {
      return util.format("/linkacc/%s", encodeURI(bankName));
    },
    fetchBankData() {
      Vue.http.get('/api/banks').then(response => {
        this.banks = response.body;
      }, () => {
        console.error('Bank data not fetched successfully');
      });
    },
    fetchServiceData() {
      Vue.http.get('/api/services').then(response => {
        this.services = response.body;
      }, () => {
        console.error('Service data not fetched successfully');
      });
    },
    showLinkModal(item) {
      this.editedItem = item;
      this.$modal.show('modal-edit');
    },

    hideLinkModal() {
      this.$modal.hide('modal-edit');
    },
    updateUserInfo() {
      this.fetchUserData();
    },
    editItem() {
      this.$http.post(`/service/add`, this.editedItem).then(this.hideLinkModal).then(this.updateUserInfo);
    },
  },
  filters: {
    formatDate: formatDate
  },
};