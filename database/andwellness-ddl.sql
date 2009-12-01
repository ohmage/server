-- MySQL DDL statements for the AndWellness database
-- Version 0.9.0
-- As the number of tables in the database grows, it would be a good idea 
-- to split this file up separate files per table.

CREATE DATABASE andwellness CHARACTER SET utf8 COLLATE utf8_general_ci;
USE andwellness;

-- --------------------------------------------------------------------
-- Stores a bundle of properties in JSON format.
-- --------------------------------------------------------------------
CREATE TABLE configuration (
  id smallint(4) unsigned NOT NULL auto_increment,
  json_data text,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Campaign properties.
-- --------------------------------------------------------------------
CREATE TABLE campaign (
  id smallint(4) unsigned NOT NULL auto_increment,
  name varchar(125) NOT NULL,
  label varchar(250) default NULL,
  subdomain varchar(125) default NULL,
  configuration_id smallint(4) unsigned default NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (subdomain),
  CONSTRAINT FOREIGN KEY (configuration_id) REFERENCES configuration (id) 
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------
-- System users.
-- TODO add a password column for future release with real authentication.
-- -----------------------------------------------------------------------
CREATE TABLE user (
  id smallint(6) unsigned NOT NULL auto_increment,
  login_id varchar(15) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------
-- Due to IRB standards, we store personally identifying information
-- separately from the user's login credentials.
-- ---------------------------------------------------------------------
CREATE TABLE user_personal (
  id smallint(6) unsigned NOT NULL auto_increment,
  email_address varchar(320),
  json_data text,
  PRIMARY KEY (id)
  -- we will have to check the uniqueness of new email addresses within the application itself because 
  -- the length of the UTF-8 encoded email address exceeds the maximum size for indexing in MySQL.
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


-- ---------------------------------------------------------------------
-- Link user to user_personal. This is a one-to-one mapping managed by
-- the application i.e., a user cannot have more than one user_personal
-- entry.
-- ---------------------------------------------------------------------
CREATE TABLE user_user_personal (
	user_id smallint(6) unsigned NOT NULL,
	user_personal_id smallint(6) unsigned NOT NULL,
	PRIMARY KEY (user_id, user_personal_id),
	CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id),
	CONSTRAINT FOREIGN KEY (user_personal_id) REFERENCES user_personal (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- User roles.
-- --------------------------------------------------------------------
CREATE TABLE user_role (
  id tinyint(1) unsigned NOT NULL auto_increment,
  label tinytext NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Bind users to roles and campaigns.
-- --------------------------------------------------------------------
CREATE TABLE user_role_campaign (
  id smallint(6) unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  campaign_id smallint(4) unsigned NOT NULL,
  user_role_id tinyint(1) unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_role_id) REFERENCES user_role (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Prompts may be grouped within a semantic space defined by the group 
-- name.
-- --------------------------------------------------------------------
CREATE TABLE campaign_prompt_group ( 
  campaign_id smallint(4) unsigned NOT NULL, 
  group_id smallint(4) unsigned NOT NULL,
  group_name tinytext NOT NULL,
  PRIMARY KEY (campaign_id, group_id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Prompt information. For now, we statically load prompts on both
-- client and server. Prompts are versioned within campaigns. Prompts
-- belong to groups. Prompts may be bound to each other through the
-- parent_id.
-- --------------------------------------------------------------------
CREATE TABLE prompt (
  campaign_id smallint(4) unsigned NOT NULL,
  version_id smallint(4) unsigned NOT NULL,
  group_id smallint(4) unsigned NOT NULL,
  id smallint(4) unsigned NOT NULL, 
  parent_id smallint(4) unsigned,
  p_text tinytext NOT NULL, 
  PRIMARY KEY (campaign_id, version_id, group_id, id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id),
  CONSTRAINT FOREIGN KEY (campaign_id, group_id) REFERENCES campaign_prompt_group (campaign_id, group_id),
  CONSTRAINT FOREIGN KEY (campaign_id, version_id, group_id, parent_id) REFERENCES prompt (campaign_id, version_id, group_id, id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Possible future tables
-- campaign_prompt_version: look-up table for versions within campaigns
-- prompt_response_config: store information about how to validate prompt responses: limits, datatypes

-- ---------------------------------------------------------------------
-- Store prompt responses.
-- --------------------------------------------------------------------
 CREATE TABLE prompt_response (
  id integer unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  campaign_id smallint(4) unsigned NOT NULL,
  version_id smallint(4) unsigned NOT NULL,
  group_id smallint(4) unsigned NOT NULL,
  prompt_id smallint unsigned NOT NULL,
  utc_time_stamp timestamp NOT NULL,
  utc_epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar (32) NOT NULL,
  latitude double,
  longitude double,
  json_data text NOT NULL, -- the structure of the json_data is dependent on the prompt_type
  PRIMARY KEY (id),
  INDEX (user_id),
  INDEX (user_id, campaign_id, version_id, group_id),
  CONSTRAINT FOREIGN KEY (campaign_id, version_id, group_id, prompt_id) REFERENCES prompt (campaign_id, version_id, group_id, id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------------------------------------------------------
-- Tags add semantic structure to a set of prompts. For the first
-- rollout, our only tag is the group name (found in the campaign_prompt_group
-- table). Tags are not stored with the JSON (1) in order to facilitate easier
-- querying via tags and (2) because tags are used across all types of prompt
-- responses so they have a natural structure.
-- ----------------------------------------------------------------------------
 CREATE TABLE campaign_prompt_repsonse_tag (
    id integer unsigned NOT NULL auto_increment, 
	prompt_response_id integer unsigned NOT NULL,
	tag_name varchar(250) NOT NULL,
--	tag_value varchar(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (prompt_response_id) REFERENCES prompt_response (id)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -------------------------------------------------------
-- Add semantic structure to prompt responses as a group.
-- -------------------------------------------------------
 CREATE TABLE campaign_prompt_group_tag (
    id integer unsigned NOT NULL auto_increment,
	campaign_id smallint(4) unsigned NOT NULL,
    version_id smallint(4) unsigned NOT NULL,
    group_id smallint(4) unsigned NOT NULL,
    user_id smallint(6) unsigned NOT NULL,
	tag_name varchar(250) NOT NULL,
--	tag_value varchar(500) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (user_id, campaign_id, version_id, group_id) REFERENCES prompt_response (user_id, campaign_id, version_id, group_id)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------
-- Link group tags to actual prompt responses. This table is
-- here to avoid storing the tag name and value for every prompt 
-- response in a group.
-- ---------------------------------------------------------------
 CREATE TABLE campaign_prompt_group_repsonse_tag (
    id integer unsigned NOT NULL auto_increment,
    prompt_response_id integer unsigned NOT NULL,
    group_response_tag_id integer unsigned NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (prompt_response_id) REFERENCES prompt_response (id),
    CONSTRAINT FOREIGN KEY (group_response_tag_id) REFERENCES campaign_prompt_group_tag (id)
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode only" mobility data. Mobility data is *not*
-- linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_mode_only_entry (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  utc_time_stamp timestamp NOT NULL,
  utc_epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  latitude double,
  longitude double,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id, utc_epoch_millis), -- enforce no-duplicates rule at the table level
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode + features" mobility data. Mobility data is 
-- *not* linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_mode_features_entry (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  utc_time_stamp timestamp NOT NULL,
  utc_epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  latitude double,
  longitude double,
  mode varchar(30) NOT NULL,
  speed double NOT NULL,
  variance double NOT NULL,
  average double NOT NULL,
  fft varchar(300) NOT NULL, -- A comma separated list of 10 FFT floating-point values. The reason the array is not unpacked  
                             -- into separate columns is because the data will not be used outside of a debugging scenario.
                             -- It is simply stored the way it is sent by the phone (minus the JSON array brackets). 
  PRIMARY KEY (id),
  UNIQUE INDEX (user_id, utc_epoch_millis),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- 5 minute summary of mobility data from mobility_mode_only_entry and
-- mobility_mode_features_entry
-- ---------------------------------------------------------------------------------
CREATE TABLE mobility_entry_five_min_summary (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  utc_time_stamp timestamp NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- Daily summary of mobility data from mobility_mode_only_entry and
-- mobility_mode_features_entry
-- ---------------------------------------------------------------------------------
CREATE TABLE mobility_entry_daily_summary (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  entry_date date NOT NULL,
  mode varchar(30) NOT NULL,
  duration smallint (5) unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

