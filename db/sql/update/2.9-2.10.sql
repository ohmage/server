-- Creates an index on prompt responses for the first 36 characters, the size 
-- of a UUID. This greatly increases the response time of image/read with a 
-- little memory overhead.
CREATE INDEX response_image ON prompt_response (response(36));