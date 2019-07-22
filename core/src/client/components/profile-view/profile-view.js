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
import helper from './profile-view-helper';

function formatDate(value) {
  if (value) {
    const val = String(value);
    const dateFormat = 'DD MMM YYYY';
    // using utc to prevent timezone offsets
    return moment(val).utc().format(dateFormat);
  }
}

export default {
  data() {
    return {
      bankUrl: '',
      banks: [],
      piiTypes: [],
      shareData: [],
      metaData: [],
      byBankData: [],
      byPIITypeData: [],
      selectedBank: '',
      selectedPIIType: '',
      byPIITypeColumns: [
        helper.column('Who', 'name', true),
        helper.column('Purpose', 'purpose', true),
        helper.dateColumn('Since', 'start_date'),
        helper.dateColumn('My consent expires on', 'end_date'),
        helper.column('', ''),
      ],
      byBankColumns: [
        helper.column('Detail', 'piitype', true),
        helper.column('Purpose', 'purpose', true),
        helper.dateColumn('Since', 'start_date'),
        helper.dateColumn('My consent expires on', 'end_date'),
        helper.column('', ''),
      ],
      editedItem: '',
    };
  },
  beforeMount() {
    this.fetchData();
  },
  methods: {
    fetchData() {
      Vue.http.get('/assertions', { params: { credentials: 'true' } }).then(response => {
        if (this.isValidResponseData(response.body)) {
          this.shareData = response.body.shareAssertions;
          this.metaData = response.body.metadataAssertions;
          this.banks = this.shareData.map((bank) => {
            return bank.name;
          }).filter((elem, pos, arr) => {
            return arr.indexOf(elem) === pos;
          }).sort();

          this.piiTypes = this.shareData.map((piiType) => {
            return piiType.piitype;
          }).filter((elem, pos, arr) => {
            return arr.indexOf(elem) === pos;
          }).sort();
        }
      }, () => {
        // Not successful
        console.log('Assertions not fetched successfully');
      });
    },
    selectedBankChanged() {
      this.byBankData = this.shareData.filter(item => item.name === this.selectedBank);
      const bankUrls = window.app.context.bankUrls;
      const bankUrlData = bankUrls.find(urls => urls.name === this.selectedBank);
      console.log('url data ' + JSON.stringify(bankUrlData));
      this.bankUrl = bankUrlData.url;
    },
    selectedPIITypeChanged() {
      this.byPIITypeData = this.shareData.filter(item => item.piitype === this.selectedPIIType);
    },
    isValidResponseData(data) {
      let isValid = false;
      if (typeof data === 'object') {
        if (Array.isArray(data.shareAssertions) || Array.isArray(data.metadataAssertions)) {
          isValid = true;
        }
      }
      return isValid;
    },
    deleteRow(props) {
      this.editedItem = props.row;
      this.$modal.show('modal-delete');
    },
    hideDeleteModal() {
      this.editedItem = '';
      this.$modal.hide('modal-delete');
    },
    editItem() {
      //not implemented yet
      console.log("Editing not implemented, yet.");
    },
    deleteItem() {
      //not implemented yet
      console.log("Deleting not implemented, yet.");
    },
  },
  filters: {
    formatDate: formatDate
  }};
