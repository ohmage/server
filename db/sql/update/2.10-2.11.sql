-- Move the Email address field to the user table.
ALTER TABLE user ADD COLUMN email_address VARCHAR(320);
UPDATE user, user_personal SET user.email_address = user_personal.email_address WHERE user.id = user_personal.user_id;
ALTER TABLE user_personal DROP COLUMN email_address;

-- Drop the JSON data column from the personal information.
ALTER TABLE user_personal DROP COLUMN json_data;