USE andwellness;

-- new columns for user authentication
--  password varchar(100) NOT NULL
-- is_new_account bit not NULL
ALTER TABLE user 
ADD COLUMN password varchar(100) NOT NULL,
ADD COLUMN is_new_account bit NOT NULL

