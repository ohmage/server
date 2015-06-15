use ohmage;

-- Update the preference table to add the new preference indicating the root 
-- directory of the document prompt (documentp) 
-- Note: ideally, we want to only set ohmage data root directory in the preference
-- table, and use that to store all data types. 
INSERT INTO preference VALUES 
    ('documentp_directory', '/opt/ohmage/userdata/file');

-- Add a metadata column to the url_based_resource table to keep track of 
-- http headers (e.g. content-type, filename, etc.) that were part of the 
-- survey/upload request. This metadata will be used for media/read.  
ALTER TABLE url_based_resource 
    ADD COLUMN `metadata` text DEFAULT NULL;
