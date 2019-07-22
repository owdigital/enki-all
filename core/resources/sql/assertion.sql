-- :name -insert-bank! :returning-execute :one
INSERT INTO bank ("user_id", "pub_key", "consus_user", "agent_url", "oauth_client_id") VALUES (:user, :pub-key, :consus-user, :agent-url, :oauth-client-id) RETURNING id

-- :name -get-banks :query :many
SELECT bank.id, "user".name, bank.pub_key, bank.consus_user, bank.agent_url
FROM bank
INNER JOIN "user" ON ("user".id = bank.user_id)

-- :name -get-bank-by-name :query :one
SELECT bank.id, "user".name, bank.pub_key, bank.consus_user, bank.agent_url
FROM bank
INNER JOIN "user" ON ("user".id = bank.user_id)
WHERE ("user".name = :name) LIMIT 1

-- :name -get-bank-by-id :query :one
SELECT bank.id, "user".name, bank.pub_key, bank.consus_user, bank.agent_url
FROM bank
INNER JOIN "user" ON ("user".id = bank.user_id)
WHERE (bank.id = :id) LIMIT 1

-- :name -get-bank-by-oauth-client :query :one
SELECT bank.id, "user".name, bank.pub_key, bank.consus_user, bank.agent_url
FROM bank
INNER JOIN "user" ON ("user".id = bank.user_id)
WHERE (bank.oauth_client_id = :oauth-client-id) LIMIT 1

-- :name -get-pii-type :query :one
SELECT id, description FROM pii_type WHERE (id = :id)

-- :name -insert-pii-type! :returning-execute :one
INSERT INTO pii_type ("id", "description")
VALUES (:id, :description)
RETURNING id

-- :name -insert-metadata-assertion! :returning-execute :one
INSERT INTO metadata_assertion ("id", "subject", "pii_type_id", "bank_id", "location", "created", "signature")
VALUES (:id, :subject, :pii_type, :bank_id, :location, :created, :signature)
RETURNING id

-- :name -get-metadata-assertion :query :one
SELECT metadata_assertion.id
FROM metadata_assertion
LEFT JOIN metadata_assertion_revocation ON (metadata_assertion.id = metadata_assertion_revocation.metadata_assertion_id)
WHERE
    metadata_assertion.id = :id
    AND ((metadata_assertion_revocation.id IS NULL) OR (metadata_assertion_revocation.valid_from > :now))
LIMIT 1

-- :name -get-metadata-assertions :query :many
SELECT "user".name, metadata_assertion.id, metadata_assertion.location, metadata_assertion.created, pii_type.description as piitype, metadata_assertion.subject
FROM bank
    INNER JOIN "user" ON ("user".id = bank.user_id)
    INNER JOIN user_association ON (user_association.bank_id = bank.id)
    INNER JOIN metadata_assertion ON (metadata_assertion.bank_id = bank.id AND metadata_assertion.subject = user_association.bank_user_id)
    INNER JOIN pii_type ON (pii_type.id = metadata_assertion.pii_type_id)
    LEFT JOIN metadata_assertion_revocation ON (metadata_assertion.id = metadata_assertion_revocation.metadata_assertion_id)
WHERE
    user_association.user_id = :user
    AND ((metadata_assertion_revocation.id IS NULL) OR (metadata_assertion_revocation.valid_from > :now))

-- :name -get-purpose :query :one
SELECT id, description FROM sharing_purpose WHERE (id = :id)

-- :name -insert-purpose! :returning-execute :one
INSERT INTO sharing_purpose ("id", "description")
VALUES (:id, :description)
RETURNING id

-- :name -insert-share-assertion! :returning-execute :one
INSERT INTO share_assertion ("metadata_assertion_id", "sharing_bank_id", "signature", "purpose_id", "created", "start_date", "end_date")
VALUES (:metadata-assertion-id, :sharing_bank_id, :signature, :purpose, :created, :consent-start, :consent-end)
RETURNING id

-- :name -get-share-assertions :query :many
SELECT "user".name, share_assertion.id, share_assertion.metadata_assertion_id, sharing_purpose.description as purpose, share_assertion.created, share_assertion.start_date, share_assertion.end_date, pii_type.description as piitype
FROM bank
    INNER JOIN user_association ON (user_association.bank_id = bank.id)
    INNER JOIN metadata_assertion ON (metadata_assertion.bank_id = bank.id AND metadata_assertion.subject = user_association.bank_user_id)
    INNER JOIN pii_type ON (pii_type.id = metadata_assertion.pii_type_id)
    INNER JOIN share_assertion ON (share_assertion.metadata_assertion_id = metadata_assertion.id)
    INNER JOIN sharing_purpose ON (share_assertion.purpose_id = sharing_purpose.id)
    INNER JOIN bank AS bank2 ON (share_assertion.sharing_bank_id = bank2.id)
    INNER JOIN "user" ON ("user".id = bank2.user_id)
    LEFT JOIN share_assertion_revocation ON (share_assertion.id = share_assertion_revocation.share_assertion_id)
WHERE
    user_association.user_id = :user
    AND share_assertion.end_date >= :now
    AND ((share_assertion_revocation.id IS NULL) OR (share_assertion_revocation.valid_from > :now));

-- :name -insert-user-association! :returning-execute :one
INSERT INTO user_association ("user_id", "bank_id", "bank_user_id")
VALUES (:user-id, :bank-id, :bank-user-id)
RETURNING id

