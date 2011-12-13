-- MySQL DDL statements for the AndWellness database

CREATE DATABASE andwellness CHARACTER SET utf8 COLLATE utf8_general_ci;
USE andwellness;

-- --------------------------------------------------------------------
-- A lookup table for the types of HTTP requests. 
-- --------------------------------------------------------------------
CREATE TABLE audit_request_type (
  -- A unique key to reference the different types.
  id int unsigned NOT NULL auto_increment,
  -- The value for the HTTP request types.
  request_type varchar(8) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- A table to audit all of the transactions with the server.
-- --------------------------------------------------------------------
CREATE TABLE audit (
  -- A unique key for each request.
  id int unsigned NOT NULL auto_increment,
  -- The type of request this was, GET, POST, etc.
  request_type_id int unsigned NOT NULL,
  -- The URI portion of the URL for the request. There will always be a URI.
  uri text NOT NULL,
  -- The client parameter. If it is missing, it will be null.
  client text,
  -- A specific identifier for devices.
  device_id text,
  -- The response we sent back to the client. If the request failed, we will
  -- record the entire failure message. If the request succeeded, we will only
  -- record "success".
  response text NOT NULL,
  -- A timestamp as recorded by a Calendar as soon as the request arrived.
  received_millis long NOT NULL,
  -- A timestamp as recorded by a Calendar as soon as the request was 
  -- completely responded to.
  respond_millis long NOT NULL,
  -- A database timestamp about the time at which this record was made.
  db_timestamp timestamp default current_timestamp NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (request_type_id) REFERENCES audit_request_type (id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- A mapping of extra keys to their values taken from the HTTP request
-- headers.
-- --------------------------------------------------------------------
CREATE TABLE audit_extra (
  -- A unique key for this parameter mapping.
  id int unsigned NOT NULL auto_increment,
  -- A reference to the audit to which this parameter was a member.
  audit_id int unsigned NOT NULL,
  -- The parameter's key.
  extra_key text NOT NULL,
  -- The parameter's value.
  extra_value text NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (audit_id) REFERENCES audit (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- A mapping of parameter keys to their values linked to a request. 
-- --------------------------------------------------------------------
CREATE TABLE audit_parameter (
  -- A unique key for this parameter mapping.
  id int unsigned NOT NULL auto_increment,
  -- A reference to the audit to which this parameter was a member.
  audit_id int unsigned NOT NULL,
  -- The parameter's key.
  param_key text NOT NULL,
  -- The parameter's value.
  param_value text NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (audit_id) REFERENCES audit (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- The class concept comes from Mobilize, but it can be used for any
-- taxonomical grouping of users. 
-- --------------------------------------------------------------------
CREATE TABLE class (
  id int unsigned NOT NULL auto_increment,
  urn varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  description text,
  PRIMARY KEY (id),
  UNIQUE (urn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Lookup table for the running states of a campaign.
-- --------------------------------------------------------------------
CREATE TABLE campaign_running_state (
  id int unsigned NOT NULL auto_increment,
  running_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (running_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Lookup table for the privacy states of a campaign.
-- --------------------------------------------------------------------
CREATE TABLE campaign_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
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
  running_state_id int unsigned NOT NULL,
  privacy_state_id int unsigned NOT NULL,
  creation_timestamp datetime NOT NULL,
  icon_url varchar(255),
  authored_by varchar(255),
  PRIMARY KEY (id),
  UNIQUE (urn),
  CONSTRAINT FOREIGN KEY (running_state_id) REFERENCES campaign_running_state (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES campaign_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
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
  username varchar(25) NOT NULL,
  password varchar(60) NOT NULL,
  enabled bit NOT NULL,
  new_account bit NOT NULL,
  campaign_creation_privilege bit NOT NULL,
  admin bit NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- ---------------------------------------------------------------------
-- Due to IRB standards, we store personally identifying information
-- separately from the user's login credentials.
-- ---------------------------------------------------------------------
CREATE TABLE user_personal (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  organization varchar(255) NOT NULL,
  personal_id varchar(255) NOT NULL,  -- this is e.g., the Mobilize student's student id
  email_address varchar(320),
  json_data text,
  PRIMARY KEY (id),
  UNIQUE (user_id),
  UNIQUE (first_name, last_name, organization, personal_id), 
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
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
  UNIQUE (user_id, campaign_id, user_role_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_role_id) REFERENCES user_role (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Roles for a user within a class.
-- --------------------------------------------------------------------
CREATE TABLE user_class_role (
  id int unsigned NOT NULL auto_increment,
  role varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Bind users to classes. 
-- --------------------------------------------------------------------
CREATE TABLE user_class (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  class_id int unsigned NOT NULL,
  user_class_role_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (user_id, class_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_class_role_id) REFERENCES user_class_role (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Add a default role to campaign class relationships.
-- --------------------------------------------------------------------
CREATE TABLE campaign_class_default_role (
  id int unsigned NOT NULL auto_increment,
  campaign_class_id int unsigned NOT NULL,
  user_class_role_id int unsigned NOT NULL,
  user_role_id tinyint unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (campaign_class_id, user_class_role_id, user_role_id),
  CONSTRAINT FOREIGN KEY (campaign_class_id) REFERENCES campaign_class (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_class_role_id) REFERENCES user_class_role (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_role_id) REFERENCES user_role (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Survey response privacy states.
-- --------------------------------------------------------------------
CREATE TABLE survey_response_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Stores survey responses for a user in a campaign 
-- --------------------------------------------------------------------
CREATE TABLE survey_response (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  user_id int unsigned NOT NULL,
  campaign_id int unsigned NOT NULL,
  client tinytext NOT NULL,
  epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar(32) NOT NULL,
  survey_id varchar(250) NOT NULL,    -- a survey id as defined in a campaign at the XPath //surveyId
  survey text NOT NULL,               -- the max length for text is 21843 UTF-8 chars
  launch_context text,                -- trigger and other data
  location_status tinytext NOT NULL,  -- one of: unavailable, valid, stale, inaccurate 
  location text,                      -- JSON location data: longitude, latitude, accuracy, provider
  upload_timestamp datetime NOT NULL, -- the upload time based on the server time and timezone  
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  privacy_state_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  INDEX (user_id, campaign_id),
  INDEX (user_id, upload_timestamp),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,    
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES survey_response_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
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
  PRIMARY KEY (id),
  INDEX (survey_response_id),
  INDEX (prompt_id),
  -- uniqueness of survey uploads is handled by the survey_response table
  
  CONSTRAINT FOREIGN KEY (survey_response_id) REFERENCES survey_response (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Points a UUID to a URL of a media resource (such as an image). The  
-- UUID is an implicit link into the prompt_response table. The privacy
-- for images is handled by the link up to the survey_response via
-- prompt_response.
-- --------------------------------------------------------------------
CREATE TABLE url_based_resource (
    id int unsigned NOT NULL auto_increment,
    user_id int unsigned NOT NULL,
    client tinytext NOT NULL,
    uuid char (36) NOT NULL, -- joined with prompt_response.response to retrieve survey context for an item
    url text,
    audit_timestamp timestamp default current_timestamp on update current_timestamp,
    
    UNIQUE (uuid), -- disallow duplicates and index on UUID
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Mobility privacy states.
-- --------------------------------------------------------------------
CREATE TABLE mobility_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode only" mobility data. Mobility data is *not*
-- linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
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
  privacy_state_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  INDEX (uuid),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES mobility_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- High-frequency "mode + sensor data" mobility data. Mobility data is 
-- *not* linked to a campaign.
-- --------------------------------------------------------------------
CREATE TABLE mobility_extended (
  id int unsigned NOT NULL auto_increment,
  mobility_id int unsigned NOT NULL,
  sensor_data text NOT NULL,
  features text NOT NULL,
  classifier_version tinytext NOT NULL,
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (mobility_id) REFERENCES mobility (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Preferences table to hold key-value pairs of items that need to be
-- stored but we don't want to store in configuration files.
-- --------------------------------------------------------------------
CREATE TABLE preference (
  p_key varchar(50) NOT NULL,
  p_value varchar(255) NOT NULL,
  UNIQUE (p_key, p_value)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Lookup table for the privacy states of a document.
-- --------------------------------------------------------------------
CREATE TABLE document_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Lookup table for roles for a document.
-- --------------------------------------------------------------------
CREATE TABLE document_role (
  id int unsigned NOT NULL auto_increment,
  role varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Documents associated with a campaign.
-- --------------------------------------------------------------------
CREATE TABLE document (
  id int unsigned NOT NULL auto_increment,
  uuid char(36) NOT NULL,
  name varchar(255) NOT NULL,
  description text,
  extension varchar(50),
  url text NOT NULL,
  size int unsigned NOT NULL,
  privacy_state_id int unsigned NOT NULL,
  last_modified_timestamp timestamp default current_timestamp on update current_timestamp,
  creation_timestamp datetime NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (uuid),
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES document_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link of documents to classes.
-- --------------------------------------------------------------------
CREATE TABLE document_class_role (
  id int unsigned NOT NULL auto_increment,
  document_id int unsigned NOT NULL,
  class_id int unsigned NOT NULL,
  document_role_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (document_id, class_id),
  CONSTRAINT FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link of documents to campaigns.
-- --------------------------------------------------------------------
CREATE TABLE document_campaign_role (
  id int unsigned NOT NULL auto_increment,
  document_id int unsigned NOT NULL,
  campaign_id int unsigned NOT NULL,
  document_role_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (document_id, campaign_id),
  CONSTRAINT FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link of documents to users.
-- --------------------------------------------------------------------
CREATE TABLE document_user_role (
  id int unsigned NOT NULL auto_increment,
  document_id int unsigned NOT NULL,
  user_id int unsigned NOT NULL,
  document_role_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (document_id, user_id),
  CONSTRAINT FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Audit trail for who created which document
-- --------------------------------------------------------------------
CREATE TABLE document_user_creator (
  id int unsigned NOT NULL auto_increment,
  document_id int unsigned NOT NULL,
  username varchar(25) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (document_id),
  CONSTRAINT FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;