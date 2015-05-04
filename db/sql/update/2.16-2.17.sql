-- Update the preference table to add the new preference indicating the root 
-- directory of the document prompt (documentp) 
-- Note: ideally, we want to only set ohmage data root directory in the preference
-- table, and use that to store all data types. 
INSERT INTO preference VALUES 
    ('documentp_directory', '/opt/ohmage/userdata/documentp');

