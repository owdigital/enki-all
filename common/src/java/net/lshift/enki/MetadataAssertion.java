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
import java.util.Date;

import static java.lang.String.format;

@Convert.ByName("metadata-assertion")
public class MetadataAssertion extends EnkiType {
    public final String subjectId;
    public final String piiType;
    public final String processorId;
    public final String location;

    public MetadataAssertion(String id, String subjectId, String piiType, String processorId, String location, String createdAt) throws ParseException {
        this(id, subjectId, piiType, processorId, location, Instant.from(RFC3339_FORMAT.parse(createdAt)));
    }
    public MetadataAssertion(String id, String subjectId, String piiType, String processorId, String location, Instant createdAt) throws ParseException {
        super(id, createdAt);
        this.subjectId = subjectId;
        this.piiType = piiType;
        this.processorId = processorId;
        this.location = location;
    }

    @Override
    public String toString() {
        return format("<%s subject:%s; piiType: %s location:%s; createdAt:%s>",
                getClass().getSimpleName(), subjectId, piiType, location, createdAt);
    }
}
