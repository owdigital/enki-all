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

const FIRST_VIEW = 1;
const SECOND_VIEW = 2;

export default {
  props: ['subjectData', 'originalData'],

  data() {
    return {
      postResultHeader: '',
      postResultText: '',
      postResultDescription: '',
      message: 'empty',
      closeHandler: null,
      currentView: FIRST_VIEW,
      enkiInfoUrl: '/#/about-enki',
      enkiRegistrationUrl: '',
      formOptions: {
        validationErrorClass: 'has-error',
        validationSuccessClass: 'has-success',
        validateAfterChanged: true
      },
      signUpSuccessful: false,
      acceptCheckBox: false
    };
  },
  computed: {
    isFirstView() {
      return this.currentView === FIRST_VIEW;
    },
    isSecondView() {
      return this.currentView === SECOND_VIEW;
    },
    user() {
      return this.$store.getters.user;
    }
  },
  methods: {
    showModal() {
      this.$modal.show('activateEnkiModal');
      document.addEventListener('keydown', (e) => {
        if (e.keyCode === 27) {
          // hide popup if ESC button is hit
          this.hideModal();
        }
      });
    },
    hideModal() {
      this.$modal.hide('activateEnkiModal');
      if (this.closeHandler) {
        this.closeHandler();
        this.closeHandler = null;
      }
    },
    finalizeSignup() {
      if (!this.acceptCheckBox) {
        this.showResult('Sorry', 'Your bank account could not be created. You need to accept the Terms and Conditions first.');
        return false;
      }
      // Is being called as last-1 step of the signup process (before done)
      // If the signup has been completed go to next tab
      if (this.signUpSuccessful) {
        return true;
      }
      // If the signup hasn't been completed yet, save to bank's db
      this.saveToBank();
      return false;
    },

    saveToBank() {
      const userData = { username: this.subjectData.username, password: this.subjectData.password, piiData: this.subjectData, piiReceived: this.originalData};
      this.$http.post('/api/user-data/local', userData).then(() => {
        return new Promise((resolve) => {
          this.closeHandler = () => resolve(true);
          this.signUpSuccessful = true;
          this.currentView = FIRST_VIEW;
          this.showModal();
        });
      }, bankError => {
        this.showResult('Sorry', 'Your bank account could not be created.', bankError);
        console.error(bankError);
      });
    },

    showResult(header, text, resError = null) {
      this.currentView = SECOND_VIEW;
      this.postResultHeader = header;
      this.postResultText = text;
      if (resError && resError.hasOwnProperty('bodyText')) {
        this.postResultDescription = resError.bodyText;
      }
      this.showModal();
    },
    saveToENKI() {
      // In this step every action should end in next tab
      this.closeHandler = () => this.$emit('tandcFinished');

      // Problem: A user that is not logged in can't fetch the user id
      // Current solution: We are fetching the user by username
      // Todo: Create an endpoint that can tell whether an account has been added to ENKI
      this.$http.get('/api/user-data/username/' + this.subjectData.username).then(res => {
        const userData = res.body;
        return userData;
      }).then((userData) => {
        this.$http.post('/api/user-data/agent', userData).then(() => {
          this.showResult('Thank you', 'Your data has been sent successfully to Enki.');
        }, enkiError => {
          this.showResult('Sorry', 'Sending data to Enki has failed.', enkiError);
          console.error(enkiError);
        });
      });
    },
    hideModalAndGoToNextTab() {
      this.closeHandler = () => this.$emit('tandcFinished');
      this.hideModal();
    }
  },
  beforeCreate() {
    this.$http.get('api/bank/getEnkiUrls').then(res => {
      this.enkiInfoUrl = res.body.enkiInfoUrl;
      this.enkiRegistrationUrl = res.body.enkiRegistrationUrl;
    });
  }
};
