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
);

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

