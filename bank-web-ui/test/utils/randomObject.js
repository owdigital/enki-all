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

'use strict';

var uuid = require('uuid');
var chai = require('chai');
var expect = chai.expect;

function setProperty(keyParts, target, value) {
  var nextPart = keyParts.shift();
  if (!keyParts.length) {
    target[nextPart] = value;
    return target[nextPart];
  }

  target[nextPart] = target[nextPart] || {};

  return setProperty(keyParts, target[nextPart], value);
}

function getProperty(keyParts, target) {
  var nextPart = keyParts.shift();
  if (!keyParts.length) {
    return target[nextPart];
  }

  return target[nextPart] ? getProperty(keyParts, target[nextPart]) : undefined;
}

var randomObjectInstances = {};

function objectInstance(instance) {
  var wrapper = {
    set: function (keyPath, value) {
      var keyParts = keyPath.split(/\./);
      var val = value || uuid.v4();
      setProperty(keyParts, instance, val);
      return val;
    },
    setInteger: function (keyPath) {
      var val = ~~(Math.random() * 100000);
      return wrapper.set(keyPath, val);
    },
    setExact: function(propertyString, value) {
      var val = value || uuid.v4();
      setProperty([propertyString], instance, val);
      return val;
    },
    get: function (keyPath) {
      var keyParts = keyPath.split(/\./);
      return getProperty(keyParts, instance);
    },
    getExact: function (propertyString) {
      return getProperty([propertyString], instance);
    },
    getObject: function () {
      return instance;
    },
    verify: function (keyPath, value) {
      var keyParts = keyPath.split(/\./);
      expect(value).to.equal(getProperty(keyParts, instance));
    }
  };

  return wrapper;
}

module.exports.instance = function getInstance(instanceName) {
  var name = instanceName || 'default';
  var instance = randomObjectInstances[name] = randomObjectInstances[name] || {};

  return objectInstance(instance);
};

module.exports.newInstance = function newInstance(instanceName) {
  var name = instanceName || 'default';
  var instance = randomObjectInstances[name] = {};

  return objectInstance(instance);
};
