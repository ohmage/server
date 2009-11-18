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

-- ---------------------------------------------------------------------
-- System users.
-- ---------------------------------------------------------------------
CREATE TABLE user (
  id smallint(6) unsigned NOT NULL auto_increment,
  email_address varchar(320)
  json_data text,
  PRIMARY KEY (id),
  -- we will have to check the uniqueness of new email addresses within the application itself because 
  -- the length of the UTF-8 encoded email address exceeds the maximum size for indexing in MySQL.
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
-- Prompt information. For now, just an id and a label. Later we can 
-- use this table to fully define prompts (limits, response data types, 
-- etc).
-- --------------------------------------------------------------------
CREATE TABLE prompt (
  id smallint unsigned NOT NULL auto_increment, 
  name tinytext CHARACTER SET latin1 COLLATE latin1_bin NOT NULL, -- internally used name, therefore latin1 encoding
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Store prompt responses.
-- --------------------------------------------------------------------
CREATE TABLE prompt_response (
  id integer unsigned NOT NULL auto_increment,
  prompt_id smallint unsigned NOT NULL,
  user_id smallint(6) unsigned NOT NULL,
  campaign_id smallint(4) unsigned NOT NULL,
  parent_prompt_id smallint(4) unsigned,
  group_id bigint unsigned NOT NULL,
  utc_time_stamp timestamp NOT NULL,
  utc_epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar (32) NOT NULL,
  latitude double NOT NULL,
  longitude double NOT NULL,
  json_data text NOT NULL, 
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (prompt_id) REFERENCES prompt (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link prompts to campaigns.
-- --------------------------------------------------------------------
CREATE TABLE prompt_campaign (
  id integer unsigned NOT NULL auto_increment,
  prompt_id smallint unsigned NOT NULL,
  campaign_id smallint(4) unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (prompt_id) REFERENCES prompt (id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
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
  latitude double NOT NULL,
  longitude double NOT NULL,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE idx_epoch_user (user_id, epoch_millis),
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
  latitude double NOT NULL,
  longitude double NOT NULL,
  mode varchar(30) NOT NULL,
  speed double NOT NULL,
  variance double NOT NULL,
  average double NOT NULL,
  fft varchar(300) NOT NULL, -- A comma separated list of 10 FFT floating-point values. The reason the array is not unpacked  
                             -- into separate columns is because the data will not be used outside of a debugging scenario.
                             -- It is simply stored the way it is sent by the phone (minus the JSON array brackets). 
  PRIMARY KEY (id),
  UNIQUE INDEX idx_epoch_user (user_id, epoch_millis),
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

-- TODO
-- SQL to initialize
