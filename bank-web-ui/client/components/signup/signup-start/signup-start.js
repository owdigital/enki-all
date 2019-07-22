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

import $ from 'jquery';
import SchemaGenerator from '../schemaGenerator';

// todo: link this possibly with the pre-defined scopes from the schema-generator
var possibleScopes = ['firstName', 'lastName', 'email',
  'streetName', 'phoneNumber', 'streetNumber',
  'city', 'country', 'birthdate', 'nationality',
  'docno', 'documentType', 'zipCode', 'addressNumber',
  'residenceAddress', 'province', 'gender', 'birthPlace'];

function encodeQueryData(data) {
  let ret = [];
  for (let d in data) {
    ret.push(encodeURIComponent(d) + '=' + encodeURIComponent(data[d]));
  }
  return ret.join('&');
}

export default {
  props: ['subjectData', 'theme'],

  data() {
    return {
      oauthdetails: {},

      formOptions: {
        validationErrorClass: 'has-error',
        validationSuccessClass: 'has-success',
        validateAfterChanged: true,
      },
      schema: SchemaGenerator.getSchema(SchemaGenerator.schemas.SignupStart, this.theme)
    };
  },
  mounted() {
    $('.existing-account').insertAfter($('legend').first());
  },
  methods: {
    getOauthUrl() {
      var oauthData = { nonce: 'somethingnonce', response_type: 'code', state: 'some_state', scope: possibleScopes.join(' ') };
      oauthData.client_id = this.oauthdetails.clientId;
      oauthData.redirect_uri = this.oauthdetails.callback;
      return this.oauthdetails.hydra + '?' + encodeQueryData(oauthData);
    }
  },
  beforeCreate() {
    this.$http.get('api/bank/getEnkiOAuthDetails').then(res => {
      this.oauthdetails = {};
      this.oauthdetails.callback = res.body.callback;
      this.oauthdetails.clientId = res.body.clientId;
      this.oauthdetails.hydra = res.body.hydra;
    });
  }
};
