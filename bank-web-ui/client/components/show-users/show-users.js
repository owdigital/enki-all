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

import _ from 'lodash';

export default {
  data() {
    return {
      items: [],
      editedItem: null,
      newItem : {
        username : '',
        password: '',
        piiData: {}
      }
    };
  },

  created() {
    this.$http.get('/api/user-data').then(res => {
      this.items = res.body;
    });
  },

  methods : {
    createItem() {
      this.$http.post('/api/user-data/local', this.newItem).then((res) => {
        this.items.push(res.body);
        this.newItem.username = '';
        console.log('Created user locally');
      });
      this.$http.post('/api/user-data/agent', this.newItem).then((res) => {
        this.items.push(res.body);
        this.newItem.username = '';
        console.log('Created user in agent');
      });
    },

    showEditModal(item) {
      this.editedItem = item;
      this.$modal.show('modal-edit');
    },

    hideEditModal() {
      this.$modal.hide('modal-edit');
    },

    editItem() {
      this.$http.put(`/api/user-data/${this.editedItem.id}`, this.editedItem).then(this.hideEditModal);
    },

    deleteItem(item) {
      this.$http.delete(`/api/user-data/${item.id}`).then(() => {
        this.items = _.without(this.items, item);
      });
    }
  }

};
