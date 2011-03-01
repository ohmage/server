-- ------------------------------------------------------------------------
-- Revisions to the Ganz prompts - the only prompts in the system currently
-- ------------------------------------------------------------------------

DELETE FROM prompt_response;
UPDATE prompt SET parent_config_id = null;
DELETE from prompt;

UPDATE prompt_type SET restriction = '{0:"<10",1:"10-20",2:"20-30",3:"30-40",4:"40-50",5:"50+"}' WHERE id = 3;
UPDATE prompt_type SET restriction = '{0:0,1:1,2:2,3:3,4:4,5:5,6:6,7:7,8:8,9:9,10:10,11:11,12:12}' WHERE id = 4;
UPDATE prompt_type SET restriction = '{0:"very bad",1:"bad",2:"good",3:"very good"}' WHERE id = 5;
UPDATE prompt_type SET restriction = '{0:"not at all",1:"slightly",2:"moderately",3:"extremely"}' WHERE id = 6;
UPDATE prompt_type SET restriction = '{0:"rarely",1:"sometimes",2:"often"}' WHERE id = 7;
UPDATE prompt_type SET restriction = '{0:"No",1:"Yes"}' WHERE id = 10; -- just a JSON cleanup, not a data change

INSERT INTO prompt_type (type, restriction) VALUES ("map", '{0:"not at all",1:"slightly",2:"moderately",3:"very"}');


INSERT INTO prompt (prompt_type_id, campaign_prompt_group_id, campaign_prompt_version_id, prompt_config_id, parent_config_id, question_text, legend_text) VALUES
  (1, 1, 1, 0, NULL, "Take a saliva sample now and enter time.", "saliva"),
  (2, 1, 1, 1, NULL, "In the 20 minutes before this sample, did you brush your teeth, eat, drink?", "brush,eat,drink"),
  
  (1, 2, 1, 0, NULL, "Bed time last night?", "bedtime"),
  (3, 2, 1, 1, NULL, "How many minutes did it take you to fall asleep last night?","time to fall asleep"),
  (1, 2, 1, 2, NULL, "Wake up time this morning?","wakeup time"),
  (4, 2, 1, 3, NULL, "How many hours of actual sleep did you get?","hours of sleep"),
  (5, 2, 1, 4, NULL, "How would you rate your sleep quality?","sleep quality"),
  
  (12, 3, 1, 0, NULL, "For the following, please rate how much you are currently feeling:","currently feeling "),
  (6, 3, 1, 1, 0, "Sad","feeling sad"),
  (6, 3, 1, 2, 0, "Relaxed","feeling relaxed"),
  (6, 3, 1, 3, 0, "Anxious","feeling anxious"),
  (6, 3, 1, 4, 0, "Tired","feeling tired"),
  (6, 3, 1, 5, 0, "Happy","feeling happy"),
  (6, 3, 1, 6, 0, "Upset","feeling upset"),
  (6, 3, 1, 7, 0, "Energetic","feeling energetic"),
  (6, 3, 1, 8, 0, "Irritable","feeling irritable"),
  (6, 3, 1, 9, 0, "Calm","feeling calm"),
  
  (12, 4, 1, 0, NULL, "Did you experience any of the following today:","experiences"),
  (10, 4, 1, 1, 0, "Little interest or pleasure in doing things?","little interest or pleasure"),
  (10, 4, 1, 2, 0, "Feeling down, depressed, or hopeless?","feeling down, depressed, or hopeless"),
  (10, 4, 1, 3, 0, "Feeling bad about yourself, feeling that you are a failure, or feeling that you have let yourself or your family down?","feeling bad, like a failure, let family or friends down"),
  (7, 4, 1, 4, NULL, "How often did you feel that you were unable to control the important things in your life today?","unable to control important things"),
  (7, 4, 1, 5, NULL, "How often did you feel confident about your ability to handle your personal problems today?","confident in handling personal problems"),
  (7, 4, 1, 6, NULL, "How often did you feel that things were going your way today?","things going your way"),
  (7, 4, 1, 7, NULL, "How often did you find that you could not cope with all the things that you had to do today?","cope with things you had to do"),
  (7, 4, 1, 8, NULL, "How often did you feel difficulties were piling up so high that you could not overcome them?","difficulties too high to overcome"),
  (10, 4, 1, 9, NULL, "Did you exercise today?","did you exercise"),
  (8, 4, 1, 10, 9, "If yes, what type of exercise did you today?","type of exercise"),
  (13, 4, 1, 11, 9, "If you exercised, for how many minutes did you exercise?","how many minutes did you exercise"),
  (14, 4, 1, 12, 9, "If you exercised, did you enjoy exercising?","exercise enjoyment"),
  (11, 4, 1, 13, NULL, "If you didn't exercise, why not? (lack of time, lack of self-discipline, fatigue, procrastination, lack of interest, family work or responsibilities,","exercise interference"),
  (9, 4, 1, 14, NULL, "How many alcoholic beverages did you have today?","number of alcoholic beverages"),
  (9, 4, 1, 15, NULL, "How many caffeinated beverages did you have today?","number of caffeinated beverages"),
  (10, 4, 1, 16, NULL, "Did you have any high sugar food or drinks today? (soft drinks, candy, etc)","sugary food or drinks"),
  (10, 4, 1, 17, NULL, "Did anything happen today that was stressful or difficult for you?","did anything stressful happen"),
  (14, 4, 1, 18, 17, "If so, how stressful was this?","how stressful"),
  (10, 4, 1, 19, NULL, "Did anything happen today that was enjoyable or felt good to you?","did anything enjoyable happen"),
  (14, 4, 1, 20, 19, "If so, how enjoyable was this?","how enjoyable");
  