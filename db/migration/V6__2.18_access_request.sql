-- ----------------------------------------------------------------------
-- The table to store the user access request 
-------------------------------------------------------------------------
-- use status:varchar for simplicity. possible values: pending/approved/rejected
CREATE TABLE IF NOT EXISTS access_request (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  user_id int unsigned NOT NULL,
  email_address varchar(320) NOT NULL,
  type varchar(25) NOT NULL,  
  status varchar(25) NOT NULL,  
  content mediumtext NOT NULL,
  creation_timestamp datetime NOT NULL, 
  last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
  PRIMARY KEY (id),
  UNIQUE (uuid),
  INDEX (uuid),
  INDEX (user_id),
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

INSERT INTO preference (p_key, p_value) VALUES 
 	('mail_admin_address', 'root@localhost'),
    ('mail_access_request_notify_admin', 'false'),
    ('mail_access_request_sender_address', 'no-reply@ohmage.org'),
    ('mail_access_request_subject', 'ohmage: Access Request Status')
    ON DUPLICATE KEY UPDATE p_value=VALUES(p_value);
