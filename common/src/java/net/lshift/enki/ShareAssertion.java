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

@Convert.ByName("share-assertion")
public class ShareAssertion extends EnkiType {
    public final String metadataId;
    public final String sharingProcessorId;
    public final String purposeId;
    public final Date consentStart;
    public final Date consentEnd;

    public ShareAssertion(String id, String metadataId, String sharingProcessorId, String purposeId, String createdAt, String consentStart, String consentEnd) throws ParseException {
        this(id, metadataId, sharingProcessorId, purposeId,
                Instant.from(RFC3339_FORMAT.parse(createdAt)),
                Instant.from(RFC3339_FORMAT.parse(consentStart)),
                Instant.from(RFC3339_FORMAT.parse(consentEnd)));
    }

    public ShareAssertion(String id, String metadataId, String sharingProcessorId, String purposeId, Instant createdAt, Instant consentStart, Instant consentEnd) throws ParseException {
        super(id, createdAt);
        this.metadataId = metadataId;
        this.sharingProcessorId = sharingProcessorId;
        this.purposeId = purposeId;
        this.consentStart = Date.from(consentStart);
        this.consentEnd = Date.from(consentEnd);
    }

    public Instant getConsentStart() {
        return consentStart.toInstant();
    }
    public Instant getConsentEnd() {
        return consentEnd.toInstant();
    }
}
