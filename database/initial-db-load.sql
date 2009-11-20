-- Populate the initially empty andwellness database.
-- This SQL is intended to be run after andwellness-ddl.sql.

INSERT INTO configuration (json_data) VALUES ('{"name":"root"}');

INSERT INTO campaign (name, label, subdomain, configuration_id) VALUES ('ganz', 'The Ganz pilot', 'pilot', 1);

INSERT INTO prompt (p_text, parent_id)VALUES ('Enter the sample time', NULL), 
    ('In the twenty minutes before this sample did you brush your teeth?', NULL),
    ('In the twenty minutes before this sample did you eat?', NULL),
    ('In the twenty minutes before this sample did you drink?', NULL),
    ('Bedtime last night?', NULL),
    ('How long did it take you to fall asleep last night?', NULL),
    ('Wake up time this morning?', NULL),
    ('How many actual hours of sleep did you get?', NULL),
    ('How would you rate your sleep quality?', NULL),
    ('Please rate how you are currently feeling.', NULL),
    ('Did you experience the following today: little interest or pleasure in doing things?', NULL),
    ('Did you experience the following today: feeling down, depressed, or hopeless?', NULL),
    ('Did you experience the following today: feeling bad about yourself, feeling that you are a failure, or felling that you have let yourself or your family down?', NULL),
    ('How often did you feel that you were unable to control the important things in your life today?', NULL),
    ('How often did you feel confident about your ability to handle your personal problems today?', NULL),
    ('How often did you feel that things were going your way today?', NULL),
    ('How often did you find that you could not cope with all the things that you had to do today?', NULL),
    ('How often did you feel difficulties were piling up so high that you could not overcome them?', NULL),
    ('What type of exercise did you today?', NULL),
    ('How long did you exercise?', NULL),
    ('Did you plan to exercise today?', NULL),
    ('Did any of the following interfere with your plan to exercise today?', NULL),
    ('How many cigarettes did you smoke today?', NULL),
    ('How many alcoholic beverages did you have today?', NULL),
    ('How many caffeinated beverages did you have today?', NULL),
    ('Did you have any high sugar food or drinks today? (soft drinks, candy, etc)', NULL),
    ('Did anything happen today that was stressful or difficult for you?', NULL),
    ('Did anything happen today that was enjoyable or felt good to you?', NULL);
    
INSERT INTO prompt (p_text, parent_id)
  SELECT 'If so, how stressful was this?', id 
  FROM prompt 
  WHERE p_text = 'Did anything happen today that was stressful or difficult for you?';

INSERT INTO prompt (p_text, parent_id)
  SELECT 'If so, how enjoyable was this?', id 
  FROM prompt 
  WHERE p_text = 'Did anything happen today that was enjoyable or felt good to you?';
  
INSERT INTO user_role (label) VALUES ('admin'), ('participant'), ('health worker');

INSERT INTO user (email_address, json_data) VALUES ('selsky@cens.ucla.edu', '{"first_name":"Joshua","last_name":"Selsky"}');

INSERT INTO user_role_campaign (user_id, campaign_id, user_role_id) VALUES (1, 1, 1);






