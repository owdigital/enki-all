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

export default {
  data() {
    return {
      postResultText: undefined,
      connectionError: false
    };
  },
  computed: {
    isLoggedIn() {
      return this.$store.getters.loggedIn;
    },
    user() {
      return this.$store.getters.user;
    },
    isConnectedToENKI() {
      return this.user.isInEnki;
    }
  },
  methods: {
    connectToEnki() {
      const userId = this.user.id;
      if (!userId) {
        return;
      }
      this.$http.get('/api/user-data/' + userId).then(res => {
        const userData = res.body;
        return userData;
      }).then((userData) => {
        this.$http.post('/api/user-data/agent', userData).then(res => {
          this.postResultText = 'Customer data saved in DB:\n' + JSON.stringify(res.body, null, 2);
          this.user.isInEnki = true;
        }, error => {
          this.postResultText = 'error: ' + JSON.stringify(error, null, 2);
          this.connectionError = true;
        });
      });
    },
    normaliseKeyString(text) {
      const newText = text.replace(/([A-Z]+)/g, ' $1').replace(/([A-Z][a-z])/g, ' $1');
      return newText.charAt(0).toUpperCase() + newText.slice(1);
    }
  }
};