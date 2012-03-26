-- Move the Email address field to the user table.
ALTER TABLE user ADD COLUMN email_address VARCHAR(320);
UPDATE user, user_personal SET user.email_address = user_personal.email_address WHERE user.id = user_personal.user_id;
ALTER TABLE user_personal DROP COLUMN email_address;

-- Drop the JSON data column from the personal information.
ALTER TABLE user_personal DROP COLUMN json_data;

-- Create the public class.
INSERT INTO class(urn, name, description)
    VALUES ('urn:class:public', 'Public Class', 'This is the public class for all self-registered users.');

-- Create the table responsible for user registration.
CREATE TABLE user_registration(
    -- A unique key for each request.
    id int unsigned NOT NULL auto_increment,
    -- A reference to the user.
    user_id int unsigned NOT NULL,
    -- The registration ID.
    registration_id VARCHAR(128) NOT NULL,
    -- The time at which the registration request was made.
    request_timestamp BIGINT UNSIGNED NOT NULL,
    -- The time at which the registration was accepted.
    accepted_timestamp BIGINT UNSIGNED DEFAULT NULL,
    -- The ID is the primary key.
    PRIMARY KEY (id),
    -- Guarantees that multiple requests for the same user cannot exist.
    UNIQUE (user_id),
    -- Link the user table.
    CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- This is being self-referenced now and doesn't need to be a preference in the
-- database.
DELETE FROM preference WHERE p_key='properties_file';

-- Add the public and private ReCaptcha keys as well as the flag regarding 
-- whether or not self-registration is allowed.
INSERT INTO preference VALUES 
    ('recaptcha_public_key', ''),
    ('recaptcha_private_key', ''),
    ('self_registration_allowed', 'false');
    
-- The key in the classifier JSON for Mobility points for the N95 variance has
-- been changed from "N95Variance" to "n95variance".
UPDATE mobility_extended SET features=REPLACE(features, 'N95Variance', 'n95variance');
