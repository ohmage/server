-- This SQL is intended to be run after andwellness-ddl.sql to initialize the user_roles in the system.
INSERT INTO user_role (role) VALUES 
    ('participant'), 
    ('author'), 
    ('analyst'), 
    ('supervisor');
