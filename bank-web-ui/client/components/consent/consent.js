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

module.exports = {
  name: 'consent',

  data () {
    return {
      error: null,
      csrfToken: null,
      clientName: '',
      scopes: [],
      challenge: '',
    };
  },

  computed: {
    isLoggedIn () {
      return this.$store.getters.loggedIn;
    }
  },

  created () {this.processChallenge();},

  methods : {
    processChallenge () {
      if (this.$route.query.error) {
        this.error = {name: this.$route.query.error, message: this.$route.query.error_description};
        return;
      }

      if (!this.$route.query.challenge) {
        this.error = {name: 'MissingParamChallenge', message: 'No challenge parameter provided.'};
        return;
      }

      /* FIXME: handle case of logout while on consent. This component whould be
         alerted of the logout by an ancestor's prop. The expected behaviour is
         probably interrupting the flow by rejecting the consent request and
         displaying an error. */

      if (!this.$store.getters.loggedIn) {
        this.$router.push({path: 'login', query:{consent: this.$route.query.challenge}});
        return;
      }

      if (this.$route.query.error) {
        this.error = {name: this.$route.query.error, message: this.$route.query.error_description};
        return;
      }

      this.challenge = this.$route.query.challenge;
      this.$http.get('api/consent/'+this.$route.query.challenge)
        .then(res => {
          this.csrfToken = res.body.csrfToken;
          this.clientName = res.body.challenge.aud;
          this.scopes = res.body.challenge.scp;
          return Promise.resolve();
        })

        .catch(err => {
          if (err !== 'break') {
            console.error(err);
            let e = err.body.error || err.body;
            this.error = {name: e.name, message: err.body.message + ': ' + e.message};
          }
        });
    }

  }

};
