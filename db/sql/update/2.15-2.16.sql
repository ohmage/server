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
    
    -- Add the campaign mask table that includes both survey and prompt IDs.
    CREATE TABLE IF NOT EXISTS `campaign_mask_survey_prompt_map` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `campaign_mask_id` int(10) unsigned NOT NULL,
        `survey_id` varchar(255) NOT NULL,
        `prompt_id` varchar(255) NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `campaing_mask_unique_mask_survey_prompt`
            (`campaign_mask_id`,`survey_id`, `prompt_id`),
        CONSTRAINT `campaign_mask_fk_survey_prompt_map`
            FOREIGN KEY (`campaign_mask_id`) REFERENCES `campaign_mask` (`id`)
            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
    -- Add the table to store the invalid data.
    CREATE TABLE IF NOT EXISTS `observer_stream_data_invalid` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `user_id` int(10) unsigned NOT NULL,
        `observer_id` int(10) unsigned NOT NULL,
        `time_recorded` bigint(20) NOT NULL,
        `point_index` int(20) unsigned NOT NULL,
        `reason` text NOT NULL,
        `data` longtext NOT NULL,
        `last_modified_timestamp` timestamp NOT NULL
            DEFAULT CURRENT_TIMESTAMP
            ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (`id`),
        CONSTRAINT `observer_stream_data_invalid_fk_user_id`
            FOREIGN KEY (`user_id`)
            REFERENCES `user` (`id`)
            ON DELETE CASCADE
            ON UPDATE CASCADE,
        CONSTRAINT `observer_stream_data_invalid_fk_observer_id`
          FOREIGN KEY (`observer_id`)
          REFERENCES `observer` (`id`)
          ON DELETE CASCADE
          ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

    -- Add the 'processed' flag to the URL-based resource table.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'url_based_resource'
        AND COLUMN_NAME = 'processed'))
    THEN
        ALTER TABLE url_based_resource
            ADD COLUMN `processed`
            BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    -- Add the 'processed' flag to the URL-based resource table.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'url_based_resource'
        AND COLUMN_NAME = 'processed'))
    THEN
        ALTER TABLE url_based_resource
            ADD COLUMN `processed`
            BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;

    -- Add the index for the observer stream data.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'observer_stream_data'
        AND INDEX_NAME = 'observer_stream_data_query'))
    THEN
        CREATE INDEX `observer_stream_data_query`
            ON observer_stream_data
            (`user_id`,`observer_stream_link_id`,`time_adjusted`,`time`);
    END IF;

    -- Add the index for the Mobility dates query.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.STATISTICS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'observer_stream_data'
        AND INDEX_NAME = 'observer_stream_data_index_link_user_adjusted'))
    THEN
        CREATE INDEX `observer_stream_data_index_link_user_adjusted`
            ON observer_stream_data
            (`observer_stream_link_id`, `user_id`, `time_adjusted`);
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