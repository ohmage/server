-- Adds the preferences to the preferences table.
INSERT INTO preference(p_key, p_value) VALUES 
    ('document_directory', '/opt/ohmage/userdata/documents'), 
    ('image_directory', '/opt/ohmage/userdata/images'), 
    ('max_files_per_dir', '1000'), 
    ('document_depth', '5'), 
    ('visualization_server_address', 'http://opencpu.org/R/pub/Mobilize'),
    ('max_survey_response_page_size', '-1'),
    ('recaptcha_public_key', ''),
    ('recaptcha_private_key', ''),
    ('public_class_id', 'urn:class:public'),
    ('video_directory', '/opt/ohmage/userdata/videos'),
    ('audit_log_location', '/opt/ohmage/logs/audits/'),
    ('fully_qualified_domain_name', 'localhost');
