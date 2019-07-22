
DROP INDEX user_association_user_id_bank_user_id_idx;
CREATE UNIQUE INDEX user_association_user_id_bank_id_idx ON user_association (user_id, bank_id);