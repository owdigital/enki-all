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

import {getSelect, getTextInput, getUsernameInput, getPasswordInput, getRadios, getDatePicker, getCheckbox} from './forms-helper';
import VueFormGenerator from 'vue-form-generator';

const schemas = {
  SignupStart: 1,
  PersonalData: 2,
  ActivityInfo: 3,
  PurposeOfRelationship: 4
};

function isBankA(theme) {
  return theme === 'bank-a';
}

function isBankB(theme) {
  return theme === 'bank-b';
}

function getSchema(schema, theme) {
  switch (schema) {
  case schemas.SignupStart:
    return signupStartSchema(theme);
  case schemas.PersonalData:
    return personalDataSchema(theme);
  case schemas.ActivityInfo:
    return activityInfoSchema();
  case schemas.PurposeOfRelationship:
    return purposeOfRelationshipSchema();
  default:
    break;
  }
}

function signupStartSchema(theme) {
  let fields = [];
  fields.push(getUsernameInput('Username', 'username'));
  fields.push(getPasswordInput('Password', 'password'));
  fields.push(getTextInput('First name', 'firstName', {required: true}));
  fields.push(getTextInput('Last name', 'lastName', {required: true}));
  fields.push(getTextInput('Email', 'email', {required: true, validatorType: VueFormGenerator.validators.email}));
  if (isBankA(theme)) {
    fields.push(getTextInput('Phone number', 'phoneNumber'));
  }
  fields.push(getRadios('Document type', 'documentType', ['Passport', 'NIF/NIE']));
  fields.push(getTextInput('Document number', 'docno'));
  fields.push(getSelect('Nationality', 'nationality', [
    { id: 'ESP', name: 'Spanish' },
    { id: 'ITA', name: 'Italian' },
    { id: 'DEU', name: 'German' },
    { id: 'FRA', name: 'French' }
  ]));

  let schema = {
    groups: [{
      legend: 'Welcome',
      fields: fields
    }]
  };
  return schema;
}

function personalDataSchema(theme) {
  let fields = [];
  fields.push(getDatePicker('Birth date', 'birthdate'));
  fields.push(getSelect('Birth place (country)', 'birthPlace', [
    { id: 'Spain', name: 'Spain' },
  ]));
  fields.push(getSelect('Birth place (province)', 'province', [
    { id: 'Madrid', name: 'Madrid' },
    { id: 'Toledo', name: 'Toledo' },
    { id: 'Valencia', name: 'Valencia' },
    { id: 'Sevilla', name: 'Sevilla' }
  ]));
  fields.push(getRadios('Gender', 'gender', ['Male', 'Female', ]));
  fields.push(getTextInput('Residence address', 'residenceAddress'));
  fields.push(getTextInput('Number', 'residenceAddressNumber'));
  fields.push(getTextInput('Zip code', 'zipCode'));
  fields.push(getTextInput('City', 'city'));
  if (isBankB(theme)) {
    fields.push(getSelect('Level of education', 'levelOfEducation', [
      { id: 'iBachiller', name: 'IBachiller, BUP, FP' },
      { id: 'fEspecial', name: 'F.Especial' },
      { id: 'primarios', name: 'Primarios' }
    ]));
  }
  let schema = {
    groups: [{
      legend: 'Your personal data',
      fields: fields
    }]
  };
  return schema;
}

