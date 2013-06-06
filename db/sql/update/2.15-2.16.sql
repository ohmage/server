-- Always make sure we are using our own database.
USE ohmage;

-- Drop the procedure if it already existed.
DROP PROCEDURE IF EXISTS upgradeFrom2Dot15To2Dot16;

-- Set a dummy delimiter so that our scripts may be properly formed.
DELIMITER //

-- Create a procedure to do the things 
CREATE PROCEDURE upgradeFrom2Dot15To2Dot16 (OUT resultCode INT)
BEGIN
    -- Declare a handler that will catch all SQL exceptions.
    DECLARE exit HANDLER FOR sqlexception
    BEGIN
        -- Set the result code to a generic -1.
        SET resultCode = -1;
    END;

	-- Add the Open mHealth table for storing authentication and authorization
	-- information.
	CREATE TABLE IF NOT EXISTS `omh_authentication` (
	  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
	  `domain` varchar(100) NOT NULL,
	  `auth_key` varchar(100) NOT NULL,
	  `auth_value` varchar(100) NOT NULL,
	  PRIMARY KEY (`id`),
	  UNIQUE KEY `omh_authentication_unique_domain_key` (`domain`,`auth_key`),
	  KEY `omh_authentication_index_domain` (`domain`)
	) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
    -- Add a default audio directory.
    IF (SELECT NOT EXISTS(
        SELECT * FROM preference
        WHERE p_key = 'audio_directory'))
    THEN
		INSERT INTO preference(p_key, p_value) VALUES
		    ('audio_directory', '/opt/ohmage/userdata/audio');
    END IF;
    
    -- Add the class creation privilege.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'user'
        AND COLUMN_NAME = 'class_creation_privilege'))
    THEN
        ALTER TABLE user
            ADD COLUMN `class_creation_privilege`
            BOOLEAN NOT NULL DEFAULT FALSE
            AFTER `campaign_creation_privilege`;
    END IF;

    -- Add the user setup privilege.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'user'
        AND COLUMN_NAME = 'user_setup_privilege'))
    THEN
		ALTER TABLE user
		    ADD COLUMN `user_setup_privilege`
		    BOOLEAN NOT NULL DEFAULT FALSE
		    AFTER `class_creation_privilege`;
    END IF;
    
	-- Add the character encoding to survey responses.
	-- No check is necessary because, if these columns do not exist, there is a
	-- serious issue. If this is already the definition of the column, there is
	-- no harm in redefining it.
	ALTER TABLE survey_response
	    MODIFY COLUMN survey text CHARACTER SET utf8 NOT NULL;
	ALTER TABLE prompt_response
	    MODIFY COLUMN response text CHARACTER SET utf8 NOT NULL;

    -- Add the SSL flag.
    IF (SELECT NOT EXISTS(
        SELECT * FROM preference
        WHERE p_key = 'ssl_enabled'))
    THEN
        INSERT INTO preference VALUES ('ssl_enabled', 'false');
    END IF;

    -- Set the result to 0.
    SET resultCode = 0;
END //

-- Reset the delimiter to its proper value.
DELIMITER ;

-- Call the procedure.
CALL upgradeFrom2Dot15To2Dot16(@upgradeResultCode);

-- Clean up.
DROP PROCEDURE upgradeFrom2Dot15To2Dot16;

-- Echo a result message.
SELECT @upgradeResultCode;