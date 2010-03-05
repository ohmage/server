USE andwellness;

-- --------------------------------------------------
-- Changes for password-based authentication and 
-- workflow.
-- --------------------------------------------------
ALTER TABLE user 
ADD COLUMN password varchar(100) NOT NULL,
ADD COLUMN new_account bit NOT NULL

