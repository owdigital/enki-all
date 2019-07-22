SET client_encoding = 'UTF8';

-- Change description of zipCode PII type

UPDATE pii_type SET description = 'Post code' WHERE id = 'zipCode';