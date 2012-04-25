-- This is probably something we should begin doing at the head of all of our
-- update scripts. It's a routine thing to do. It can, however, be very time
-- consuming because InnoDB's OPTIMIZE TABLE command is mapped to ALTER TABLE,
-- which simply recreates the table.
OPTIMIZE TABLE mobility;
OPTIMIZE TABLE mobility_extended;
OPTIMIZE TABLE audit;
OPTIMIZE TABLE audit_extra;
OPTIMIZE TABLE audit_parameter;
-- We could optimize other tables, but these are simply the large ones at the 
-- time of writing this.

-- There is an unnecessary complex key that exited before we had UUIDs on the
-- survey responses. To remove it, we need to do some MySQL tricks.
-- First, we can get rid of this unnecessary key.
ALTER TABLE survey_response DROP KEY user_id_2;
-- Next, we have to create some keys. MySQL needs at least one key per row in
-- a constraint. For example, we have a foreign key constraint on the campaign
-- ID, so we must have at least one key that contains the campaign_id column.
-- It is probably best if we simply add individual keys for those columns 
-- instead of doing MySQL's default which is to create a complex key.
ALTER TABLE survey_response ADD KEY key_user_id (user_id);
ALTER TABLE survey_response ADD KEY key_campaign_id (campaign_id);
-- Now, we can get rid of some of these composite keys.
ALTER TABLE survey_response DROP KEY user_id;
ALTER TABLE survey_response DROP KEY campaign_id;

-- Point the image and document directories to the /opt/ohmage subtree 
UPDATE preference SET p_value = '/opt/ohmage/userdata/documents' 
	WHERE p_key = 'document_directory'; 

UPDATE preference SET p_value = '/opt/ohmage/userdata/images' 
	WHERE p_key = 'image_directory'; 
    