function activityInfoSchema() {
  return {
    groups: [{
      legend: 'Confirm your identity',
      fields: [
        getRadios('Upload a photo of your ID or Passport', 'idOrPassport', ['NIE/NIF', 'Passport'], 'col-act-btn col-xs-8'),
        getTextInput('Introduce an account number from a different identity (IBAN)', 'iban')
      ]
    }, {
      legend: 'Activity Information',
      fields: [
        getSelect('Type of activity', 'typeOfActivity', [
          { id: 'employed', name: 'Employed' },
          { id: 'unemployed', name: 'Unemployed' },
        ]),
        getSelect('Sector', 'sector', [
          { id: 'pharmaceutical', name: 'Pharmaceutical' },
          { id: 'banking', name: 'Banking' },
          { id: 'education', name: 'Education' },
          { id: 'na', name: 'Not relevant' },
        ]),
        getSelect('Company name', 'companyName', [
          { id: 'phizer', name: 'Phizer' },
          { id: 'ratiopharm', name: 'ratiopharm' },
        ]),
        getSelect('Years employed', 'yearsEmployed', [
          { id: 'zero', name: 'None' },
          { id: 'less5', name: 'Less than 5' },
          { id: 'less10', name: 'From 5 to 10' },
          { id: 'more10', name: '10 or more' },
        ]),
        getSelect('Income range', 'incomeRange', [
          { id: 'income<30', name: '0-29,999' },
          { id: 'income30', name: '30,000-59,999' },
          { id: 'income60', name: '60,000-99,999' },
          { id: 'income100', name: '100,000-150,000' },
        ]),
        getRadios('Do you have any other sources of income?', 'otherSourcesOfIncome', ['Yes', 'No'], 'col-xs-8 yes-no')
      ]
    }]
  };
}

function purposeOfRelationshipSchema() {
  return {
    groups: [{
      legend: 'Purpose of relationship',
      fields: [
        getCheckbox('Transfer to your/other accounts', 'transferToOther', 'col-xs-8'),
        getCheckbox('Deposit cash > €1,000', 'depositCash', 'col-xs-8'),
        getCheckbox('Deposit checks', 'depositChecks', 'col-xs-8'),
        getCheckbox('Other', 'other', 'col-xs-8'),
        getRadios('Are you going to make regular transfers to third countries?', 'thirdCountries', ['Yes', 'No'], 'col-xs-8 yes-no')
      ]
    }, {
      legend: 'Other relevant information',
      fields: [
        getRadios('Are you/have you been entrusted with a public function during the last two years or are you family/related to someone who has? (father/mother, son/daughter, son-in-law/daughter-in-law, partner of a company)', 'publicFunction', ['Yes', 'No', ], 'col-xs-8 yes-no'),
        getRadios('Do you have US residence?', 'USCitizen', ['Yes', 'No'], 'col-xs-8 yes-no'),
        getRadios('Do you have fiscal obligations abroad?', 'fiscalObligation', ['Yes', 'No'], 'col-xs-8 yes-no')
      ]
    }]
  };
}

function getModel(theme) {
  // This is the JSON data for the PII request,
  // sorted by the names of the tabs and the (Groups).
  let model = {};
  //Signup start
  model.username = '';
  model.password = '';
  model.firstName = '';
  model.lastName = '';
  model.email = '';
  if(isBankA(theme)) {
    model.phoneNumber = '';
  }
  model.documentType = '';
  model.docno = '';
  model.nationality = '';
  // Your personal data
  model.birthdate = '';
  model.birthPlace = '';
  model.province = '';
  model.gender = '';
  model.residenceAddress = '';
  model.residenceAddressNumber = '';
  model.zipCode = '';
  model.city = '';
  // Activity Info
  // (Confirm your Identity)
  model.idOrPassport = '';
  model.iban = '';
  // (Activity Information)
  model.typeOfActivity = '';
  model.sector = '';
  model.companyName = '';
  model.yearsEmployed = '';
  model.incomeRange = '';
  model.otherSourcesOfIncome = '';
  // Purpose of relationship
  // (Purpose of relationship)
  model.transferToOther = '';
  model.depositCash = '';
  model.depositChecks = '';
  model.other = '';
  model.thirdCountries = '';
  // (Other relevant information)
  model.publicFunction = '';
  model.USCitizen = '';
  model.fiscalObligation = '';
  if(isBankB(theme)) {
    model.levelOfEducation = '';
  }
  return model;
}

function prefillModel(model, data) {
  if (!model) {
    console.error('Prefill attempt with no model!');
    return model;
  }

  if (!data) {
    console.error('Prefill attempt with no data!');
    return model;
  }

  for (let key in model) {
    if (model.hasOwnProperty(key) && data[key]) {
      model[key] = data[key];
    }
  }

  return model;
}

export default {
  getModel,
  getSchema,
  schemas,
  prefillModel,
  isBankA,
  isBankB
};
