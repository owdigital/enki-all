
-- :name -create-user! :returning-execute :one
INSERT INTO "user" ("name", "password") VALUES (:name, :password) RETURNING id, "name"

-- :name -get-user :query :one
SELECT id, name, admin FROM "user" WHERE (id = :id)

-- :name -get-user-by-name :query :one
SELECT id, name, admin FROM "user" WHERE (name = :name)

-- :name -get-user-by-name-password :query :one
SELECT id, name, password FROM "user" WHERE (name = :name)

-- :name -list-users :query :many
SELECT id, name FROM "user"

-- :name -update-user-password! :returning-execute :one
UPDATE "user" SET password = :password WHERE id = :id RETURNING id, "name"

-- :name -check-user-name :query :one
SELECT EXISTS(SELECT 1 from "user" where name = :name)

-- :name -is-user-bank :query :one
SELECT EXISTS(SELECT 1 from bank where user_id = :user)

-- :name -list-services :query :many
SELECT id, name, url FROM services

-- :name -get-services-for-user :? :many
SELECT services.id, services.url, services.name, service.proof_url, "user".id as user_id
FROM services 
full outer join service on (service.service_id = services.id)
full outer join "user" on ("user".id = service.user_id)
where ("user".id = :user-id or "user".id is NULL);