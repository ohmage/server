-- get public class id
SET @public_class_id =
  (SELECT id
   FROM class
   WHERE urn IN
       (SELECT p_value
        FROM preference
        WHERE p_key="public_class_id"));

-- perhaps the analyst role isn't id=3 ?
SET @analyst_role_id = 
  (SELECT id
    FROM user_role
    WHERE role = "analyst");

-- add users who are currently *only* "participants" in public campaigns
-- as "analysts" as well. so they can see shared data.
INSERT INTO user_role_campaign (user_id, campaign_id, user_role_id)
SELECT user_id,
       campaign_id,
       @analyst_role_id
FROM user_role_campaign
WHERE campaign_id IN
    (SELECT campaign.id
     FROM campaign
     JOIN campaign_class ON campaign.id = campaign_class.campaign_id
     WHERE campaign_class.class_id = @public_class_id)
GROUP BY user_id,
         campaign_id 
HAVING sum(user_role_id) = 1;