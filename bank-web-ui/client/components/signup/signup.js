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

import SchemaGenerator from './schemaGenerator';
import $ from 'jquery';

export default {
  props: ['piiDataId'],

  data() {
    return {
      bankTheme: null,
      model: {},
      originalData: {},
      formOptions: {
        validationErrorClass: 'has-error',
        validationSuccessClass: 'has-success',
        validateAfterChanged: true
      },
      postResultText: undefined,
      postResultStatus: undefined,
      postResultError: undefined
    };
  },
  mounted() {
    $('legend').wrap('<div class="legend-wrapper"></div>');

    this.$http.get('api/bank/getClientConfig').then(res => {
      let theme = res.body.config.bankTheme;
      this.bankTheme = theme;
      this.model = SchemaGenerator.getModel(theme);
      if (this.piiDataId) {
        this.$http.get('/api/session-pii-shared/' + this.piiDataId).then(piiRes => {
          this.originalData = piiRes.body;
          SchemaGenerator.prefillModel(this.model, piiRes.body);
        }, () => {
          console.error('no piiData fetched!');
        });
      }
    });
  },

  methods: {
    onComplete() {
      this.$router.push({path: '/login'});
    },
    validateSignupStartTab() {
      return this.$refs.signupStartForm.$refs.signupStartTabForm.validate();
    },
    validatePersonalDataTab() {
      return this.$refs.personalDataForm.$refs.personalDataTabForm.validate();
    },
    validateActivityInfoTab() {
      return this.$refs.activityInfoForm.$refs.activityInfoTabForm.validate();
    },
    validatePurposeOfRelationshipTab() {
      return this.$refs.purposeOfRelationshipForm.$refs.purposeOfRelationshipTabForm.validate();
    },
    validateTermsAndConditionsTab() {
      return this.$refs.termsAndConditionsForm.finalizeSignup();
    },

    prettyJSON(json) {
      if (json) {
        json = JSON.stringify(json);
        return json;
      } else {
        return 'no json';
      }
    }
  }
};