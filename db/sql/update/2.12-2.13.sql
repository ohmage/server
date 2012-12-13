-- Add the new preferences.
INSERT INTO preference VALUES
    ('audit_log_location', '/opt/ohmage/logs/audits/'),
    ('fully_qualified_domain_name', 'http://localhost');
    
-- Update the observer_stream_data table.
ALTER TABLE observer_stream_data
    MODIFY COLUMN `time` bigint(20) DEFAULT NULL;
ALTER TABLE observer_stream_data
    MODIFY COLUMN `time_offset` bigint(20) DEFAULT NULL;
ALTER TABLE observer_stream_data 
    ADD COLUMN `time_adjusted` bigint(20) DEFAULT NULL
    AFTER `time_offset`;
ALTER TABLE observer_stream_data
    MODIFY COLUMN `data` longtext NOT NULL;
ALTER TABLE observer_stream_data 
    ADD INDEX `observer_stream_data_index_time` (time);
ALTER TABLE observer_stream_data 
    ADD INDEX `observer_stream_data_index_time_adjusted` (time_adjusted);
    
-- NOTE: After running this script, be sure to upgrade any Avro data to regular
-- JSON data. 
SELECT 'Be sure to upgrade the data in the observer_stream_data table if any Avro data exists.';

-- Add the survey and prompt ID lookup tables.
CREATE TABLE `campaign_survey_lookup` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `survey_id` varchar(255) NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_survey_lookup_index_survey_id` (`survey_id`),
  KEY `campaign_survey_lookup_fk_campaign_id` (`campaign_id`),
  CONSTRAINT `campaign_survey_lookup_fk_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES `campaign` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
CREATE TABLE `campaign_prompt_lookup` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `prompt_id` varchar(255) NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_prompt_lookup_index_prompt_id` (`prompt_id`),
  KEY `campaign_prompt_lookup_fk_campaign_id` (`campaign_id`),
  CONSTRAINT `campaign_prompt_lookup_fk_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES `campaign` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- NOTE: After running this script, be sure to populate the survey and prompt
-- ID lookup tables.
SELECT 'Be sure to populate the survey and prompt ID lookup tables.';