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

import {Entity, Column, PrimaryGeneratedColumn, OneToOne, JoinColumn} from 'typeorm';
import {PII_Data} from './PII_Data';

@Entity()
export class User {

  @PrimaryGeneratedColumn({type: 'int'})
  id = undefined;

  @Column({type: 'text', unique: true})
  username = '';

  @Column('text')
  password = '';

  @Column({type: 'boolean', default: false})
  isInEnki = false;

  @Column({type: 'text', default: '{}'})
  piiReceived = '{}';

  @OneToOne(() => PII_Data, piiData => piiData.user, {
    cascade: true,
    eager: true
  })
  @JoinColumn()
  piiData = undefined;

  constructor(username, pwd, pii_json = '', id = undefined) {
    this.username = username;
    this.password = pwd;
    this.isInEnki = false;
    this.piiReceived = '{}';
    this.piiData = new PII_Data(pii_json);
    if (id !== undefined) {
      this.id = id;
    }
  }
}
