CREATE TABLE audit_request_type (
  id int unsigned NOT NULL auto_increment,
  request_type varchar(8) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE audit (
  id int unsigned NOT NULL auto_increment,
  request_type_id int unsigned NOT NULL,
  uri text NOT NULL,
  client text,
  request_id VARCHAR(255) NOT NULL,
  device_id text,
  response text NOT NULL,
  received_millis long NOT NULL,
  respond_millis long NOT NULL,
  db_timestamp timestamp default current_timestamp NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (request_type_id) REFERENCES audit_request_type (id) ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE audit_extra (
  id int unsigned NOT NULL auto_increment,
  audit_id int unsigned NOT NULL,
  extra_key text NOT NULL,
  extra_value text NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (audit_id) REFERENCES audit (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE audit_parameter (
  id int unsigned NOT NULL auto_increment,
  audit_id int unsigned NOT NULL,
  param_key text NOT NULL,
  param_value text NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (audit_id) REFERENCES audit (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE class (
  id int unsigned NOT NULL auto_increment,
  urn varchar(255) NOT NULL,
  name varchar(255) NOT NULL,
  description text,
  PRIMARY KEY (id),
  UNIQUE (urn)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE campaign_running_state (
  id int unsigned NOT NULL auto_increment,
  running_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (running_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE campaign_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE user (
  id int unsigned NOT NULL auto_increment,
  username varchar(25) NOT NULL,
  password varchar(60) NOT NULL,
  enabled bit NOT NULL,
  new_account bit NOT NULL,
  campaign_creation_privilege bit NOT NULL,
  class_creation_privilege bit NOT NULL DEFAULT FALSE,
  user_setup_privilege bit NOT NULL DEFAULT FALSE,
  email_address varchar(320),
  admin bit NOT NULL,
  last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
  PRIMARY KEY (id),
  UNIQUE (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_personal (
  id int unsigned NOT NULL auto_increment,
  user_id int unsigned NOT NULL,
  first_name varchar(255) NOT NULL,
  last_name varchar(255) NOT NULL,
  organization varchar(255) NOT NULL,
  personal_id varchar(255) NOT NULL,
  last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
  PRIMARY KEY (id),
  UNIQUE (user_id),
  UNIQUE (first_name, last_name, organization, personal_id), 
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_registration(
    id int unsigned NOT NULL auto_increment,
    user_id int unsigned NOT NULL,
    registration_id VARCHAR(128) NOT NULL,
    request_timestamp BIGINT UNSIGNED NOT NULL,
    accepted_timestamp BIGINT UNSIGNED DEFAULT NULL,
    PRIMARY KEY (id),
    UNIQUE (user_id),
    CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE user_role (
  id tinyint unsigned NOT NULL auto_increment,
  role varchar(50) NOT NULL,
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE user_class_role (
  id int unsigned NOT NULL auto_increment,
  role varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE survey_response_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE survey_response (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  user_id int unsigned NOT NULL,
  campaign_id int unsigned NOT NULL,
  client tinytext NOT NULL,
  epoch_millis bigint unsigned NOT NULL, 
  phone_timezone varchar(32) NOT NULL,
  survey_id varchar(250) NOT NULL,    
  survey text CHARACTER SET utf8 NOT NULL,
  launch_context text,             
  location_status tinytext NOT NULL,
  location text,                   
  upload_timestamp datetime NOT NULL,
  last_modified_timestamp timestamp default current_timestamp on update current_timestamp,
  privacy_state_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  KEY key_user_id (user_id),
  KEY key_campaign_id (campaign_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,    
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES survey_response_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE prompt_response (
  id int unsigned NOT NULL auto_increment,
  survey_response_id int unsigned NOT NULL,
  prompt_id varchar(250) NOT NULL,
  prompt_type varchar(250) NOT NULL,
  repeatable_set_id varchar(250),
  repeatable_set_iteration tinyint unsigned,
  response text CHARACTER SET utf8 NOT NULL,
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  PRIMARY KEY (id),
  INDEX (survey_response_id),
  INDEX (prompt_id),
  INDEX response_image (response(36)),
  
  CONSTRAINT FOREIGN KEY (survey_response_id) REFERENCES survey_response (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE url_based_resource (
    id int unsigned NOT NULL auto_increment,
    user_id int unsigned NOT NULL,
    client tinytext NOT NULL,
    uuid char (36) NOT NULL,
    url text,
    audit_timestamp timestamp default current_timestamp on update current_timestamp,
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    UNIQUE (uuid),
    PRIMARY KEY (id),
    CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mobility_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE mobility (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  user_id int unsigned NOT NULL,
  client tinytext NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  phone_timezone varchar(32) NOT NULL,
  location_status tinytext NOT NULL,
  location text,
  mode varchar(30) NOT NULL,
  upload_timestamp datetime NOT NULL,
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  privacy_state_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  INDEX (uuid),
  INDEX index_time (epoch_millis),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES mobility_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE preference (
  p_key varchar(50) NOT NULL,
  p_value text NOT NULL,
  UNIQUE unique_p_key (p_key)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE document_privacy_state (
  id int unsigned NOT NULL auto_increment,
  privacy_state varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (privacy_state)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE document_role (
  id int unsigned NOT NULL auto_increment,
  role varchar(50) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (role)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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

CREATE TABLE document_user_creator (
  id int unsigned NOT NULL auto_increment,
  document_id int unsigned NOT NULL,
  username varchar(25) NOT NULL,
  PRIMARY KEY (id),
  UNIQUE (document_id),
  CONSTRAINT FOREIGN KEY (document_id) REFERENCES document (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE annotation (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  user_id int unsigned NOT NULL,
  epoch_millis bigint unsigned NOT NULL,
  timezone varchar(32) NOT NULL,
  client tinytext NOT NULL, 
  annotation text NOT NULL,
  last_modified_timestamp timestamp default current_timestamp on update current_timestamp,
  creation_timestamp datetime NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE survey_response_annotation (
  id int unsigned NOT NULL auto_increment,
  survey_response_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (survey_response_id) REFERENCES survey_response (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE prompt_response_annotation (
  id int unsigned NOT NULL auto_increment,
  prompt_response_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (prompt_response_id) REFERENCES prompt_response (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE campaign_annotation (
  id int unsigned NOT NULL auto_increment,
  campaign_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (campaign_id) REFERENCES campaign (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE annotation_annotation (
  id int unsigned NOT NULL auto_increment,
  root_annotation_id int unsigned NOT NULL,
  annotation_id int unsigned NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (root_annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (annotation_id) REFERENCES annotation (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

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
  time bigint(20) DEFAULT NULL,
  time_offset bigint(20) DEFAULT NULL,
  time_adjusted bigint(20) DEFAULT NULL,
  time_zone varchar(32) DEFAULT NULL,
  location_timestamp varchar(64) DEFAULT NULL,
  location_latitude double DEFAULT NULL,
  location_longitude double DEFAULT NULL,
  location_accuracy double DEFAULT NULL,
  location_provider varchar(255) DEFAULT NULL,
  data longtext NOT NULL,
  last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
  PRIMARY KEY (id),
  KEY observer_stream_data_key_observer_stream_link_id (observer_stream_link_id),
  KEY observer_stream_data_key_user_id (user_id),
  INDEX observer_stream_data_index_time (time),
  INDEX observer_stream_data_index_time_adjusted (time_adjusted),
  INDEX `observer_stream_data_query`
    (`user_id`,`observer_stream_link_id`,`time_adjusted`,`time`),
  INDEX `observer_stream_data_index_link_user_adjusted`
    (`observer_stream_link_id`,`user_id`,`time_adjusted`),
  INDEX `osd_duplicate_data_point_read`
    (`user_id`, `observer_stream_link_id`, `uid`),  
  CONSTRAINT observer_stream_data_foreign_key_user_id 
    FOREIGN KEY (user_id) 
    REFERENCES user (id) 
    ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT observer_stream_data_foreign_key_observer_stream_link_id 
    FOREIGN KEY (observer_stream_link_id) 
    REFERENCES observer_stream_link (id) 
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaign_survey_lookup` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `survey_id` varchar(255) NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_survey_lookup_index_survey_id` (`survey_id`),
  KEY `campaign_survey_lookup_fk_campaign_id` (`campaign_id`),
  CONSTRAINT `campaign_survey_lookup_fk_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES `campaign` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaign_prompt_lookup` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `prompt_id` varchar(255) NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  PRIMARY KEY (`id`),
  KEY `campaign_prompt_lookup_index_prompt_id` (`prompt_id`),
  KEY `campaign_prompt_lookup_fk_campaign_id` (`campaign_id`),
  CONSTRAINT `campaign_prompt_lookup_fk_campaign_id`
    FOREIGN KEY (`campaign_id`)
    REFERENCES `campaign` (`id`)
    ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaign_mask` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `assigner_user_id` int(10) unsigned NOT NULL,
  `assignee_user_id` int(10) unsigned NOT NULL,
  `campaign_id` int(10) unsigned NOT NULL,
  `mask_id` varchar(36) NOT NULL,
  `creation_time` bigint(20) NOT NULL,
  PRIMARY KEY (`id`),
  INDEX `campaign_mask_index_mask_id` (`mask_id`),
  CONSTRAINT `campaign_mask_fk_assigner_user_id` FOREIGN KEY (`assigner_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaign_mask_fk_assignee_user_id` FOREIGN KEY (`assignee_user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `campaign_mask_fk_campaign_id` FOREIGN KEY (`campaign_id`) REFERENCES `campaign` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaign_mask_survey_id` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `campaign_mask_id` int(10) unsigned NOT NULL,
  `survey_id` varchar(255) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `campaing_mask_unique_mask_survey` (`campaign_mask_id`,`survey_id`),
  CONSTRAINT `campaign_mask_fk_survey_id` FOREIGN KEY (`campaign_mask_id`) REFERENCES `campaign_mask` (`id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `omh_authentication` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL,
  `auth_key` varchar(100) NOT NULL,
  `auth_value` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `omh_authentication_unique_domain_key` (`domain`,`auth_key`),
  KEY `omh_authentication_index_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `campaign_mask_survey_prompt_map` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `campaign_mask_id` int(10) unsigned NOT NULL,
    `survey_id` varchar(255) NOT NULL,
    `prompt_id` varchar(255) NOT NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `campaing_mask_unique_mask_survey_prompt`
        (`campaign_mask_id`,`survey_id`, `prompt_id`),
    CONSTRAINT `campaign_mask_fk_survey_prompt_map`
        FOREIGN KEY (`campaign_mask_id`) REFERENCES `campaign_mask` (`id`)
        ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

CREATE TABLE `observer_stream_data_invalid` (
    `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
    `user_id` int(10) unsigned NOT NULL,
    `observer_id` int(10) unsigned NOT NULL,
    `time_recorded` bigint(20) NOT NULL,
    `point_index` int(20) unsigned NOT NULL,
    `reason` text NOT NULL,
    `data` longtext NOT NULL,
    `last_modified_timestamp` timestamp NOT NULL
        DEFAULT CURRENT_TIMESTAMP
        ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    CONSTRAINT `observer_stream_data_invalid_fk_user_id`
        FOREIGN KEY (`user_id`)
        REFERENCES `user` (`id`)
        ON DELETE CASCADE
        ON UPDATE CASCADE,
    CONSTRAINT `observer_stream_data_invalid_fk_observer_id`
      FOREIGN KEY (`observer_id`)
      REFERENCES `observer` (`id`)
      ON DELETE CASCADE
      ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
