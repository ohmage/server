-- This is probably something we should begin doing at the head of all of our
-- update scripts. It's a routine thing to do. It can, however, be very time
-- consuming because InnoDB's OPTIMIZE TABLE command is mapped to ALTER TABLE,
-- which simply recreates the table.
OPTIMIZE TABLE mobility;
OPTIMIZE TABLE mobility_extended;
OPTIMIZE TABLE audit;
OPTIMIZE TABLE audit_extra;
OPTIMIZE TABLE audit_parameter;
-- We could optimize other tables, but these are simply the large ones at the 
-- time of writing this.

-- There is an unnecessary complex key that exited before we had UUIDs on the
-- survey responses. To remove it, we need to do some MySQL tricks.
-- First, we can get rid of this unnecessary key.
ALTER TABLE survey_response DROP KEY user_id_2;
-- Next, we have to create some keys. MySQL needs at least one key per row in
-- a constraint. For example, we have a foreign key constraint on the campaign
-- ID, so we must have at least one key that contains the campaign_id column.
-- It is probably best if we simply add individual keys for those columns 
-- instead of doing MySQL's default which is to create a complex key.
ALTER TABLE survey_response ADD KEY key_user_id (user_id);
ALTER TABLE survey_response ADD KEY key_campaign_id (campaign_id);
-- Now, we can get rid of some of these composite keys.
ALTER TABLE survey_response DROP KEY user_id;
ALTER TABLE survey_response DROP KEY campaign_id;

-- Add audit timestamps to the user table.
ALTER TABLE user ADD COLUMN
    last_modified_timestamp TIMESTAMP DEFAULT now() ON UPDATE now();
ALTER TABLE user_personal ADD COLUMN
    last_modified_timestamp TIMESTAMP DEFAULT now() ON UPDATE now();
    
-- Change the 'audit_timestamp' to the 'last_modified_timestamp' in the
-- 'survey_response' table.
ALTER TABLE survey_response CHANGE COLUMN audit_timestamp 
    last_modified_timestamp TIMESTAMP DEFAULT now() ON UPDATE now();

-- Point the image and document directories to the /opt/ohmage subtree 
UPDATE preference SET p_value = '/opt/ohmage/userdata/documents' 
	WHERE p_key = 'document_directory'; 

UPDATE preference SET p_value = '/opt/ohmage/userdata/images' 
	WHERE p_key = 'image_directory';
	
-- Add the new video directory.
INSERT INTO preference VALUES
    ('video_directory', '/opt/ohmage/userdata/videos');
    
-- Create the observer tables.
CREATE TABLE observer (
	id int unsigned NOT NULL AUTO_INCREMENT,
	user_id int unsigned NOT NULL,
	observer_id varchar(255) NOT NULL,
	version bigint NOT NULL,
	name varchar(256) NOT NULL,
	description text NOT NULL,
	version_string varchar(32) NOT NULL,
	last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
	PRIMARY KEY (id),
	UNIQUE KEY observer_unique_key_id_version (observer_id, version),
	KEY observer_key_observer_id (observer_id),
	KEY observer_key_user_id (user_id),
	CONSTRAINT observer_foreign_key_user_id 
	   FOREIGN KEY (user_id) 
	   REFERENCES user (id) 
	   ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE observer_stream (
	id int unsigned NOT NULL AUTO_INCREMENT,
	stream_id varchar(255) NOT NULL,
	version bigint NOT NULL,
	name varchar(256) NOT NULL,
	description text NOT NULL,
	with_id boolean DEFAULT NULL,
	with_timestamp boolean DEFAULT NULL,
	with_location boolean DEFAULT NULL,
	stream_schema text NOT NULL,
    last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
	PRIMARY KEY (id),
	KEY observer_stream_key_stream_id (stream_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE observer_stream_link (
  id int(10) unsigned NOT NULL AUTO_INCREMENT,
  observer_id int(10) unsigned NOT NULL,
  observer_stream_id int(10) unsigned NOT NULL,
  PRIMARY KEY (id),
  KEY observer_stream_link_key_observer_id (observer_id),
  KEY observer_stream_link_key_stream_id (observer_stream_id),
  -- There should only be one instance of an observer ID/version pair to a 
  -- stream ID/version pair.
  UNIQUE KEY observer_stream_link_unique_key_observer_stream 
    (observer_id, observer_stream_id),
  CONSTRAINT observer_stream_link_foreign_key_observer_id
    FOREIGN KEY (observer_id)
    REFERENCES observer (id)
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT observer_stream_link_foreign_key_stream_id
    FOREIGN KEY (observer_id)
    REFERENCES observer (id)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE observer_stream_data (
  id int unsigned NOT NULL AUTO_INCREMENT,
  user_id int unsigned NOT NULL,
  observer_stream_link_id int unsigned NOT NULL,
  uid varchar(255) DEFAULT NULL,
  time bigint DEFAULT NULL,
  time_offset bigint DEFAULT NULL,
  time_adjusted bigint DEFAULT NULL,
  time_zone varchar(32) DEFAULT NULL,
  location_timestamp varchar(64) DEFAULT NULL,
  location_latitude double DEFAULT NULL,
  location_longitude double DEFAULT NULL,
  location_accuracy double DEFAULT NULL,
  location_provider varchar(255) DEFAULT NULL,
  data blob,
  last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
  PRIMARY KEY (id),
  KEY observer_stream_data_key_observer_stream_link_id (observer_stream_link_id),
  KEY observer_stream_data_key_user_id (user_id),
  INDEX observer_stream_data_index_time_adjusted (time_adjusted),
  CONSTRAINT observer_stream_data_foreign_key_user_id 
    FOREIGN KEY (user_id) 
    REFERENCES user (id) 
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT observer_stream_data_foreign_key_observer_stream_link_id 
    FOREIGN KEY (observer_stream_link_id) 
    REFERENCES observer_stream_link (id) 
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Add the new preference key.
INSERT INTO preference 
    VALUES ('audit_log_location', '/opt/ohmage/logs/audits/'),
           ('fully_qualified_domain_name', 'http://localhost/');