-- Update the R server.
UPDATE preference SET p_value = 'http://rdev.mobilizingcs.org/R/call/Mobilize/' WHERE p_key = 'visualization_server_address';

-- Alter the Mobility table by droping the timestamp.
ALTER TABLE mobility DROP COLUMN msg_timestamp;

-- Update the Mobility table by adding the UUID.
ALTER TABLE mobility ADD COLUMN uuid CHAR(36) NOT NULL;
UPDATE mobility SET uuid=UUID() WHERE uuid='';
ALTER TABLE mobility ADD CONSTRAINT UNIQUE (uuid);

-- Alter the survey response table by droping the timestamp.
ALTER TABLE survey_response DROP COLUMN msg_timestamp;

-- Alter the survey response table by adding the UUID.
ALTER TABLE survey_response ADD COLUMN uuid CHAR(36) NOT NULL;
UPDATE survey_response SET uuid=UUID() WHERE uuid='';
ALTER TABLE survey_response ADD CONSTRAINT UNIQUE (uuid);

-- Update the preference table to add the new preference indicating whether or
-- not a privileged user in a class can view the Mobility data for everyone 
-- else in that class.
INSERT INTO preference VALUES 
    ('privileged_user_in_class_can_view_others_mobility', 'true'),
    ('mobility_enabled', 'true');

-- We probably want to drop the pre-existing constraints for Mobility and 
-- survey responses, but try as I might I couldn't find any documentation on
-- how to do it. ALTER TABLE's DROP CONSTRAINT appears to not be implemented in
-- MySQL 5.1.

-- Note: The data will be corrupt. There was a change to the way location,
-- survey response, launch context, and Mobility data was stored involving the
-- timestamp, time, and timezone. The timestamp was dropped from each of these
-- and the time and timezone added if one or both did not previously exist. In
-- order to update the database, each of these objects will need to be removed
-- from the database, updated with a script, and reinserted back into the
-- database. 