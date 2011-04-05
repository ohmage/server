-- MySQL DDL statements for the AndWellness database

CREATE DATABASE andwellness CHARACTER SET utf8 COLLATE utf8_general_ci;
USE andwellness;

-- --------------------------------------------------------------------
-- The class concept comes from Mobilize, but it can be used for any
-- taxonomical grouping of users. 
-- --------------------------------------------------------------------
CREATE TABLE class (
  id int unsigned NOT NULL auto_increment,
  urn varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (urn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- A campaign and its associated XML configuration.
-- --------------------------------------------------------------------
CREATE TABLE campaign (
  id int unsigned NOT NULL auto_increment,
  urn varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  description text,
  xml mediumtext NOT NULL,
  running_state varchar(50) NOT NULL,
  privacy_state varchar(50) NOT NULL,
  creation_timestamp datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (urn)
-- create an index on name?
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Bind campaigns to classes.
-- --------------------------------------------------------------------
CREATE TABLE campaign_class (
  id int unsigned NOT NULL auto_increment,
  campaign_id int unsigned NOT NULL,
  class_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (campaign_id, class_id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- -----------------------------------------------------------------------
-- System users.
-- -----------------------------------------------------------------------
CREATE TABLE user (
  id int unsigned NOT NULL auto_increment,
  login_id varchar(15) NOT NULL,
  password varchar(100) NOT NULL,
  enabled bit NOT NULL,
  new_account bit NOT NULL
  campaign_creation_privilege bit NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (login_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------
-- Due to IRB standards, we store personally identifying information
-- separately from the user's login credentials. ** This table is currently
-- unused, but it is kept around in order to avoid changing the command
-- line registration process. **
-- ---------------------------------------------------------------------
CREATE TABLE user_personal (
  id int unsigned NOT NULL auto_increment,
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
	user_id int unsigned NOT NULL,
	user_personal_id int unsigned NOT NULL,
	PRIMARY KEY (user_id, user_personal_id),
	CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
	CONSTRAINT FOREIGN KEY (user_personal_id) REFERENCES user_personal (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- User role lookup table.
-- --------------------------------------------------------------------
CREATE TABLE user_role (
  id tinyint unsigned NOT NULL auto_increment,
  role varchar(50) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Bind users to roles and campaigns. A user can have a different role
-- for each campaign they belong to.
-- --------------------------------------------------------------------
CREATE TABLE user_role_campaign (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  campaign_id int unsigned NOT NULL,
  user_role_id tinyint unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_role_id) REFERENCES user_role (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Bind users to classes. 
-- --------------------------------------------------------------------

CREATE TABLE user_class (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  class_id int unsigned NOT NULL,
  class_role varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id, class_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Stores survey responses for a user in a campaign 
-- --------------------------------------------------------------------
CREATE TABLE survey_response (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  campaign_id int unsigned NOT NULL,
  client varchar(250) NOT NULL,
  msg_timestamp datetime NOT NULL,
  epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar(32) NOT NULL,
  survey_id varchar(250) NOT NULL,    -- a survey id as defined in a configuration at the XPath //surveyId
  survey text NOT NULL,               -- the max length for text is 21843 UTF-8 chars
  launch_context text,                -- trigger and other data
  location_status tinytext NOT NULL,  -- one of: unavailable, valid, stale, inaccurate 
  location text,                      -- JSON location data: longitude, latitude, accuracy, provider
  upload_timestamp datetime NOT NULL, -- the upload time based on the server time and timezone  
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  
  PRIMARY KEY (id),
  INDEX (user_id, campaign_id),
  INDEX (user_id, upload_timestamp),
  UNIQUE (user_id, survey_id, epoch_millis), -- handle duplicate survey uploads
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,    
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Stores individual prompt responses for a user in a campaign. Both
-- the entire survey response and each prompt response in for a survey
-- are stored.
-- --------------------------------------------------------------------
CREATE TABLE prompt_response (
  id int unsigned NOT NULL auto_increment,
  survey_response_id int unsigned NOT NULL,
  prompt_id varchar(250) NOT NULL,  -- a prompt id as defined in a configuration at the XPath //promptId
  prompt_type varchar(250) NOT NULL, -- a prompt type as defined in a configuration at the XPath //promptType
  repeatable_set_id varchar(250), -- a repeatable set id as defined in a configuration at the XPath //repeatableSetId
  repeatable_set_iteration tinyint unsigned,
  response text NOT NULL,   -- the data format is defined by the prompt type: a string or a JSON string
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  privacy_state varchar(50) NOT NULL,
  
  PRIMARY KEY (id),
  INDEX (survey_response_id),
  INDEX (prompt_id),
  -- uniqueness of survey uploads is handled by the survey_response table
  
  CONSTRAINT FOREIGN KEY (survey_response_id) REFERENCES survey_response (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Points a UUID to a URL of a media resource (such as an image). The  
-- UUID is an implicit link into the prompt_response table.
-- --------------------------------------------------------------------
CREATE TABLE url_based_resource (
    id int unsigned NOT NULL auto_increment,
    user_id int unsigned NOT NULL,
    client varchar(250) NOT NULL,
    uuid char (36) NOT NULL, -- joined with prompt_response.response to retrieve survey context for an item
    url text,
    audit_timestamp timestamp default current_timestamp on update current_timestamp,
    
    UNIQUE (uuid), -- disallow duplicates and index on UUID
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode only" mobility data. Mobility data is *not*
-- linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_mode_only (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  client tinytext NOT NULL,
  msg_timestamp datetime NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  location_status tinytext NOT NULL,
  location text,
  mode varchar(30) NOT NULL,
  upload_timestamp datetime NOT NULL, -- the upload time based on the server time and timezone
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  
  PRIMARY KEY (id),
  INDEX (user_id, msg_timestamp),
  UNIQUE (user_id, epoch_millis), -- enforce no-duplicates rule at the table level
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode + sensor data" mobility data. Mobility data is 
-- *not* linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_extended (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  client tinytext NOT NULL,
  msg_timestamp datetime NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  location_status tinytext NOT NULL,
  location text,
  sensor_data text NOT NULL,
  features text NOT NULL,
  classifier_version tinytext NOT NULL, 
  mode varchar(30) NOT NULL,
  upload_timestamp datetime NOT NULL, -- the upload time based on the server time and timezone
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  
  PRIMARY KEY (id),
  INDEX (user_id, msg_timestamp),
  UNIQUE INDEX (user_id, epoch_millis),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