-- :name -get-user-association :query :one
SELECT id, user_id, bank_id, bank_user_id
FROM user_association
WHERE
    (user_id = :user-id)
    AND (bank_id = :bank-id)
LIMIT 1

-- :name -get-user-associations :query :many
SELECT id, user_id, bank_id, bank_user_id
FROM user_association
WHERE (user_id = :user-id)

-- :name -create-metadata-assertion-revocation :returning-execute :one
INSERT INTO metadata_assertion_revocation ("created", "valid_from", "user_id", "metadata_assertion_id")
    VALUES (:now, :valid-from, :user, :assertion) RETURNING id

-- :name -create-share-assertion-revocation :returning-execute :one
INSERT INTO share_assertion_revocation ("created", "valid_from", "user_id", "share_assertion_id")
    VALUES (:now, :valid-from, :user, :assertion) RETURNING id

-- :name -get-metadata-linked-share-assertions :query :many
SELECT share_assertion.id
FROM metadata_assertion
INNER JOIN share_assertion ON (metadata_assertion.id = share_assertion.metadata_assertion_id)
WHERE metadata_assertion.id = :id

-- :name -get-metadata-revocation :query :one
SELECT metadata_assertion_revocation.id
FROM metadata_assertion
INNER JOIN metadata_assertion_revocation ON (metadata_assertion_revocation.metadata_assertion_id = metadata_assertion.id)
WHERE metadata_assertion.id = :id
LIMIT 1

-- :name -get-share-revocation :query :one
SELECT share_assertion_revocation.id
FROM share_assertion
INNER JOIN share_assertion_revocation ON (share_assertion_revocation.share_assertion_id = share_assertion.id)
WHERE share_assertion.id = :id
LIMIT 1

-- :name -get-bank-metadata-assertions :query :many
SELECT "user".name, metadata_assertion.id, metadata_assertion.location, metadata_assertion.created, pii_type.description as piitype, metadata_assertion.subject
FROM bank
    INNER JOIN "user" ON ("user".id = bank.user_id)
    INNER JOIN metadata_assertion ON (metadata_assertion.bank_id = bank.id)
    INNER JOIN pii_type ON (pii_type.id = metadata_assertion.pii_type_id)
    LEFT JOIN metadata_assertion_revocation ON (metadata_assertion.id = metadata_assertion_revocation.metadata_assertion_id)
WHERE
    "user".id = :user
    AND ((metadata_assertion_revocation.id IS NULL) OR (metadata_assertion_revocation.valid_from > :now))

-- :name -get-bank-share-assertions :query :many
SELECT "user".name, share_assertion.id, share_assertion.metadata_assertion_id, sharing_purpose.description as purpose, share_assertion.created, share_assertion.start_date, share_assertion.end_date, pii_type.description as piitype
FROM bank
    INNER JOIN "user" ON ("user".id = bank.user_id)
    INNER JOIN share_assertion ON (share_assertion.sharing_bank_id = bank.id)
    INNER JOIN sharing_purpose ON (share_assertion.purpose_id = sharing_purpose.id)
    INNER JOIN metadata_assertion ON (metadata_assertion.id = share_assertion.metadata_assertion_id)
    INNER JOIN pii_type ON (pii_type.id = metadata_assertion.pii_type_id)
    LEFT JOIN share_assertion_revocation ON (share_assertion.id = share_assertion_revocation.share_assertion_id)
WHERE
    "user".id = :user
    AND share_assertion.end_date >= :now
    AND ((share_assertion_revocation.id IS NULL) OR (share_assertion_revocation.valid_from > :now))

-- :name -get-metadata-assertions-given-pii-types :query :many
SELECT metadata_assertion.id, pii_type.id as pii, pii_type.description, "user".name as bank_name, metadata_assertion.location, bank.id as bank_id
FROM metadata_assertion
INNER JOIN user_association ON (metadata_assertion.subject = user_association.bank_user_id)
INNER JOIN pii_type ON (pii_type.id = metadata_assertion.pii_type_id)
INNER JOIN bank ON (bank.id = metadata_assertion.bank_id)
INNER JOIN "user" ON ("user".id = bank.user_id)
LEFT JOIN metadata_assertion_revocation ON (metadata_assertion.id = metadata_assertion_revocation.metadata_assertion_id)
WHERE
    pii_type.id IN (:v*:pii-types)
    AND user_association.user_id = :user
    AND ((metadata_assertion_revocation.id IS NULL) OR (metadata_assertion_revocation.valid_from > :now))

-- :name -get-metadata-assertion-locations :query :many
SELECT metadata_assertion.id::TEXT, pii_type.id as pii, metadata_assertion.location
FROM
    metadata_assertion
    INNER JOIN pii_type ON (pii_type.id = metadata_assertion.pii_type_id)
    LEFT JOIN metadata_assertion_revocation ON (metadata_assertion.id = metadata_assertion_revocation.metadata_assertion_id)
WHERE
    metadata_assertion.id IN (:v*:ids)
    AND ((metadata_assertion_revocation.id IS NULL) OR (metadata_assertion_revocation.valid_from > :now))

-- :name -add-service :returning-execute :one
INSERT INTO service ("user_id", "service_id", "proof_url")
    VALUES (:user-id, :service-id, :proof-url) RETURNING id
