--
-- These are very destructive statements due to cascading deletes. Use with care!
--

USE andwellness;

-- new user roles
TRUNCATE user_role;
ALTER TABLE user_role DROP COLUMN label;
ALTER TABLE user_role ADD COLUMN role varchar(50) NOT NULL;


-- remove campaign_configuration
TRUNCATE campaign_configuration;
TRUNCATE campaign;
-- remove campaign_configuration foreign key from survey_response
ALTER TABLE survey_response DROP FOREIGN KEY survey_response_ibfk_2;
ALTER TABLE survey_response DROP COLUMN campaign_configuration_id;
DROP TABLE campaign_configuration;


-- campaign changes
ALTER TABLE campaign DROP COLUMN label;
ALTER TABLE campaign DROP COLUMN name;
ALTER TABLE campaign ADD COLUMN name varchar(255) NOT NULL;
ALTER TABLE campaign ADD COLUMN description text NOT NULL;
ALTER TABLE campaign ADD COLUMN urn varchar(255) NOT NULL;
ALTER TABLE campaign ADD COLUMN xml mediumtext NOT NULL;
ALTER TABLE campaign ADD COLUMN running_state varchar(50) NOT NULL;
ALTER TABLE campaign ADD COLUMN privacy_state varchar(50) NOT NULL;
ALTER TABLE campaign ADD COLUMN creation_timestamp datetime NOT NULL;
ALTER TABLE campaign ADD UNIQUE INDEX (urn);


-- link survey_response to campaign
ALTER TABLE survey_response ADD COLUMN campaign_id int unsigned NOT NULL;
ALTER TABLE survey_response ADD CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE;
ALTER TABLE survey_response ADD INDEX (user_id, campaign_id);  


-- sharing of prompt responses
ALTER TABLE prompt_response ADD COLUMN privacy_state varchar(50) NOT NULL;


-- PRIVILEGED users can create campaigns. Does this belong in user_class?
ALTER TABLE user ADD COLUMN campaign_creation_privilege bit NOT NULL;


-- new tables
CREATE TABLE class (
  id int unsigned NOT NULL auto_increment,
  urn varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (urn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
 
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

CREATE TABLE campaign_class (
  id int unsigned NOT NULL auto_increment,
  campaign_id int unsigned NOT NULL,
  class_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (campaign_id, class_id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (class_id) REFERENCES class (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;


