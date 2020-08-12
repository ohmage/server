-- Adds configuration option for enabling sha512 password hashing
-- with default value of false

INSERT INTO preference (p_key, p_value) VALUES 
  ('sha512_password_hash_enabled', 'false')
    ON DUPLICATE KEY UPDATE p_value=VALUES(p_value);

-- Changes password column length to 120 chars to accomadate sha512 password hashes
ALTER TABLE `user` MODIFY `password` VARCHAR(120);
