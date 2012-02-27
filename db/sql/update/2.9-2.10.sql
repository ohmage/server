-- Creates an index on prompt responses for the first 36 characters, the size 
-- of a UUID. This greatly increases the response time of image/read with a 
-- little memory overhead.
CREATE INDEX response_image ON prompt_response (response(36));

-- Add the preference defining the number of survey responses. The default is
-- -1 which represents "no limit".
INSERT INTO preference(p_key, p_value) VALUES 
    ('max_survey_response_page_size', '-1');

-- --------------------------------------------------------------------
-- Annotations (text blobs) for different system entities.
-- --------------------------------------------------------------------
CREATE TABLE annotation (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  epoch_millis bigint unsigned NOT NULL,
  timezone varchar(32) NOT NULL,
  client tinytext NOT NULL, 
  annotation text NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link survey_responses to annotations.
-- --------------------------------------------------------------------
CREATE TABLE survey_response_annotation (
  id int unsigned NOT NULL auto_increment,
  survey_response_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (survey_response_id) REFERENCES survey_response (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link prompt_responses to annotations.
-- --------------------------------------------------------------------
CREATE TABLE prompt_response_annotation (
  id int unsigned NOT NULL auto_increment,
  prompt_response_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (prompt_response_id) REFERENCES prompt_response (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link campaigns to annotations.
-- --------------------------------------------------------------------
CREATE TABLE campaign_annotation (
  id int unsigned NOT NULL auto_increment,
  campaign_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- --------------------------------------------------------------------
-- Link annotations to annotations.
-- --------------------------------------------------------------------
CREATE TABLE annotation_annotation (
  id int unsigned NOT NULL auto_increment,
  root_annotation_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (root_annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;