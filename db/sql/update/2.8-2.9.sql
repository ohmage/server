UPDATE preference SET p_value = 'http://rdev1.mobilizingcs.org/R/call/Mobilize/' WHERE p_key = 'visualization_server_address';
ALTER TABLE mobility ADD COLUMN uuid CHAR(36) NOT NULL;
UPDATE mobility SET uuid=UUID() WHERE uuid='';
ALTER TABLE mobility ADD CONSTRAINT UNIQUE (uuid);