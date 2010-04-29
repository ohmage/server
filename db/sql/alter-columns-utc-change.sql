USE andwellness;

-- the alter statements helpfully also update the names on any index using the renamed columns

alter table mobility_mode_only_entry change column utc_time_stamp time_stamp timestamp;
alter table mobility_mode_only_entry change column utc_epoch_millis epoch_millis bigint(20) unsigned;

alter table mobility_mode_features_entry change column utc_time_stamp time_stamp timestamp;
alter table mobility_mode_features_entry change column utc_epoch_millis epoch_millis bigint(20) unsigned;

alter table mobility_entry_five_min_summary change column utc_time_stamp time_stamp timestamp;

alter table prompt_response change column utc_time_stamp time_stamp timestamp;
alter table prompt_response change column utc_epoch_millis epoch_millis bigint(20) unsigned;

