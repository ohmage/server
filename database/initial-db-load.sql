-- Populate the initially empty andwellness database.
-- This SQL is intended to be run after andwellness-ddl.sql.

INSERT INTO configuration (json_data) VALUES ('{"name":"root"}');

INSERT INTO campaign (name, label, subdomain, configuration_id) VALUES ('ganz', 'The Ganz pilot', 'pilot', 1);

INSERT INTO user_role (label) VALUES ('admin'), ('participant'), ('health worker');

INSERT INTO user (login_id) VALUES ("abbe.zoom"); -- login ids are generated from a dictionary file

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
  ("map", "{1:10,2:20,3:30,4:40,5:50,6:60+}"),
  ("integer", "0,24"),
  ("map", "{0:very bad,1:ok,2:good,3:very good}"),
  ("map", "{0:not at all,1:...,2:slightly,3:...,:moderately,5:...,6:extremely}"),
  ("map", "{0:never,1:almost never,2:sometimes,3:fairly often,4:very often}"),
  ("map", "{0:none,1:light,2:moderate,3:vigorous}"),
  ("map", "{0:1,1:2,2:3,3:4,4:5,5:6,6:7,7:8,8:9,9:10+}"),
  ("map", "{1:yes,0:no}"),
  ("array_boolean", "6"),
  ("null", NULL);

INSERT INTO prompt (prompt_type_id, campaign_prompt_group_id, campaign_prompt_version_id, prompt_config_id, parent_config_id, question_text, legend_text) VALUES
  (1, 1, 1, 0, NULL, "Take a saliva sample now and enter time.", "saliva"),
  (2, 1, 1, 1, NULL, "In the 20 minutes before this sample, did you brush your teeth, eat, drink?", "brush,eat,drink"),
  (1, 2, 1, 0, NULL, "What time did you go to bed last night?", "bedtime"),
  (3, 2, 1, 1, NULL, "How long did it take for you to fall asleep last night?","time to fall asleep"),
  (1, 2, 1, 2, NULL, "What time did you wake up this morning?","wakeup time"),
  (4, 2, 1, 3, NULL, "How many hours of actual sleep did you get?","hours of sleep"),
  (5, 2, 1, 4, NULL, "How would you rate your sleep quality overall?","sleep quality"),
  (6, 3, 1, 0, NULL, "Currently, are you feeling sad?","feeling sad"),
  (6, 3, 1, 1, NULL, "Currently, are you feeling blue?","feeling blue"),
  (6, 3, 1, 2, NULL, "Currently, are you feeling anxious?","feeling anxious"),
  (6, 3, 1, 3, NULL, "Currently, are you feeling nervous?","feeling nervous"),
  (6, 3, 1, 4, NULL, "Currently, are you feeling upset?","feeling upset"),
  (6, 3, 1, 5, NULL, "Currently, are you feeling stressed?","feeling stressed"),
  (6, 3, 1, 6, NULL, "Currently, are you feeling happy?","feeling happy"),
  (6, 3, 1, 7, NULL, "Currently, are you feeling cheerful?","feeling cheerful"),
  (6, 3, 1, 8, NULL, "Currently, are you feeling inspired?","feeling inspired"),
  (6, 3, 1, 9, NULL, "Currently, are you feeling enthusiastic?","feeling enthusiastic"),
  (6, 3, 1, 10, NULL, "Currently, are you feeling relaxed?","feeling relaxed"),
  (6, 3, 1, 11, NULL, "Currently, are you feeling calm?","feeling calm"),
  (6, 3, 1, 12, NULL, "Currently, are you feeling tired?","feeling tired"),
  (6, 3, 1, 13, NULL, "Currently, are you feeling energetic?","feeling energetic"),
  (6, 3, 1, 14, NULL, "Currently, does your body feel heavy or weak?","body feels heavy or weak"),
  (6, 3, 1, 15, NULL, "Currently, are you having difficulty concentrating?","difficulty concentrating"),
  (2, 4, 1, 0, NULL, "Did you experience and of the following today? Little interest or pleasure in doing things? Feeling down, depressed, or hopeless? Feeling bad about your self, feeling that you are a failure, or feeling that you have let yourself or your family down?","negative feelings"),
  (7, 4, 1, 1, NULL, "How often did you feel that you were unable to control the important things in your life today?","important things"),
  (7, 4, 1, 2, NULL, "How often did you feel confident about your ability to handle your personal problems today?","personal problems"),
  (7, 4, 1, 3, NULL, "How often did you feel that things were going your way today?","going your way"),
  (7, 4, 1, 4, NULL, "How often did you find that you could not cope with all the things that you had to do today?","cope with things"),
  (7, 4, 1, 5, NULL, "How often did you feel difficulties were piling up so high that you could not overcome them?","difficulties"),
  (10, 4, 1, 6, NULL, "Did you plan to exercise today?","exercise plan"),
  (8, 4, 1, 7, NULL, "What type of exercise did you today?","exercise"),
  (3, 4, 1, 8, NULL, "How long did you exercise?","exercise time"),
  (11, 4, 1, 9, NULL, "Did any of the following interfere with your plan to exercise today: lack of time? lack of self-discipline? fatigue? procrastination? lack of interest? family work or responsibilities?","exercise interference"),
  (9, 4, 1, 10, NULL, "How many cigarettes did you smoke today?","cigarettes"),
  (9, 4, 1, 11, NULL, "How many alcoholic beverages did you have today?","alcoholic beverages"),
  (9, 4, 1, 12, NULL, "How many caffeinated beverages did you have today?","caffeinated beverages"),
  (9, 4, 1, 13, NULL, "Did you have any high sugar food or drinks today? (soft drinks, candy, etc)","sugar"),
  (9, 4, 1, 14, NULL, "Did anything happen today that was stressful or difficult for you?","stressful/difficult"),
  (6, 4, 1, 15, 14, "If so, how stressful was this?","how stressful"),
  (9, 4, 1, 16, NULL, "Did anything happen today that was enjoyable or felt good to you?","enjoyable"),
  (6, 4, 1, 17, 16, "If so, how enjoyable was this?","how enjoyable");
  