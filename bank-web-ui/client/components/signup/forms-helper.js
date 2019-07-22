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

import VueFormGenerator from 'vue-form-generator';

function setStyle(style) {
  return style ? style : 'col-xs-8';
}

let preId = 'enki-su-'; // enki signup prefix

function getTextInput(labelName, modelName, options={}) {
  return {
    type: 'input',
    inputType: 'text',
    id: preId + modelName,
    label: labelName,
    model: modelName,
    required: options.required || false,
    validator: options.validatorType || VueFormGenerator.validators.string,
    styleClasses: setStyle(options.style || '')
  };
}

function getUsernameInput(labelName, modelName) {
  const minLength = 1;
  let usernameValidator = function(value) {
    return new Promise((resolve) => {
      if (value.length < minLength) {
        resolve(['Username is too short']);
      }
      return fetch('/api/user-data/username/' + value).then(res => {
        if (res.ok) {
          resolve(['Username \"' + value + '\" already exists']);
        }
        else {
          resolve();
        }
      });
    });
  };
  return getTextInput(labelName, modelName, {required: true, validatorType: usernameValidator});
}

function getPasswordInput(labelName, modelName, options = {}) {
  return {
    type: 'password',
    label: labelName,
    id: preId + modelName,
    model: modelName,
    required: options.required || true,
    validator: options.validatorType || VueFormGenerator.validators.string,
    styleClasses: setStyle(options.style || '')
  };
}

function getSelect(labelName, modelName, selectValues, style = '') {
  return {
    type: 'select',
    id: preId + modelName,
    label: labelName,
    model: modelName,
    values() {
      return selectValues;
    },
    styleClasses: setStyle(style)
  };
}

function getRadios(labelName, modelName, selectValues, style = '') {
  return {
    type: 'radios',
    inputType: 'text',
    id: preId + modelName,
    label: labelName,
    model: modelName,
    values: selectValues,
    validator: VueFormGenerator.validators.string,
    styleClasses: setStyle(style)
  };
}

function getDatePicker(labelName, modelName) {
  return {
    type: 'datepick',
    id: preId + modelName,
    label: labelName,
    model: modelName,
    styleClasses: 'col-xs-8',
    initialView: 'year'
  };
}

function getCheckbox(labelName, modelName, style = '') {
  return {
    type: 'checkbox',
    id: preId + modelName,
    label: labelName,
    model: modelName,
    styleClasses: setStyle(style)
  };
}

export default {
  getTextInput,
  getUsernameInput,
  getPasswordInput,
  getSelect,
  getRadios,
  getDatePicker,
  getCheckbox
};
