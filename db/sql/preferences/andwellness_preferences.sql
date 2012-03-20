-- Preferences for an AndWellness deployment.
INSERT INTO preference(p_key, p_value) 
VALUES 
    ('default_can_create_privilege', 'false'), 
    ('default_survey_response_sharing_state', 'private'),
    ('privileged_user_in_class_can_view_others_mobility', 'true'),
    ('mobility_enabled', 'true'),
    ('self_registration_allowed', 'true');