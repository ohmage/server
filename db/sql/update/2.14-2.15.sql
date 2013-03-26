-- Add the campaign mask tables.
CREATE TABLE `campaign_mask` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assigner_user_id` int(10) unsigned NOT NULL,
  `assignee_user_id` int(10) unsigned NOT NULL,
  -- Class ID?
  `campaign_id` int(10) unsigned NOT NULL,
  `mask_id` varchar(36) NOT NULL,
  `creation_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `campaign_mask_index_mask_id` (`mask_id`),
  CONSTRAINT `campaign_mask_fk_assigner_user_id` FOREIGN KEY (`assigner_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaign_mask_fk_assignee_user_id` FOREIGN KEY (`assignee_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaign_mask_fk_campaign_id` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaign_mask_survey_id` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `campaign_mask_id` int(10) unsigned NOT NULL,
  `survey_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `campaing_mask_unique_mask_survey` (`campaign_mask_id`,`survey_id`),
  CONSTRAINT `campaign_mask_fk_survey_id` FOREIGN KEY (`campaign_mask_id`) REFERENCES `campaign_mask` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Add the request_id column to the audit table.
ALTER TABLE audit ADD COLUMN request_id VARCHAR(255) NOT NULL AFTER client;