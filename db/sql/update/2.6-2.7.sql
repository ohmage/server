CREATE TABLE mobility (
  id int unsigned NOT NULL auto_increment,
  original_id int unsigned,
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
  INDEX (user_id, msg_timestamp),
  UNIQUE (user_id, epoch_millis), -- enforce no-duplicates rule at the table level
  CONSTRAINT FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT FOREIGN KEY (privacy_state_id) REFERENCES mobility_privacy_state (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into mobility(id, user_id, client, msg_timestamp, epoch_millis, phone_timezone, location_status, location, mode, upload_timestamp, audit_timestamp, privacy_state_id) select * from mobility_mode_only;

drop table mobility_mode_only;

insert into mobility(original_id, user_id, client, msg_timestamp, epoch_millis, phone_timezone, location_status, location, mode, upload_timestamp, audit_timestamp, privacy_state_id) select id, user_id, client, msg_timestamp, epoch_millis, phone_timezone, location_status, location, mode, upload_timestamp, audit_timestamp, privacy_state_id from mobility_extended;

CREATE TABLE mobility_extended_new (
  id int unsigned NOT NULL auto_increment,
  mobility_id int unsigned NOT NULL,
  sensor_data text NOT NULL,
  features text NOT NULL,
  classifier_version tinytext NOT NULL,
  audit_timestamp timestamp default current_timestamp on update current_timestamp,
  PRIMARY KEY (id),
  CONSTRAINT FOREIGN KEY (mobility_id) REFERENCES mobility (id) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

insert into mobility_extended_new(mobility_id, sensor_data, features, classifier_version, audit_timestamp) select m.id, sensor_data, features, classifier_version, me.audit_timestamp from mobility m, mobility_extended me where m.original_id=me.id;

drop table mobility_extended;

rename table mobility_extended_new to mobility_extended;

alter table mobility drop column original_id;

alter table campaign add icon_url varchar(255);

alter table campaign add authored_by varchar(255);