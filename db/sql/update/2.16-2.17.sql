use ohmage;

-- Update the preference table to add the new preference indicating the root 
-- directory of the file prompt  
-- Note: ideally, we want to only set ohmage data root directory in the preference
-- table, AND use that to store all data types. 
INSERT INTO preference VALUES 
    ('file_directory', '/opt/ohmage/userdata/files')
    ON DUPLICATE KEY UPDATE p_value = VALUES(p_value);
    
-- Configure user/setup functionality. If the row already exist, update it.
INSERT INTO preference VALUES 
    ('user_setup_enabled', 'false')
    ON DUPLICATE KEY UPDATE p_value = VALUES(p_value) ;
 
-- Add a metadata column to the url_based_resource table to keep track of 
-- http headers (e.g. content-type, filename, etc.) that were part of the 
-- survey/upload request. This metadata will be used for media/read.  
ALTER TABLE url_based_resource 
    ADD COLUMN `metadata` text CHARACTER SET utf8 DEFAULT NULL;

-- If the server is running 2.16 instead of 2.16.1 (user-setup), the user table 
-- will not have the plaintext_password column. 
-- Check whether the plaintext_password exist, if not create it first.
DROP PROCEDURE IF EXISTS add_plaintext_password;

DELIMITER $$

CREATE DEFINER=CURRENT_USER PROCEDURE add_plaintext_password ( ) 
BEGIN
DECLARE colName TEXT;
SELECT column_name INTO colName
FROM information_schema.columns 
WHERE table_schema = 'ohmage'
  AND table_name = 'user'
  AND column_name = 'plaintext_password';

IF colName is null THEN 
  ALTER TABLE user ADD COLUMN `plaintext_password` TEXT CHARACTER SET utf8 DEFAULT null;
END IF; 

END$$

DELIMITER ;

CALL add_plaintext_password;

DROP PROCEDURE add_plaintext_password; 
    
-- Renaming plaintext_password to original_password to appropriately reflect 
-- the implementation    
ALTER TABLE user
    CHANGE COLUMN `plaintext_password` `initial_password` text CHARACTER SET utf8 DEFAULT NULL;

-- Add a creation_time AND last_modified_timestamp to keep track of the
-- class activity. 
-- For mysql 5.6, the default of the datetime can be set to CURRENT_TIMESTAMP.
ALTER TABLE class 
    ADD COLUMN (
    	`creation_timestamp` datetime DEFAULT null,
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);
    	
-- Create a trigger so that the class.creation_time will be automatically set to now.
-- This will minimize the impact to the java code since it is done at the db level.     	
-- No need to create trigger. Already update the app to reflect creation_time. 
-- CREATE TRIGGER `class_insert` BEFORE INSERT ON `class`
--	FOR EACH ROW SET new.creation_timestamp = NOW();
	
	
-- Add creation_time to keep track of when the user is created. 
-- For mysql 5.6, the default of the datetime can be set to CURRENT_TIMESTAMP.
ALTER TABLE user
    ADD COLUMN `creation_timestamp` datetime DEFAULT null;
    	
-- Create a trigger so that the user.creation_time will be automatically set to now.
-- This will minimize the impact to the java code since it is done at the db level. 
-- No need to create trigger. Already update the app to reflect creation_time.     	
-- CREATE TRIGGER `user_insert` BEFORE INSERT ON `user`
--	FOR EACH ROW SET new.creation_timestamp = NOW();

-- create last_modified_timestamp to the campaign table
ALTER TABLE campaign 
    ADD COLUMN (
       	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    );
	
-- create last_modified_timestamp to keep track of object relationship 	
ALTER TABLE user_class 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);
    	
ALTER TABLE campaign_class 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);

ALTER TABLE user_role_campaign 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);

ALTER TABLE document_user_role 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);
    	
ALTER TABLE document_user_creator 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);
   
ALTER TABLE document_campaign_role 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);

ALTER TABLE document_class_role 
    ADD COLUMN (
    	`last_modified_timestamp` timestamp DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    	);
   	    	
    	
