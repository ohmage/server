-- ----------------------------------------------------------------------
-- Inserts a default preference key so as not to be statefully reliant on
-- the compiled war file.
-------------------------------------------------------------------------

INSERT INTO preference (p_key, p_value) VALUES 
  ('application_name', 'ohmage')
    ON DUPLICATE KEY UPDATE p_value=VALUES(p_value);
