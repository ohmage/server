-- ----------------------------------------------------------------------
-- The table to store the user setup request 
-------------------------------------------------------------------------
-- use status:varchar for simplicity. possible values: pending/approved/rejected
CREATE TABLE IF NOT EXISTS user_setup_request (
  id int unsigned NOT NULL auto_increment,
  uuid CHAR(36) NOT NULL UNIQUE,
  user_id int unsigned NOT NULL,
  email_address varchar(320) NOT NULL,
  content mediumtext NOT NULL,
  status varchar(25) NOT NULL DEFAULT "pending",  
  creation_timestamp datetime NOT NULL, 
  last_modified_timestamp timestamp DEFAULT now() ON UPDATE now(),
  PRIMARY KEY (id),
  UNIQUE (uuid),
  INDEX (uuid),
  INDEX (user_id),
  CONSTRAINT FOREIGN
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
