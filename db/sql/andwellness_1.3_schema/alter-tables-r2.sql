USE andwellness;

-- Get rid of the UTC designation on the column names as UTC data is no longer stored there
-- The alter statements helpfully also update the names on any index using the renamed columns

alter table mobility_mode_only_entry change column utc_time_stamp time_stamp timestamp;
alter table mobility_mode_only_entry change column utc_epoch_millis epoch_millis bigint(20) unsigned;

alter table mobility_mode_features_entry change column utc_time_stamp time_stamp timestamp;
alter table mobility_mode_features_entry change column utc_epoch_millis epoch_millis bigint(20) unsigned;

alter table mobility_entry_five_min_summary change column utc_time_stamp time_stamp timestamp;

alter table prompt_response change column utc_time_stamp time_stamp timestamp;
alter table prompt_response change column utc_epoch_millis epoch_millis bigint(20) unsigned;

-- Renaming "health worker" role to "researcher"

update user_role set label = 'researcher' where id = 3;