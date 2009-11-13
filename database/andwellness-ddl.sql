-- MySQL DDL statements for the AndWellness database
-- Version 0.9.0

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
  email_address varchar(320) CHARACTER SET latin1 COLLATE latin1_bin NOT NULL, -- cannot use UTF-8 here because the field will
                                                                               -- exceed the max number of characters that MySQL
                                                                               -- will index.
  json_data text,
  PRIMARY KEY (id),
  UNIQUE (email_address)
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
-- Prompt information. For now, just a label.
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
  time_stamp timestamp NOT NULL,
  epoch_millis bigint unsigned NOT NULL, 
  json_data text, 
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
  time_stamp timestamp NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  latitude decimal(14,12) NOT NULL,
  longitude decimal(15,12) NOT NULL,
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
  time_stamp timestamp NOT NULL,
  timezone varchar(9) NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  latitude decimal(14,12) NOT NULL,
  longitude decimal(15,12) NOT NULL,
  mode varchar(30) NOT NULL,
  
  -- TBC precision for these columns
  -- speed decimal(5,3) 
  -- variance decimal(5,3) 
  -- average decimal(5,3)
  -- fft varchar(100)
  
  PRIMARY KEY (id),
  UNIQUE INDEX idx_epoch_user (user_id, epoch_millis),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- 5 minute summary of mobility data from mobility_mode_only_entry_raw and
-- mobility_mode_feature_entry_raw
-- ---------------------------------------------------------------------------------
CREATE TABLE mobility_entry_five_min_summary (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  time_stamp timestamp NOT NULL,
  timezone varchar(9) NOT NULL,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------------------
-- Daily summary of mobility data from mobility_mode_only_entry_raw and
-- mobility_mode_feature_entry_raw
-- ---------------------------------------------------------------------------------
CREATE TABLE mobility_entry_daily_summary (
  id bigint unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  entry_date date NOT NULL,
  timezone varchar(9) NOT NULL,
  mode varchar(30) NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- TODO
-- SQL to initialize 
-- Split this file apart into separate components for each table.
