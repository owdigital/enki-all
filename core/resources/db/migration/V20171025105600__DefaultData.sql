SET client_encoding = 'UTF8';

-- Based off of https://github.com/lshift/enki-bank-web-ui/blob/develop/server/entity/PII_Data.js
INSERT INTO pii_type (id, description) VALUES
    ('firstName', 'First Name'),
    ('lastName', 'Last Name'),
    ('email', 'Email'),
    ('streetName', 'Street name'),
    ('phoneNumber', 'Phone number'),
    ('streetNumber', 'Street number'),
    ('city', 'City'),
    ('country', 'Country'),
    ('birthdate', 'Birth date'),
    ('nationality', 'Nationality'),
    ('docno', 'Document number'),
    ('documentType', 'Document type'),
    ('zipCode', 'Zip code'),
    ('addressNumber', 'Address number'),
    ('residenceAddress', 'Residence address'),
    ('province', 'Province'),
    ('gender', 'Gender'),
    ('birthPlace', 'Birth place');

-- Skipped thirdCountries, activities and phone, as they're not needed for PII Data

INSERT INTO sharing_purpose (id, description) VALUES ('a4cd2c37-786d-4601-a0b5-83e32b1e0077', 'For a bank account');
