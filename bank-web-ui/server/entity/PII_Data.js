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

import {Entity, PrimaryGeneratedColumn, Column, OneToOne} from 'typeorm';
import {User} from './User';
import logger from '../lib/logger';

@Entity('PII_Data')
export class PII_Data {

  @PrimaryGeneratedColumn({type: 'int'})
  id = undefined;

  @Column('text')
  firstName = '';

  @Column('text')
  lastName = '';

  @Column('text')
  email = '';

  @Column('text')
  streetName = '';

  @Column('text')
  phoneNumber = '';

  @Column('text')
  streetNumber = '';

  @Column('text')
  city = '';

  @Column('text')
  country = '';

  @Column('text')
  birthdate = '';

  @Column('text')
  phone = '';

  @Column('text')
  nationality = '';

  @Column('text')
  docno = '';

  @Column('text')
  documentType = '';

  @Column('text')
  zipCode = '';

  @Column('text')
  addressNumber = '';

  @Column('text')
  residenceAddress = '';

  @Column('text')
  province = '';

  @Column('text')
  gender = '';

  @Column('text')
  birthPlace = '';

  @Column('text')
  thirdCountries = '';

  @Column('text')
  activities = '';

  @Column('text')
  acceptTerms = '';

  @OneToOne(() => User, user => user.piiData)
  user = undefined;

  constructor(pii_json = '') {
    this.fromJson(pii_json);
  }

  fromJson(json) {
    if (!json) {
      return;
    }

    try {
      const jsonObj = typeof json === 'string' ? JSON.parse(json) : json;
      Object.assign(this, jsonObj);
    } catch(e) {
      logger.error('malformed JSON input received: ' + e);
    }
  }
}
