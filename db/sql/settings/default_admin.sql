-- Inserts the default document privacy states into the lookup table in the database.
INSERT INTO user(username, password, admin, new_account, enabled, campaign_creation_privilege) VALUES 
    ('ohmage.admin', '$2a$13$yxus2tQ3/QiOwWcELImOQuy9d5PXWbByQ6Bhp52b1se7fNYGFxN5i', true, true, true, true);