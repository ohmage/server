-- Add the new preferences.
INSERT INTO preference VALUES
    ('audit_log_location', '/opt/ohmage/logs/audits/'),
    ('fully_qualified_domain_name', 'http://localhost');
    
-- Update the observer_stream_data table.
ALTER TABLE observer_stream_data
    MODIFY COLUMN `time` bigint(20) DEFAULT NULL;
ALTER TABLE observer_stream_data
    MODIFY COLUMN `time_offset` bigint(20) DEFAULT NULL;
ALTER TABLE observer_stream_data 
    ADD COLUMN `time_adjusted` bigint(20) DEFAULT NULL
    AFTER `time_offset`;
ALTER TABLE observer_stream_data
    MODIFY COLUMN `data` longtext NOT NULL;
ALTER TABLE observer_stream_data 
    ADD INDEX `observer_stream_data_index_time` (time);
ALTER TABLE observer_stream_data 
    ADD INDEX `observer_stream_data_index_time_adjusted` (time_adjusted);
    
-- NOTE: After running this script, be sure to upgrade any Avro data to regular
-- JSON data. 
SELECT 'Be sure to upgrade the data in the observer_stream_data table if any Avro data exists.';