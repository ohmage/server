-- Create the campaign to survey ID lookup table.
CREATE TABLE `campaign_survey_lookup` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `survey_id` varchar(255) NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_survey_lookup_index_survey_id` (`survey_id`) USING HASH,
  KEY `campaign_survey_lookup_fk_campaign_id` (`campaign_id`),
  CONSTRAINT `campaign_survey_lookup_fk_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES campaign (id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;

-- Create the campaign to prompt ID lookup table.
CREATE TABLE `campaign_prompt_lookup` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `prompt_id` varchar(255) NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_prompt_lookup_index_prompt_id` (`prompt_id`) USING HASH,
  KEY `campaign_prompt_lookup_fk_campaign_id` (`campaign_id`),
  CONSTRAINT `campaign_prompt_lookup_fk_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES campaign (id)
    ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=UTF8;