-- In mysql 5.5, we can create `last_modified_timestamp timestamp NULL timesstamp DEFAULT CURRENT_TIEMSTAMP ... 
-- It will assisng the NULL value to the existing entries. However, mysql 5.6 will always 
-- assign the current timestamps to existing entries. To code againsts these two cases, explicitly assign 
-- 0 date to the existing entries.. Then exucute the rest of the script to update last_modified_timestamp.

UPDATE class SET last_modified_timestamp = 0;
UPDATE campaign SET last_modified_timestamp = 0;    	

UPDATE user_class SET last_modified_timestamp = 0;
UPDATE campaign_class SET last_modified_timestamp = 0;
UPDATE user_role_campaign SET last_modified_timestamp = 0;
UPDATE document_user_role SET last_modified_timestamp = 0;
UPDATE document_user_creator SET last_modified_timestamp = 0;
UPDATE document_campaign_role SET last_modified_timestamp = 0;
UPDATE document_class_role SET last_modified_timestamp = 0;

-- * Update class: update the creation_timestamp AND last_modified_timestamp of the class table       
-- If the timestamp of class/create is newer than class/update, use it for 
-- last_modified_timestamp as well. 
UPDATE class JOIN 
  (SELECT t1.urn AS urn,
         t1.creation_timestamp AS creation_timestamp,
         IF(t2.urn IS NULL OR t2.update_timestamp < t1.creation_timestamp, 
           t1.creation_timestamp, 
       	   t2.update_timestamp) AS update_timestamp
  FROM
    (SELECT ap.param_value AS urn, MAX(a.db_timestamp) AS creation_timestamp
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
     WHERE a.uri = '/app/class/create'
        AND a.response like '%success%'
        AND ap.param_key = 'class_urn'
     GROUP BY ap.param_value
    ) AS t1
  LEFT JOIN 
    (SELECT ap.param_value AS urn, max(a.db_timestamp) AS update_timestamp
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
        JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
     WHERE a.uri = '/app/class/update'
        AND a.response like '%success%'
        AND ap.param_key = 'class_urn'
	    AND ( ap2.param_key = 'class_name' OR ap2.param_key = 'description') 
     GROUP BY ap.param_value
    ) AS t2 ON (t1.urn = t2.urn)
  ) AS audit ON (class.urn = audit.urn)
SET class.creation_timestamp = audit.creation_timestamp,
    class.last_modified_timestamp = audit.update_timestamp
WHERE class.creation_timestamp IS NULL;

-- * update campaign last_modified_timestamp 
UPDATE campaign c JOIN 
  (SELECT ap.param_value AS urn, MAX(a.db_timestamp) AS last_modified_timestamp
   FROM audit a 
     JOIN audit_parameter ap ON (a.id = ap.audit_id)
     JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
   WHERE (a.uri = '/app/campaign/update')
      AND a.response like '%success%'
      AND ap.param_key = 'campaign_urn'
      AND ap2.param_key IN ('xml', 'description', 'privacy_state', 'running_state') 
   GROUP BY ap.param_value
  ) AS audit ON c.urn = audit.urn
SET c.last_modified_timestamp = audit.last_modified_timestamp
WHERE c.last_modified_timestamp = 0;


-- For the rest of the campaigns, derive from campaign creation time.
UPDATE campaign c
SET c.last_modified_timestamp = c.creation_timestamp 
WHERE c.creation_timestamp > c.last_modified_timestamp; 

-- * update user: update the last_modified_timestamp of the user table.
-- Delete this after last_modified_timestamp is restored
-- Derived FROM audit table
UPDATE user JOIN 
    (SELECT ap.param_value AS username, MAX(a.db_timestamp) AS last_modified_timestamp
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
     WHERE (a.uri = '/app/user/update' OR a.uri = '/app/user/create')
        AND a.response like '%success%'
        AND ap.param_key = 'username'
     GROUP BY ap.param_value
    ) AS audit ON user.username = audit.username
SET user.last_modified_timestamp = audit.last_modified_timestamp
WHERE user.last_modified_timestamp = 0;

-- Derived last_modified_timestamp from user_personal
UPDATE user u JOIN user_personal up ON (u.id = up.user_id)
SET u.last_modified_timestamp = up.last_modified_timestamp
WHERE u.last_modified_timestamp < up.last_modified_timestamp;

-- Derived from the class info
UPDATE user u JOIN
  (SELECT u.id as uid, c.id as cid, MAX(c.last_modified_timestamp) as ts
   FROM user u JOIN user_class uc on (u.id = uc.user_id)
   JOIN class c on (c.id = uc.class_id)
   GROUP BY u.id
  ) AS t1 on (u.id = t1.uid)
SET u.last_modified_timestamp = t1.ts
WHERE u.last_modified_timestamp = 0;

-- randomly assign 
UPDATE user u 
SET u.last_modified_timestamp = "2013-01-01 00:00:01"
WHERE u.last_modified_timestamp = 0;
  
-- * update user: update the creation_timestamp of the user table.
-- Derived FROM audit table
UPDATE user JOIN 
    (SELECT ap.param_value AS username, MAX(a.db_timestamp) AS creation_timestamp
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
     WHERE a.uri = '/app/user/create'
        AND a.response like '%success%'
        AND ap.param_key = 'username'
     GROUP BY ap.param_value
    ) AS audit ON user.username = audit.username
SET user.creation_timestamp = audit.creation_timestamp
WHERE user.creation_timestamp IS NULL;

-- Derived creation_timestamp from user/setup by using the min timestamp 
-- of the user setup call that happened after the latest user/delete was called.
UPDATE
  user u
	JOIN
    (SELECT u.id AS user_id, 
        ap.param_value AS first_name, ap2.param_value AS last_name, 
        ap3.param_value AS organization, ap4.param_value AS personal_id, 
        MIN(a.db_timestamp) AS creation_timestamp,
        MAX(a.db_timestamp) AS last_modified_timestamp
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
        JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
        JOIN audit_parameter ap3 ON (a.id = ap3.audit_id)
        JOIN audit_parameter ap4 ON (a.id = ap4.audit_id) 
        JOIN user_personal up ON (ap.param_value=up.first_name 
          AND ap2.param_value=up.last_name 
          AND ap3.param_value=up.organization 
          AND ap4.param_value=up.personal_id)
        JOIN user u ON (u.id = up.user_id)
        LEFT JOIN (
          SELECT u.id AS user_id, u.username AS username, 
            MAX(a.db_timestamp) as del_ts, ap.param_value
          FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
            JOIN user u on (u.username = ap.param_value)
          WHERE a.uri = '/app/user/delete'
            AND a.response LIKE '%success%'
            AND ap.param_key = 'user_list'
            AND ap.param_value RLIKE u.username
          GROUP BY u.id
        ) AS del on (u.id=del.user_id)
     WHERE a.uri = '/app/user/setup'
        AND a.response like '%success%'
        AND ap.param_key = 'first_name'
        AND ap2.param_key = 'last_name'
        AND ap3.param_key = 'organization'
        AND ap4.param_key = 'personal_id'
        AND (del.del_ts IS NULL or a.db_timestamp > del.del_ts)
     GROUP BY u.id
    ) AS audit ON (u.id = audit.user_id)
SET u.creation_timestamp = audit.creation_timestamp
WHERE u.creation_timestamp IS NULL;

-- For those that don't appear in the audit, use last_modified_timestamp
UPDATE user 
SET user.creation_timestamp = user.last_modified_timestamp 
WHERE user.creation_timestamp IS NULL;


-- * Deal with urn:class:public AND dohmage.admin
-- set creation_timestamp of "urn:class:public" to the 
-- earliest entry in the audit table
UPDATE class c, 
       (SELECT MIN(db_timestamp) AS ts FROM audit) AS a
SET c.creation_timestamp = a.ts
where c.urn = 'urn:class:public';

UPDATE class c
SET c.last_modified_timestamp = c.creation_timestamp
where c.urn = 'urn:class:public'
    AND c.last_modified_timestamp = 0; 

-- set the creation_timestamp of ohmage.admin to the 
-- earliest entry in the audit table.
UPDATE user u,
       (SELECT MIN(db_timestamp) AS ts FROM audit) AS a
SET u.creation_timestamp = a.ts
where u.username = 'ohmage.admin';

-- for other class with no info, use user or class creation_timestamp
UPDATE class c 
  JOIN (SELECT uc.class_id AS id, MIN(u.creation_timestamp) AS user_ts
        FROM user_class uc JOIN user u ON (uc.user_id = u.id)
        WHERE u.creation_timestamp > 0
	    GROUP BY uc.class_id) AS t1 ON (c.id = t1.id)
  JOIN (SELECT cc.class_id AS id, MIN(ca.creation_timestamp) AS ca_ts
        FROM campaign_class cc JOIN campaign ca ON (cc.campaign_id = ca.id)
        GROUP BY cc.class_id) AS t2 ON (c.id = t2.id)
SET c.creation_timestamp = IF(t1.user_ts < t2.ca_ts, t1.user_ts, t2.ca_ts)
where c.creation_timestamp IS NULL;


-- * update user_class: last_modified_timestamp

-- By looking when the users were last added through /app/class/update
UPDATE 
  user_class uc JOIN 
  	(SELECT uc.user_id AS user_id, uc.class_id AS class_id, uc.user_class_role_id AS user_class_role_id, MAX(t1.ts) AS max_ts,
  	   u.username AS username
  	 FROM user_class uc
  	   JOIN class c ON (uc.class_id = c.id)
       JOIN user u ON (uc.user_id = u.id)
       JOIN user_class_role ucr ON (uc.user_class_role_id = ucr.id)
       JOIN 
        (SELECT ap.param_value AS urn, ap2.param_value AS user_list, a.db_timestamp AS ts 
         FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
           JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
         WHERE a.uri = '/app/class/update'
       	   AND a.response like '%success%'
       	   AND ap.param_key = 'class_urn'
       	   AND ap2.param_key = 'user_role_list_add'
        ) AS t1 ON (c.urn = t1.urn AND t1.user_list RLIKE CONCAT(u.username, ";", ucr.role))
     GROUP BY uc.user_id, uc.class_id, uc.user_class_role_id
    ) AS src ON (uc.user_id=src.user_id AND uc.class_id=src.class_id AND uc.user_class_role_id=src.user_class_role_id)
SET uc.last_modified_timestamp = src.max_ts
WHERE src.max_ts > uc.last_modified_timestamp;

-- When a class was created /app/class/create, the requesting user is automatically added 
-- to the table 
UPDATE
  user_class uc JOIN 
  (SELECT uc.user_id AS user_id, uc.class_id AS class_id, MAX(t1.ts) as max_ts 
   FROM user_class uc JOIN class c ON (uc.class_id = c.id)
    JOIN user u ON (uc.user_id = u.id)
    JOIN 
    (SELECT ap.param_value AS urn, ae.extra_value AS username, a.db_timestamp AS ts 
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
       JOIN audit_extra ae ON (a.id = ae.audit_id)
     WHERE a.uri = '/app/class/create'
       AND a.response like '%success%'
       AND ap.param_key = 'class_urn'
       AND ae.extra_key = 'user'
     ) AS t1 ON (c.urn = t1.urn AND u.username = t1.username)
   ) AS src on (uc.user_id = src.user_id AND uc.class_id = src.class_id)
SET uc.last_modified_timestamp = src.max_ts
where src.max_ts > uc.last_modified_timestamp;

-- Derived FROM class.creation_timestamp or user.creation_timestamp whichever is newer
UPDATE 
user_class uc 
    JOIN class c ON (uc.class_id = c.id)
    JOIN user u ON (uc.user_id = u.id)
    JOIN user_class_role ucr ON (uc.user_class_role_id = ucr.id)
SET uc.last_modified_timestamp = IF(u.creation_timestamp > c.creation_timestamp, u.creation_timestamp, c.creation_timestamp)
WHERE uc.last_modified_timestamp = 0;


-- * update campaign_class: last_modified_timestamp

-- Derived FROM /app/campaign/update through class_list_add parameter
UPDATE 
  campaign_class cc JOIN 
  (SELECT cc.campaign_id AS campaign_id, cc.class_id AS class_id, MAX(t1.ts) AS max_ts, cc.last_modified_timestamp  
   FROM campaign_class cc JOIN class c ON (cc.class_id = c.id)
    JOIN campaign ca ON (cc.campaign_id = ca.id)
    JOIN 
    (SELECT ap.param_value AS urn, ap2.param_value AS class_list, a.db_timestamp AS ts
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
       JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
     WHERE a.uri = '/app/campaign/update'
       AND a.response like '%success%'
       AND ap.param_key = 'campaign_urn'
       AND ap2.param_key = 'class_list_add'
     ) AS t1 ON (ca.urn = t1.urn AND t1.class_list rlike c.urn)
   GROUP BY cc.campaign_id, cc.class_id
  ) AS src on (src.campaign_id = cc.campaign_id AND src.class_id = cc.class_id)
SET cc.last_modified_timestamp = src.max_ts
WHERE src.max_ts > cc.last_modified_timestamp;

-- Derived FROM /app/campaign/create
UPDATE 
  campaign_class cc JOIN 
  (SELECT cc.campaign_id AS campaign_id, cc.class_id AS class_id, MAX(t1.ts) AS max_ts, cc.last_modified_timestamp    
   FROM campaign_class cc JOIN class c ON (cc.class_id = c.id)
    JOIN campaign ca ON (cc.campaign_id = ca.id)
    JOIN 
    (SELECT ap.param_value AS urn, ap2.param_value AS class_list, a.db_timestamp AS ts
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
       JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
     WHERE a.uri = '/app/campaign/create'
       AND a.response like '%success%'
       AND ap.param_key = 'campaign_urn'
       AND ap2.param_key = 'class_urn_list'
     ) AS t1 ON (ca.urn = t1.urn AND t1.class_list rlike c.urn)
   GROUP BY cc.campaign_id, cc.class_id
  ) AS src on (src.campaign_id = cc.campaign_id AND src.class_id = cc.class_id)
SET cc.last_modified_timestamp = src.max_ts
WHERE src.max_ts > cc.last_modified_timestamp;

-- Derived FROM maximum timestamp between campaign AND class creation time
UPDATE 
  campaign_class cc
    JOIN class c ON (cc.class_id = c.id)
    JOIN campaign ca ON (cc.campaign_id = ca.id)
SET cc.last_modified_timestamp = IF(ca.creation_timestamp > c.creation_timestamp, ca.creation_timestamp, c.creation_timestamp)
WHERE cc.last_modified_timestamp = 0;

-- * update user_role_campaign: last_modified_timestamp
-- Derived from campaign_class last_modified_timestamp
UPDATE 
  user_role_campaign urc JOIN
  (SELECT urc.user_id as user_id, urc.campaign_id as campaign_id, 
     MAX(uc.last_modified_timestamp) AS uc_ts,
     MAX(cc.last_modified_timestamp) AS cc_ts
   FROM user_role_campaign urc 
     JOIN campaign_class cc ON (urc.campaign_id = cc.campaign_id)
     JOIN user_class uc ON (cc.class_id = uc.class_id)
   GROUP BY urc.user_id, urc.campaign_id
   ) AS src on (urc.user_id = src.user_id AND urc.campaign_id = src.campaign_id)
SET urc.last_modified_timestamp = 
  IF(src.cc_ts > src.uc_ts, src.cc_ts, src.uc_ts)
where src.cc_ts > urc.last_modified_timestamp
  OR src.uc_ts > urc.last_modified_timestamp;
  
-- campaign update
UPDATE 
  user_role_campaign urc JOIN 
  (SELECT urc.campaign_id AS campaign_id, urc.user_id AS user_id, urc.user_role_id AS user_role_id, 
     MAX(t1.ts) AS max_ts, urc.last_modified_timestamp  
   FROM user_role_campaign urc 
    JOIN campaign ca ON (urc.campaign_id = ca.id)
    JOIN user u ON (urc.user_id = u.id)
    JOIN user_role ur ON (urc.user_role_id = ur.id)
    JOIN 
    (SELECT ap.param_value AS urn, ap2.param_value AS user_list, MAX(a.db_timestamp) AS ts
     FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
       JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
     WHERE a.uri = '/app/campaign/update'
       AND a.response like '%success%'
       AND ap.param_key = 'campaign_urn'
       AND ap2.param_key = 'user_role_list_add'
     GROUP BY ap.param_value, ap2.param_value
     ) AS t1 ON (ca.urn = t1.urn AND t1.user_list rlike CONCAT(u.username,";",ur.role))
   GROUP BY u.id, ca.id, ur.id
  ) AS src on (src.campaign_id = urc.campaign_id AND src.user_id = urc.user_id AND src.user_role_id=urc.user_role_id)
SET urc.last_modified_timestamp = src.max_ts
WHERE src.max_ts > urc.last_modified_timestamp;

-- Some campaigns were detached from classes but still have the direct links to the users
-- In this case, use campaign last modified timestamp
UPDATE
  user_role_campaign urc 
  JOIN campaign c ON (urc.campaign_id = c.id)
  JOIN user u ON (urc.user_id = u.id)
SET urc.last_modified_timestamp = IF(u.creation_timestamp > c.creation_timestamp, u.creation_timestamp, c.creation_timestamp)
WHERE urc.last_modified_timestamp = 0;


-- * update document_user_role, document_class_role, document_campaign_role
-- Need to check how this table is updated
 
-- document_user_role: update document_user_role with document's creation_timestamp
UPDATE
  document_user_role dur 
  JOIN document d ON (dur.document_id = d.id)
SET dur.last_modified_timestamp = d.creation_timestamp
WHERE dur.last_modified_timestamp = 0;

 -- document_user_creator: update using document's creation_timestamp
 -- Those that were not set referenced to deleted document
UPDATE
  document_user_creator duc 
  JOIN document d ON (duc.document_id = d.id)
SET duc.last_modified_timestamp = d.creation_timestamp
WHERE duc.last_modified_timestamp = 0;

-- document_class_role 
-- First: try to derive from the audit logs
UPDATE
  document_class_role dcr JOIN
  (SELECT dcr.document_id as document_id, dcr.class_id as class_id, 
     dcr.document_role_id as document_role_id, MAX(t1.ts) as max_ts
   FROM  document_class_role dcr
     JOIN document_role dr ON (dcr.document_role_id = dr.id)
     JOIN document d ON (dcr.document_id = d.id)
     JOIN class c ON (dcr.class_id = c.id)
     JOIN 
     (SELECT ap.param_value as uuid, ap2.param_value as class_list, a.db_timestamp as ts
      FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
        JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
      WHERE a.uri = '/app/document/update'
        AND a.response like '%success%'
        AND ap.param_key = 'document_id'
        AND ap2.param_key = 'class_role_list_add'
     ) AS t1 on (t1.uuid = d.uuid AND t1.class_list like CONCAT(c.urn, ";", dr.role))
   GROUP BY dcr.document_id, dcr.class_id, dcr.document_role_id
  ) AS src on (src.document_id = dcr.document_id 
  	AND src.class_id=dcr.class_id 
  	AND src.document_role_id = dcr.document_role_id)
SET dcr.last_modified_timestamp = src.max_ts
WHERE src.max_ts > dcr.last_modified_timestamp;

-- The rest: update the rest using document creation_timestamp
UPDATE
  document_class_role dcr 
  JOIN document d ON (dcr.document_id = d.id)
SET dcr.last_modified_timestamp = d.creation_timestamp
WHERE dcr.last_modified_timestamp = 0;

-- document_campaign_role: 
-- First, derive the timestamp from the audit logs
UPDATE
  document_campaign_role dcr JOIN
  (SELECT dcr.document_id as document_id, dcr.campaign_id as campaign_id, 
     dcr.document_role_id as document_role_id, MAX(t1.ts) as max_ts
   FROM  document_campaign_role dcr
     JOIN document_role dr ON (dcr.document_role_id = dr.id)
     JOIN document d ON (dcr.document_id = d.id)
     JOIN campaign c ON (dcr.campaign_id = c.id)
     JOIN 
     (SELECT ap.param_value as uuid, ap2.param_value as campaign_list, a.db_timestamp as ts
      FROM audit a JOIN audit_parameter ap ON (a.id = ap.audit_id)
        JOIN audit_parameter ap2 ON (a.id = ap2.audit_id)
      WHERE a.uri = '/app/document/update'
        AND a.response like '%success%'
        AND ap.param_key = 'document_id'
        AND ap2.param_key = 'campaign_role_list_add'
     ) AS t1 on (t1.uuid = d.uuid AND t1.campaign_list like CONCAT(c.urn, ";", dr.role))
   GROUP BY dcr.document_id, dcr.campaign_id, dcr.document_role_id
  ) AS src on (src.document_id = dcr.document_id 
  	AND src.campaign_id=dcr.campaign_id 
  	AND src.document_role_id = dcr.document_role_id)
SET dcr.last_modified_timestamp = src.max_ts
WHERE src.max_ts > dcr.last_modified_timestamp;

-- update the rest using document creation_timestamp
UPDATE
  document_campaign_role dcr 
  JOIN document d ON (dcr.document_id = d.id)
SET dcr.last_modified_timestamp = d.creation_timestamp
WHERE dcr.last_modified_timestamp = 0;


-- * remove unintentional user_role_campaign entries resulting in the issue that when 
-- a class is deleted, the user_role_campaign isn't updated properly. This issue is 
-- fixed in 2.17. 

-- delete the relationship between user and campaigns where the campaign is
-- orphaned (eg. no class associated with the campaign). Delete all roles
-- except "author". 
DELETE FROM user_role_campaign
WHERE id IN (
  SELECT id FROM (
    SELECT urc.id AS id
    FROM user_role_campaign urc
      JOIN campaign cp on (urc.campaign_id = cp.id)
      JOIN user u on (urc.user_id = u.id)
      JOIN user_role ur on (urc.user_role_id = ur.id)
      LEFT JOIN campaign_class cc on (urc.campaign_id = cc.campaign_id)
    WHERE
      ur.role IN ("supervisor", "analyst", "participant")
      AND cc.id IS NULL
    GROUP BY u.id, cp.id, ur.id
  ) t1
)

-- delete the relationship between user and campaigns where the campaigns are in
-- classes that users are not members of. Delete all roles except "author". 
DELETE FROM user_role_campaign
WHERE id IN (
  SELECT id FROM (
    SELECT urc.id as id
    FROM user_role_campaign urc
      JOIN campaign cp on (urc.campaign_id = cp.id)
      JOIN user u on (urc.user_id = u.id)
      JOIN user_role ur on (urc.user_role_id = ur.id)
      JOIN campaign_class cc on (urc.campaign_id = cc.campaign_id)
      LEFT JOIN user_class uc on (urc.user_id = uc.user_id and cc.class_id = uc.class_id)
    WHERE
      ur.role IN ("supervisor", "analyst", "participant")
    GROUP BY u.id, cp.id, ur.id
    HAVING count(cc.class_id) - SUM(IF(uc.class_id IS NULL, 1, 0)) = 0
  ) t1
) 


