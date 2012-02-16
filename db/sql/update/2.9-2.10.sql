-- Creates an index on prompt responses for the first 36 characters, the size 
-- of a UUID. This greatly increases the response time of image/read with a 
-- little memory overhead.
CREATE INDEX response_image ON prompt_response (response(36));

-- Add the preference defining the number of survey responses. The default is
-- -1 which represents "no limit".
INSERT INTO preference(p_key, p_value) VALUES 
    ('max_survey_response_page_size', '-1');