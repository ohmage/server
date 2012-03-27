-- A default set of preferences for a generic ohmage deployment.
INSERT INTO preference(p_key, p_value) 
VALUES 
    ('default_can_create_privilege', 'true'), 
    ('default_survey_response_sharing_state', 'private'),
    ('privileged_user_in_class_can_view_others_mobility', 'false'),
    ('mobility_enabled', 'true'),
    ('self_registration_allowed', 'true'),
    ('terms_of_service', ''),
    ('mail_sender_address', 'no-reply@ohmage.org'),
    ('mail_subject', 'ohmage: New Account Request'),
    ('mail_text', 'Registration Activation\n\nThank you for creating an account. To activate your account, follow the link at the end of this message. By following the link you agree to our terms of service.\n\n<_TOS_>\n\n<_REGISTRATION_LINK_>');