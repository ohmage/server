-- Adds the request types for the audit table.
INSERT INTO audit_request_type(request_type) VALUES 
    ('get'), 
    ('post'), 
    ('options'), 
    ('head'), 
    ('put'), 
    ('delete'), 
    ('trace'), 
    ('unknown');