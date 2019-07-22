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

import sinon from 'sinon';
import chai from 'chai';
const expect = chai.expect;

import { testAll, readModels, getVM } from './signup-test-helper';

import signupComponent from '../../../../../client/components/signup/signup';
import signupStartComponent from '../../../../../client/components/signup/signup-start/signup-start';
import purposeOfRelationshipComponent from '../../../../../client/components/signup/purpose-of-relationship/purpose-of-relationship';
import activityInfoComponent from '../../../../../client/components/signup/activity-info/activity-info';
import personalDataComponent from '../../../../../client/components/signup/personal-data/personal-data';
import SchemaGenerator from '../../../../../client/components/signup/schemaGenerator';

describe('the signup component', () => {

  let sandbox;

  let activityInfoLabels;
  let activityInfoLabelsConfirm;
  let personalDataLabels;
  let signupStartLabels;
  let purposeOfRelationshipLabels;
  let purposeOfRelationshipLabelsOther;
  const theme = 'bank-a';

  before(() => {
    activityInfoLabelsConfirm = [
      'Upload a photo of your ID or Passport',
      'Introduce an account number from a different identity (IBAN)',
    ];

    activityInfoLabels = [
      'Type of activity',
      'Sector',
      'Company name',
      'Years employed',
      'Income range',
      'Do you have any other sources of income?'
    ];

    // Default: using bank-a
    // Using SchemaGenerator.isBankA(theme) like the real components
    signupStartLabels = [];
    signupStartLabels.push('Username');
    signupStartLabels.push('Password');
    signupStartLabels.push('First name');
    signupStartLabels.push('Last name');
    signupStartLabels.push('Email');
    // only used for bank-a, as in real component
    if (SchemaGenerator.isBankA(theme)) { signupStartLabels.push('Phone number'); }
    signupStartLabels.push('Document type');
    signupStartLabels.push('Document number');
    signupStartLabels.push('Nationality');

    personalDataLabels = [];
    personalDataLabels.push('Birth date');
    personalDataLabels.push('Birth place (country)');
    personalDataLabels.push('Birth place (province)');
    personalDataLabels.push('Gender');
    personalDataLabels.push('Residence address');
    personalDataLabels.push('Number');
    personalDataLabels.push('Zip code');
    personalDataLabels.push('City');
    // only used for bank-b, as in real component
    if (SchemaGenerator.isBankB(theme)) { personalDataLabels.push('Level of education'); }

    purposeOfRelationshipLabels = [
      'Transfer to your/other accounts',
      'Deposit cash > €1,000',
      'Deposit checks',
      'Other',
      'Are you going to make regular transfers to third countries?',
    ];

    purposeOfRelationshipLabelsOther = [
      'Are you/have you been entrusted with a public function during the last two years or are you family/related to someone who has? (father/mother, son/daughter, son-in-law/daughter-in-law, partner of a company)',
      'Do you have US residence?',
      'Do you have fiscal obligations abroad?',
    ];
  });

  beforeEach(() => {
    sandbox = sinon.createSandbox();
  });

  afterEach(() => {
    sandbox.restore();
  });

  describe('activityInfo component', () => {
    it('should contain activityInfo schemas group 0', (done) => {
      sandbox.stub(activityInfoComponent, 'mounted');
      testAll(activityInfoComponent, activityInfoLabelsConfirm, theme, 0, done);
    });

    it('should contain activityInfo schemas group 1', (done) => {
      sandbox.stub(activityInfoComponent, 'mounted');
      testAll(activityInfoComponent, activityInfoLabels, theme, 1, done);
    });
  });

  describe('signupStart component', () => {
    it('should contain signupStart schemas', (done) => {
      sandbox.stub(signupComponent, 'mounted');
      sandbox.stub(signupStartComponent, 'mounted');
      sandbox.stub(signupStartComponent, 'beforeCreate');
      testAll(signupStartComponent, signupStartLabels, theme, 0, done);
    });
  });

  describe('personalData component', () => {
    it('should contain personalData schemas', (done) => {
      testAll(personalDataComponent, personalDataLabels, theme, 0, done);
    });
  });

  describe('purposeOfRelationship component', () => {
    it('should contain purposeOfRelationship schemas group 0', (done) => {
      testAll(purposeOfRelationshipComponent, purposeOfRelationshipLabels, theme, 0, done);
    });

    it('should contain purposeOfRelationship schemas group 1', (done) => {
      testAll(purposeOfRelationshipComponent, purposeOfRelationshipLabelsOther, theme, 1, done);
    });
  });

  it('should contain all models', () => {
    sandbox.stub(activityInfoComponent, 'mounted');
    sandbox.stub(signupStartComponent, 'beforeCreate');
    sandbox.stub(signupStartComponent, 'mounted');
    sandbox.stub(signupComponent, 'mounted');
    const purposeOfRelationshipModel = readModels(getVM(purposeOfRelationshipComponent, theme));
    const signupStartModel = readModels(getVM(signupStartComponent, theme));
    const activityInfoModel = readModels(getVM(activityInfoComponent, theme));
    const personalDataModel = readModels(getVM(personalDataComponent, theme));

    let componentsModel = [];
    componentsModel = componentsModel.concat(activityInfoModel, personalDataModel, signupStartModel, purposeOfRelationshipModel);
    let generatorModel = SchemaGenerator.getModel(theme);
    generatorModel = Object.keys(generatorModel);

    componentsModel.sort();
    generatorModel.sort();

    generatorModel.forEach((m, i) => {
      if(m !== componentsModel[i]) {
        // eslint-disable-next-line no-console
        console.error(m + ' != ' + componentsModel[i]);
      }
      expect(m).equals(componentsModel[i]);
    });

    expect(generatorModel.length).equals(componentsModel.length);
  });
});
