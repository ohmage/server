-- --------------------------------------------------------------------
-- Links surveys (stored in raw XML format) to a campaign. 
-- --------------------------------------------------------------------
CREATE TABLE campaign_surveys (
  id smallint(4) unsigned NOT NULL auto_increment,
  campaign_id NOT NULL,
  xml mediumtext NOT NULL,
  PRIMARY KEY (id)
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Stores survey prompt responses for a user in a campaign 
-- --------------------------------------------------------------------
CREATE TABLE survey_response (
  id integer unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  campaign_id smallint(4) unsigned NOT NULL,
  time_stamp timestamp NOT NULL,
  epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar(32) NOT NULL,
  latitude double,
  longitude double,
  json text NOT NULL, -- the max length for text is 21875 UTF-8 chars
  PRIMARY KEY (id),
  INDEX (campaign_id, user_id),
  CONSTRAINT FOREIGN KEY (prompt_id) REFERENCES prompt (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE    
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Prompt types. Types are strings to be used as hints in determining 
-- how to process prompt response data. 
-- --------------------------------------------------------------------
CREATE TABLE prompt_type (
    id tinyint unsigned NOT NULL auto_increment,
    type varchar(10) NOT NULL,
    PRIMARY KEY (id)
)

-- --------------------------------------------------------------------
-- Stores individual prompt responses for a user in a campaign 
-- --------------------------------------------------------------------
CREATE TABLE prompt_response (
  id integer unsigned NOT NULL auto_increment,
  user_id smallint(6) unsigned NOT NULL,
  campaign_id smallint(4) unsigned NOT NULL,
  survey_response_id integer unsigned NOT NULL,
  prompt_id text NOT NULL,  -- defined by the prompt id from the survey XML
  group_id varchar(50),
  iteration tinyint unsigned,
  epoch_millis bigint unsigned NOT NULL,
  response text NOT NULL, -- the data format is defined by the prompt type, most likely it will be JSON or a UUID (for images)
  prompt_response_type_id tinyint unsigned NOT NULL,
   
  PRIMARY KEY (id),
  INDEX (campaign_id, user_id, survey_response_id),
  UNIQUE (user_id, epoch_millis, prompt_id, iteration), -- this will not work because iteration can be null
  
  CONSTRAINT FOREIGN KEY (prompt_id) REFERENCES prompt (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,    
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (survey_response_id) REFERENCES survey_response (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (prompt_type_id) REFERENCES prompt_type (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Points a UUID to a URL-based resource (such as an image) 
-- --------------------------------------------------------------------
CREATE TABLE url_based_resource (
    id  integer unsigned NOT NULL auto_increment,
    uuid char (xxxx) NOT NULL,
    url text,
    UNIQUE (uuid),
    PRIMARY KEY (id)
)
