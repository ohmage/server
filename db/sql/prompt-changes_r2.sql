-- ------------------------------------------------------------------------
-- Thge second revision to the Ganz prompts - 5/7/10 
-- ------------------------------------------------------------------------
  
DELETE FROM prompt_response;

UPDATE prompt SET parent_config_id = null;

DELETE from prompt;

INSERT INTO campaign_prompt_group (campaign_id, campaign_prompt_version_id, group_id, group_name) VALUES  (1, 1, 4, "Emotional State (End of Day)");

INSERT INTO prompt (prompt_type_id, campaign_prompt_group_id, campaign_prompt_version_id, prompt_config_id, parent_config_id, question_text, legend_text) VALUES
  (1, 1, 1, 0, NULL, "Take a saliva sample now and enter time.", "saliva"),
  (2, 1, 1, 1, NULL, "In the 20 minutes before this sample, did you brush your teeth, eat, drink?", "brush,eat,drink"),
  
  (1, 2, 1, 0, NULL, "Bed time last night?", "bedtime"),
  (3, 2, 1, 1, NULL, "How many minutes did it take you to fall asleep last night?","time to fall asleep"),
  (1, 2, 1, 2, NULL, "Wake up time this morning?","wakeup time"),
  (4, 2, 1, 3, NULL, "How many hours of actual sleep did you get?","hours of sleep"),
  (5, 2, 1, 4, NULL, "How would you rate your sleep quality?","sleep quality"),
  
  (12, 3, 1, 0, NULL, "Please rate how much you are currently feeling:","currently feeling "),
  (6, 3, 1, 1, 0, "Sad","feeling sad"),
  (6, 3, 1, 2, 0, "Relaxed","feeling relaxed"),
  (6, 3, 1, 3, 0, "Anxious","feeling anxious"),
  (6, 3, 1, 4, 0, "Tired","feeling tired"),
  (6, 3, 1, 5, 0, "Happy","feeling happy"),
  (6, 3, 1, 6, 0, "Upset","feeling upset"),
  (6, 3, 1, 7, 0, "Energetic","feeling energetic"),
  (6, 3, 1, 8, 0, "Irritable","feeling irritable"),
  (6, 3, 1, 9, 0, "Calm","feeling calm"),
  -- new
  (6, 3, 1, 10, 0, "Enjoyment/fun","enjoyment/fun"),
  
  -- new/reordered
  (14, 4, 1, 0, NULL, "Overall, how stressed did you feel today?","how stressed"),
  (12, 4, 1, 1, NULL, "Did you experience any of the following today:","experiences"),
  (10, 4, 1, 2, 1, "Little interest or pleasure in doing things?","little interest or pleasure"),
  (10, 4, 1, 3, 1, "Feeling down, depressed, or hopeless?","feeling down, depressed, or hopeless"),
  (10, 4, 1, 4, 1, "Feeling bad about yourself, feeling that you are a failure, or feeling that you have let yourself or your family down?","feeling bad, like a failure, let family or friends down"),
  (10, 4, 1, 5, NULL, "Did you exercise today?","did you exercise"),
  (8, 4, 1, 6, 5, "If yes, what type of exercise did you today?","type of exercise"),
  (13, 4, 1, 7, 5, "If you exercised, for how many minutes did you exercise?","how many minutes did you exercise"),
  (14, 4, 1, 8, 5, "If you exercised, did you enjoy exercising?","exercise enjoyment"),
  (11, 4, 1, 9, 5, "If you didn't exercise, why not? (lack of time, family work or responsibilities, fatigue, procrastination, lack of interest, lack of self-discipline","exercise interference"),
  (9, 4, 1, 10, NULL, "How many alcoholic beverages did you have today?","number of alcoholic beverages"),
  (9, 4, 1, 11, NULL, "How many caffeinated beverages did you have today?","number of caffeinated beverages"),
  (10, 4, 1, 12, NULL, "Did you have any high sugar food or drinks today? (soft drinks, candy, etc)","sugary food or drinks"),
  
  -- new
  (12, 5, 1, 0, NULL, "Please rate how much you felt each of the following today:","feeling today"),
  (6, 5, 1, 1, 0, "Sad","feeling sad"),
  (6, 5, 1, 2, 0, "Relaxed","feeling relaxed"),
  (6, 5, 1, 3, 0, "Anxious","feeling anxious"),
  (6, 5, 1, 4, 0, "Tired","feeling tired"),
  (6, 5, 1, 5, 0, "Happy","feeling happy"),
  (6, 5, 1, 6, 0, "Upset","feeling upset"),
  (6, 5, 1, 7, 0, "Energetic","feeling energetic"),
  (6, 5, 1, 8, 0, "Irritable","feeling irritable"),
  (6, 5, 1, 9, 0, "Calm","feeling calm"),
  (6, 5, 1, 10, 0, "Enjoyment/fun","enjoyment/fun");
  