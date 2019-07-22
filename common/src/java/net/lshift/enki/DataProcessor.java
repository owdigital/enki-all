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

@Convert.ByName("data-processor")
public class DataProcessor extends EnkiType {
    public final String displayName;

    public DataProcessor(String id, String displayName, String createdAt) throws ParseException {
        this(id, displayName, Instant.from(RFC3339_FORMAT.parse(createdAt)));
    }

    public DataProcessor(String id, String displayName, Instant createdAt) throws ParseException {
        super(id, createdAt);
        this.displayName = displayName;
    }
}
