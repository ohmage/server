-- Always make sure we are using our own database.
USE ohmage;

-- Drop the procedure if it already existed.
DROP PROCEDURE IF EXISTS upgradeFrom2Dot14To2Dot15;

-- Set a dummy delimiter so that our scripts may be properly formed.
DELIMITER //

-- Create a procedure to do the things 
CREATE PROCEDURE upgradeFrom2Dot14To2Dot15 (OUT resultCode INT)
BEGIN
    -- Declare a handler that will catch all SQL exceptions.
    DECLARE exit HANDLER FOR sqlexception
    BEGIN
        -- Set the result code to a generic -1.
        SET resultCode = -1;
    END;
    
    -- Add the campaign mask tables.
    CREATE TABLE IF NOT EXISTS `campaign_mask` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `assigner_user_id` int(10) unsigned NOT NULL,
        `assignee_user_id` int(10) unsigned NOT NULL,
        `campaign_id` int(10) unsigned NOT NULL,`mask_id` varchar(36) NOT NULL,
        `creation_time` bigint(20) NOT NULL,
        PRIMARY KEY (`id`),
        INDEX `campaign_mask_index_mask_id` (`mask_id`),
        CONSTRAINT `campaign_mask_fk_assigner_user_id`
            FOREIGN KEY (`assigner_user_id`) REFERENCES `user` (`id`)
            ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT `campaign_mask_fk_assignee_user_id`
            FOREIGN KEY (`assignee_user_id`) REFERENCES `user` (`id`)
            ON DELETE CASCADE ON UPDATE CASCADE,
        CONSTRAINT `campaign_mask_fk_campaign_id`
            FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`)
            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
    -- Add the campaign mask survey ID table.
    CREATE TABLE IF NOT EXISTS `campaign_mask_survey_id` (
        `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
        `campaign_mask_id` int(10) unsigned NOT NULL,
        `survey_id` varchar(255) NOT NULL,
        PRIMARY KEY (`id`),
        UNIQUE KEY `campaing_mask_unique_mask_survey`
            (`campaign_mask_id`,`survey_id`),
        CONSTRAINT `campaign_mask_fk_survey_id`
            FOREIGN KEY (`campaign_mask_id`) REFERENCES `campaign_mask` (`id`)
            ON DELETE CASCADE ON UPDATE CASCADE
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
    
    -- Alter the audit table to include the new request_id column.
    IF (SELECT NOT EXISTS(
        SELECT * FROM INFORMATION_SCHEMA.COLUMNS
        WHERE TABLE_SCHEMA = 'ohmage'
        AND TABLE_NAME = 'audit'
        AND COLUMN_NAME = 'request_id'))
    THEN
        ALTER TABLE audit
            ADD COLUMN request_id
            VARCHAR(255) NOT NULL
            AFTER client;
    END IF;
    
    -- Set the result to 0.
    SET resultCode = 0;
END //

-- Reset the delimiter to its proper value.
DELIMITER ;

-- Call the procedure.
CALL upgradeFrom2Dot14To2Dot15(@upgradeResultCode);

-- Clean up.
DROP PROCEDURE upgradeFrom2Dot14To2Dot15;

-- Echo a result message.
SELECT @upgradeResultCode;