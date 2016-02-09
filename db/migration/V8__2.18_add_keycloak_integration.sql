-- Inserts a default preference key to determine keycloak enabledness
INSERT INTO preference (p_key, p_value) VALUES 
  ('keycloak_auth_enabled', 'false')
    ON DUPLICATE KEY UPDATE p_value=VALUES(p_value);

-- Inserts a default preference key to determine local auth enabledness
INSERT INTO preference (p_key, p_value) VALUES 
  ('local_auth_enabled', 'true')
    ON DUPLICATE KEY UPDATE p_value=VALUES(p_value);

-- Adds a bit column in user table to determine if user is local or external
ALTER TABLE user
    ADD COLUMN `external` bit NOT NULL DEFAULT FALSE;
