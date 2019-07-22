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

import chai from 'chai';
const expect = chai.expect;
import _ from 'lodash';
import Vue from 'vue';

const readModels = (vm) => {
  let labels = vm.$data.schema.groups.map(group => group.fields)
    .map(fields => fields.map(field => field.model))
    .reduce(
      (acc, val) => acc.concat(val),
      []);
  return labels;
};

const expectLabels = (fields, labels) => {
  let isLabels = fields.map(field => field.label);

  isLabels.sort();
  let shouldLabels = labels.sort();

  shouldLabels.forEach((shouldLabel, i) => {
    let isLabel = isLabels[i];
    if (isLabel !== shouldLabel) {
      // eslint-disable-next-line no-console
      console.error('#' + i + ': ' + isLabel + ' != ' + shouldLabel);
    }
    expect(isLabel).equals(shouldLabel);
    i ++;
  });
};

const getVM = (component, theme) => {
  let Ctor = Vue.extend(_.extend({
    render: () => '',
    props: ['theme']
  }, component));
  let vm = new Ctor({propsData: {theme: theme}}).$mount();
  return vm;
};

const getFields = (vm, group) => {
  let schema = vm.schema;
  let fields = schema.groups[group].fields;

  return fields;
};

const testAll = (component, labels, theme, group = 0, done) => {
  let vm = getVM(component, theme);
  let fields = getFields(vm, group);
  vm.$nextTick(() => {
    expect(vm.schema).not.be.undefined;

    if (fields.length !== labels.length) {
      // eslint-disable-next-line no-console
      console.error('fields.length: ' + fields.length + ' != labels.length:' + labels.length);

      let filtredFields = fields.map(field => {
        return field.label;
      });
      // eslint-disable-next-line no-console
      console.error('fields: ' + JSON.stringify(filtredFields));
      // eslint-disable-next-line no-console
      console.error('labels: ' + JSON.stringify(labels));
    }
    expect(fields.length).equals(labels.length);
    expectLabels(fields, labels);
    done();
  });
};

module.exports = {
  getVM,
  testAll,
  readModels
};
