USE andwellness;

-- --------------------------------------------------
-- Changes for password-based authentication and 
-- workflow.
-- --------------------------------------------------
ALTER TABLE user 
ADD COLUMN password varchar(100) NOT NULL,
ADD COLUMN new_account bit NOT NULL;

-- --------------------------------------------------
-- Removing subdomains from app
-- --------------------------------------------------
ALTER TABLE campaign
DROP COLUMN subdomain;

-- --------------------------------------------------
-- Give the initial pilot campaign a better label
-- --------------------------------------------------

UPDATE campaign SET label = 'Breast Cancer Survivor Study' WHERE id = 1;