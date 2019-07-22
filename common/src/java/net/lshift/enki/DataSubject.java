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

package net.lshift.enki;

import net.lshift.spki.convert.Convert;

import java.text.ParseException;
import java.time.Instant;

@Convert.ByName("data-subject")
public class DataSubject extends EnkiType {
    public final String subjectType;
    public final String processorId;

    public DataSubject(String id, String subjectType, String processorId, String createdAt) throws ParseException {
        this(id, subjectType, processorId, Instant.from(RFC3339_FORMAT.parse(createdAt)));
    }

    public DataSubject(String id, String subjectType, String processorId, Instant createdAt) throws ParseException {
        super(id, createdAt);
        this.subjectType = subjectType;
        this.processorId = processorId;
    }
}
