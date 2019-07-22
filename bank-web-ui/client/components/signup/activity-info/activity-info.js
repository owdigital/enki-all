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

export default {
  props: ['subjectData', 'theme'],

  data() {
    return {
      uploadButtonDisabled: true,
      uploadButtonHidden: false,
      formOptions: {
        validationErrorClass: 'has-error',
        validationSuccessClass: 'has-success',
        validateAfterChanged: true
      },
      schema: SchemaGenerator.getSchema(SchemaGenerator.schemas.ActivityInfo, this.theme)
    };
  },

  methods: {
    show () {
      this.$modal.show('uploadModal');
    },
    hide () {
      this.$modal.hide('uploadModal');
    },
    upload () {
      const uploadBox = this.$refs.uploadBox;
      uploadBox.triggerUpload();
    },
    addFiles () {
      const uploadBox = this.$refs.uploadBox;
      uploadBox.addFiles();
    },
    statusChanged () {
      const uploadBox = this.$refs.uploadBox;
      this.uploadButtonDisabled = uploadBox.isAdded || uploadBox.isFailed ? false : true;
      this.uploadButtonHidden = uploadBox.isSuccess ? true : false;
    }
  },
  mounted () {
    $('.button-group').detach().appendTo('.col-act-btn');
  }
};
