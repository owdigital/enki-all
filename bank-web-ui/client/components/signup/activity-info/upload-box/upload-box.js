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
const STATUS_INITIAL = 0, STATUS_ADDED = 1, STATUS_SAVING = 2, STATUS_SUCCESS = 3, STATUS_FAILED = 4;

export default {
  props: ['subjectData'],

  before() {
    this.formData = new FormData();
  },

  data() {
    return {
      addedFiles: [],
      formData: null,
      uploadedFiles: [],
      uploadError: null,
      currentStatus: null,
      uploadFieldName: 'photos'
    };
  },
  computed: {
    isInitial() {
      return this.currentStatus === STATUS_INITIAL;
    },
    isAdded() {
      return this.currentStatus === STATUS_ADDED;
    },
    isSaving() {
      return this.currentStatus === STATUS_SAVING;
    },
    isSuccess() {
      return this.currentStatus === STATUS_SUCCESS;
    },
    isFailed() {
      return this.currentStatus === STATUS_FAILED;
    }
  },

  watch: {
    currentStatus() {
      this.$emit('statusChanged');
    }
  },

  methods: {
    addFiles () {
      $('input[type="file"]').click();
    },
    reset() {
      console.log('reset');
      this.formData = new FormData();
      this.currentStatus = STATUS_INITIAL;
      this.uploadedFiles = [];
      this.addedFiles = [];
      this.uploadError = null;
    },
    filesChange(fieldName, fileList) {
      console.log('files change');

      if (!fileList.length) return;
      Array.from(Array(fileList.length).keys())
        .map(x => {
          this.formData.append(fieldName, fileList[x], fileList[x].name);
          this.addedFiles.push({name: fileList[x].name});
        });

      this.renderImages()
        .then(x => {
          console.log('render success');
          this.addedFiles = [].concat(x);

          this.currentStatus = STATUS_ADDED;
        }).catch(err => {
          console.log('render error: ' + err);
        });
    },
    triggerUpload() {
      if (this.formData) {
        this.upload();
        console.log('saving form data');
      } else {
        console.log('no form data');
      }
    },
    upload() {
      console.log('save');
      this.currentStatus = STATUS_SAVING;
      this.$http.post('/api/upload-file', this.formData).then(() => {
        console.log('upload success');
        this.uploadedFiles = this.addedFiles;
        this.currentStatus = STATUS_SUCCESS;
        this.formData = new FormData();
      }).catch(err => {
        console.log('upload error');
        this.uploadError = err.response;
        this.currentStatus = STATUS_FAILED;
      });
    },
    renderImages() {
      const photos = this.formData.getAll(this.uploadFieldName);
      return Promise.all(photos.map(x => this.getImage(x)
        .then(img => ({
          id: x,
          originalName: img.name,
          fileName: img.name,
          url: img.src
        }))));
    },
    getImage(file) {
      return new Promise((resolve) => {
        const fReader = new FileReader();
        let img = document.createElement('img');

        fReader.onload = () => {
          img.src = fReader.result;
          resolve(img);
        };

        fReader.readAsDataURL(file);
      });
    }
  },
  mounted() {
    this.reset();
  }
};
