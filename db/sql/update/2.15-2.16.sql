-- Add the Open mHealth table for storing authentication and authorization
-- information.
CREATE TABLE `omh_authentication` (
  `id` int(10) unsigned NOT NULL AUTO_INCREMENT,
  `domain` varchar(100) NOT NULL,
  `auth_key` varchar(100) NOT NULL,
  `auth_value` varchar(100) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `omh_authentication_unique_domain_key` (`domain`,`auth_key`),
  KEY `omh_authentication_index_domain` (`domain`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- Add a default audio directory.
INSERT INTO preference(p_key, p_value) VALUES
    ('audio_directory', '/opt/ohmage/userdata/audio');

-- Add the class creation privilege.
ALTER TABLE user
    ADD COLUMN `class_creation_privilege`
    BOOLEAN NOT NULL DEFAULT FALSE
    AFTER `campaign_creation_privilege`;

-- Add the user setup privilege.
ALTER TABLE user
    ADD COLUMN `user_setup_privilege`
    BOOLEAN NOT NULL DEFAULT FALSE
    AFTER `class_creation_privilege`;