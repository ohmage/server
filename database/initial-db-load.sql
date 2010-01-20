-- Populate the initially empty andwellness database.
-- This SQL is intended to be run after andwellness-ddl.sql.

INSERT INTO configuration (json_data) VALUES ('{"name":"root"}');

INSERT INTO campaign (name, label, subdomain, configuration_id) VALUES ('ganz', 'The Ganz pilot', 'pilot', 1);

INSERT INTO user_role (label) VALUES ('admin'), ('participant'), ('health worker');

INSERT INTO user (login_id, enabled) VALUES ("abbe.zoom", true); -- login ids are programmatically generated from a dictionary file

INSERT INTO user_personal (email_address, json_data) VALUES ('selsky@cens.ucla.edu', '{"first_name":"Joshua","last_name":"Selsky"}');

INSERT INTO user_user_personal (user_id, user_personal_id) VALUES (1, 1);

INSERT INTO user_role_campaign (user_id, campaign_id, user_role_id) VALUES (1, 1, 1);

INSERT INTO campaign_prompt_version (campaign_id, version_id) VALUES (1, 1);

INSERT INTO campaign_prompt_group (campaign_id, campaign_prompt_version_id, group_id, group_name) VALUES 
  (1, 1, 0, "Saliva"),
  (1, 1, 1, "Sleep"),
  (1, 1, 2, "Emotional State"),
  (1, 1, 3, "Diary");

INSERT INTO prompt_type (type, restriction) VALUES
  ("time_military", NULL),
  ("array_boolean", "3"),
  ("map", "{0:10,1:20,2:30,3:40,4:50,5:60+}"),
  ("map", "{0:<4,1:5,2:6,3:7,4:8,5:>8}"),
  ("map", "{0:very bad,1:,2:,3:very good}"),
  ("map", "{0:not at all,1:,2:slightly,3:,:moderately,5:,6:extremely}"),
  ("map", "{0:never,1:almost never,2:sometimes,3:fairly often,4:very often}"),
  ("map", "{0:none,1:light,2:moderate,3:vigorous}"),
  ("map", "{0:1,1:2,2:3,3:4,4:5,5:6,6:7,7:8,8:9,9:10+}"),
  ("map", "{1:Yes,0:No}"),
  ("array_boolean", "6"),
  ("null", NULL),
  ("map", "{0:10,1:20,2:30,3:40,4:50,5:60+,6:N/A}");

INSERT INTO prompt (prompt_type_id, campaign_prompt_group_id, campaign_prompt_version_id, prompt_config_id, parent_config_id, question_text, legend_text) VALUES
  (1, 1, 1, 0, NULL, "Take a saliva sample now and enter time.", "saliva"),
  (2, 1, 1, 1, NULL, "In the 20 minutes before this sample, did you brush your teeth, eat, drink?", "brush,eat,drink"),
  
  (1, 2, 1, 0, NULL, "Bed time last night?", "bedtime"),
  (3, 2, 1, 1, NULL, "How many minutes did it take you to fall asleep last night?","time to fall asleep"),
  (1, 2, 1, 2, NULL, "Wake up time this morning?","wakeup time"),
  (4, 2, 1, 3, NULL, "How many hours of actual sleep did you get?","hours of sleep"),
  (5, 2, 1, 4, NULL, "How would you rate your sleep quality?","sleep quality"),
  
  (12, 3, 1, 0, NULL, "For the following, please rate how much you are currently feeling:","currently feeling "),
  (6, 3, 1, 1, 0, "Sad?","feeling sad"),
  (6, 3, 1, 2, 0, "Inspired?","feeling inspired"),
  (6, 3, 1, 3, 0, "Calm?","feeling calm"),
  (6, 3, 1, 4, 0, "Upset?","feeling upset"),
  (6, 3, 1, 5, 0, "Nervous?","feeling nervous"),
  (6, 3, 1, 6, 0, "Energetic?","feeling energetic"),
  (6, 3, 1, 7, 0, "Happy?","feeling happy"),
  (6, 3, 1, 8, 0, "Blue?","feeling blue"),
  (6, 3, 1, 9, 0, "Anxious","feeling anxious"),
  (6, 3, 1, 10, 0, "Relaxed?","feeling relaxed"),
  (6, 3, 1, 11, 0, "Tired?","feeling tired"),
  (6, 3, 1, 12, 0, "Cheerful","feeling cheerful"),
  (6, 3, 1, 13, 0, "Enthusiastic?","feeling enthusiastic"),
  (6, 3, 1, 14, 0, "Stressed?","feeling stressed"),
  
  (12, 4, 1, 0, NULL, "Did you experience any of the following today:","experiences"),
  (10, 4, 1, 1, 0, "Little interest or pleasure in doing things?","little interest or pleasure"),
  (10, 4, 1, 2, 0, "Feeling down, depressed, or hopeless?","feeling down, depressed, or hopeless"),
  (10, 4, 1, 3, 0, "Feeling bad about yourself, feeling that you are a failure, or feeling that you have let yourself or your family down?","feeling bad, like a failure, let family or friends down"),
  (7, 4, 1, 4, NULL, "How often did you feel that you were unable to control the important things in your life today?","unable to control important things"),
  (7, 4, 1, 5, NULL, "How often did you feel confident about your ability to handle your personal problems today?","confident in handling personal problems"),
  (7, 4, 1, 6, NULL, "How often did you feel that things were going your way today?","things going your way"),
  (7, 4, 1, 7, NULL, "How often did you find that you could not cope with all the things that you had to do today?","cope with things you had to do"),
  (7, 4, 1, 8, NULL, "How often did you feel difficulties were piling up so high that you could not overcome them?","difficulties too high to overcome"),
  (10, 4, 1, 9, NULL, "Did you plan to exercise today?","did you plan to exercise"),
  (10, 4, 1, 10, NULL, "Did you exercise today?","did you exercise"),
  (8, 4, 1, 11, 10, "If yes, what type of exercise did you today?","type of exercise"),
  (13, 4, 1, 12, 10, "If you exercised, for how many minutes did you exercise?","how many minutes did you exercise"),
  (11, 4, 1, 13, NULL, "Did any of the following interfere with your plan to exercise today? (lack of time, lack of self-discipline, fatigue, procrastination, lack of interest, family work or responsibilities,","exercise interference"),
  (9, 4, 1, 14, NULL, "How many cigarettes did you smoke today?","number of cigarettes"),
  (9, 4, 1, 15, NULL, "How many alcoholic beverages did you have today?","number of alcoholic beverages"),
  (9, 4, 1, 16, NULL, "How many caffeinated beverages did you have today?","number of caffeinated beverages"),
  (10, 4, 1, 17, NULL, "Did you have any high sugar food or drinks today? (soft drinks, candy, etc)","sugary food or drinks"),
  (10, 4, 1, 18, NULL, "Did anything happen today that was stressful or difficult for you?","did anything stressful happen"),
  (6, 4, 1, 19, 18, "If so, how stressful was this?","how stressful"),
  (10, 4, 1, 20, NULL, "Did anything happen today that was enjoyable or felt good to you?","did anything enjoyable happen"),
  (6, 4, 1, 21, 20, "If so, how enjoyable was this?","how enjoyable");
  