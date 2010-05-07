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
  configuration_id smallint(4) unsigned default NULL,
  PRIMARY KEY (id),
  UNIQUE KEY (subdomain),
  UNIQUE KEY (name),
  CONSTRAINT FOREIGN KEY (configuration_id) REFERENCES configuration (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------
-- System users.
-- -----------------------------------------------------------------------
CREATE TABLE user (
  id smallint(6) unsigned NOT NULL auto_increment,
  login_id varchar(15) NOT NULL,
  password varchar(100) NOT NULL,
  enabled bit NOT NULL,
  new_account bit NOT NULL,
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
	CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (user_personal_id) REFERENCES user_personal (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- User roles.
-- --------------------------------------------------------------------
CREATE TABLE user_role (
  id tinyint(1) unsigned NOT NULL auto_increment,
  label varchar(50) NOT NULL,
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
  CONSTRAINT FOREIGN KEY (user_role_id) REFERENCES user_role (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- The set of prompts for a campaign is versioned.
-- --------------------------------------------------------------------
CREATE TABLE campaign_prompt_version (
  id smallint(4) unsigned NOT NULL auto_increment,
  campaign_id smallint(4) unsigned NOT NULL,
  version_id smallint(4) unsigned NOT NULL,     -- static id shared with phone configuration
  PRIMARY KEY (id),
  UNIQUE (campaign_id, version_id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------------------------------------------------
-- Prompts may be grouped within a semantic space defined by the group 
-- name. Groups have a version.
-- ----------------------------------------------------------------------
CREATE TABLE campaign_prompt_group ( 
  id smallint(4) unsigned NOT NULL auto_increment,
  campaign_id smallint(4) unsigned NOT NULL, 
  campaign_prompt_version_id smallint(4) unsigned NOT NULL,
  group_id smallint(4) unsigned NOT NULL,     -- static id shared with phone configuration
  group_name varchar(100) NOT NULL,           -- static name shared with phone configuration
  PRIMARY KEY (id),
  UNIQUE (campaign_id, campaign_prompt_version_id, group_id),
  UNIQUE (campaign_id, campaign_prompt_version_id, group_name),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_prompt_version_id) REFERENCES campaign_prompt_version (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Prompts have data types and restrictions on the data that are
-- defined using this table.
-- --------------------------------------------------------------------
CREATE TABLE prompt_type (
  id smallint(4) unsigned NOT NULL auto_increment,
  type tinytext NOT NULL,
  restriction text,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Prompt information. For now, we statically load prompts on both
-- client and server. Prompts are versioned within campaigns. Prompts
-- belong to groups. Prompts may be bound to each other through the
-- parent_config_id.
-- --------------------------------------------------------------------
CREATE TABLE prompt (
  id smallint(4) unsigned NOT NULL auto_increment,
  prompt_type_id smallint(4) unsigned NOT NULL,
  campaign_prompt_group_id smallint(4) unsigned NOT NULL, 
  campaign_prompt_version_id smallint(4) unsigned NOT NULL,
  prompt_config_id smallint(4) unsigned NOT NULL, -- static id shared with phone configuration
  parent_config_id smallint(4) unsigned,          -- static id shared with phone configuration
  question_text tinytext NOT NULL, 
  legend_text tinytext NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (campaign_prompt_group_id, campaign_prompt_version_id, prompt_config_id),
  UNIQUE (legend_text(255)),
  CONSTRAINT FOREIGN KEY (prompt_type_id) REFERENCES prompt_type (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_prompt_group_id) REFERENCES campaign_prompt_group (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_prompt_version_id) REFERENCES campaign_prompt_version (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_prompt_group_id, campaign_prompt_version_id, parent_config_id) REFERENCES prompt (campaign_prompt_group_id, campaign_prompt_version_id, prompt_config_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------------
-- Store prompt responses.
-- ----------------------------------
 CREATE TABLE prompt_response (
  id integer unsigned NOT NULL auto_increment,
  prompt_id smallint(4) unsigned NOT NULL,
  user_id smallint(6) unsigned NOT NULL,
  time_stamp timestamp NOT NULL,
  epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar (32) NOT NULL,
  latitude double,
  longitude double,
  json_data text NOT NULL, -- the structure of the json_data is dependent on the prompt_type
  PRIMARY KEY (id),
  INDEX (user_id),
  UNIQUE (user_id, prompt_id, epoch_millis, json_data(25)), -- the number 25 is not arbitrary; it is the size of the longest JSON response we currently have
  CONSTRAINT FOREIGN KEY (prompt_id) REFERENCES prompt (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -------------------------------------------------------
-- Store arbitrary tags.
-- -------------------------------------------------------
 CREATE TABLE tag (
  id integer unsigned NOT NULL auto_increment,
  tag_name tinytext NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (tag_name(255))
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
 -- ----------------------------------------------------------------------------
-- Link a prompt response to a tag. Tags are not stored with the JSON in 
-- order to facilitate easier querying via tags and because tags are used 
-- across all types of prompt responses so they have a natural structure.
-- ----------------------------------------------------------------------------
 CREATE TABLE prompt_response_tag (
  id integer unsigned NOT NULL auto_increment, 
  prompt_response_id integer unsigned NOT NULL,
  tag_id integer unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (prompt_response_id) REFERENCES prompt_response (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------
-- Link a tag to a prompt group.
-- -----------------------------------
 CREATE TABLE prompt_group_tag (
  id integer unsigned NOT NULL auto_increment,
  campaign_prompt_group_id smallint(4) unsigned NOT NULL,
  tag_id integer unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (campaign_prompt_group_id) REFERENCES campaign_prompt_group (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (tag_id) REFERENCES tag (id) ON DELETE CASCADE ON UPDATE CASCADE
 ) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ----------------------------------------------
-- Link group tags to actual prompt responses. 
-- ----------------------------------------------
CREATE TABLE prompt_group_response_tag (
  id integer unsigned NOT NULL auto_increment,
  prompt_response_id integer unsigned NOT NULL,
  group_tag_id integer unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (prompt_response_id) REFERENCES prompt_response (id),
  CONSTRAINT FOREIGN KEY (group_tag_id) REFERENCES prompt_group_tag (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode only" mobility data. Mobility data is *not*
-- linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_mode_only_entry (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  time_stamp timestamp NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  latitude double,
  longitude double,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id, epoch_millis), -- enforce no-duplicates rule at the table level
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode + features" mobility data. Mobility data is 
-- *not* linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_mode_features_entry (
  id integer unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  time_stamp timestamp NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  latitude double,
  longitude double,
  mode varchar(30) NOT NULL,
  speed double NOT NULL,
  variance double NOT NULL,
  average double NOT NULL,
  fft varchar(300) NOT NULL, -- A comma separated list of 10 FFT floating-point values. The reason the array is not unpacked  
                             -- into separate columns is because the data will not be used outside of a debugging scenario.
                             -- It is simply stored the way it is sent by the phone (as a JSON array). 
  PRIMARY KEY (id),
  UNIQUE INDEX (user_id, epoch_millis),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- 5 minute summary of mobility data from mobility_mode_only_entry and
-- mobility_mode_features_entry
-- ---------------------------------------------------------------------------------
CREATE TABLE mobility_entry_five_min_summary (
  id integer unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  time_stamp timestamp NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX (user_id,time_stamp, mode),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- Daily summary of mobility data from mobility_mode_only_entry and
-- mobility_mode_features_entry
-- ---------------------------------------------------------------------------------
CREATE TABLE mobility_entry_daily_summary (
  id integer unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  entry_date date NOT NULL,
  mode varchar(30) NOT NULL,
  duration smallint (5) unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX (user_id, entry_date),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- Weekly summary of prompt responses for visualization dashboards
-- ---------------------------------------------------------------------------------
CREATE TABLE prompt_response_weekly_summary (
  id integer unsigned NOT NULL auto_increment,
  prompt_id smallint(4) unsigned NOT NULL, 
  user_id smallint(6) unsigned NOT NULL,
  week_start date NOT NULL,
  json_data text NOT NULL,
  PRIMARY KEY (id),
  UNIQUE INDEX (user_id, prompt_id, week_start),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (prompt_id) REFERENCES prompt(id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

