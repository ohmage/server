-- BEFORE RUNNING THIS SCRIPT:
-- These operations must all be run by a user that has enough permissions to
-- create new users and grant them permissions on tables.
-- 1) Give the user 'andwellness' full permissions on the database 'ohmage', 
--    which doesn't yet exist.
--      GRANT ALL PRIVILEGES ON ohmage.* TO 'andwellness'@'localhost';
--
-- 2) Create a user call 'ohmage';
--      CREATE USER 'ohmage'@'localhost' IDENTIFIED BY '&!sickly';
--
-- 3) Give 'ohmage' all permissions on the still non-existant table.
--      GRANT ALL PRIVILEGES ON ohmage.* TO 'ohmage'@'localhost';

-- Renames the database to ohmage.
CREATE DATABASE ohmage;
RENAME TABLE 
    andwellness.annotation TO ohmage.annotation,
    andwellness.annotation_annotation TO ohmage.annotation_annotation,
    andwellness.audit TO ohmage.audit,
    andwellness.audit_extra TO ohmage.audit_extra,
    andwellness.audit_parameter TO ohmage.audit_parameter,
    andwellness.audit_request_type TO ohmage.audit_request_type,
    andwellness.campaign TO ohmage.campaign,
    andwellness.campaign_annotation TO ohmage.campaign_annotation,
    andwellness.campaign_class TO ohmage.campaign_class,
    andwellness.campaign_class_default_role TO ohmage.campaign_class_default_role,
    andwellness.campaign_privacy_state TO ohmage.campaign_privacy_state,
    andwellness.campaign_running_state TO ohmage.campaign_running_state,
    andwellness.class TO ohmage.class,
    andwellness.document TO ohmage.document,
    andwellness.document_campaign_role TO ohmage.document_campaign_role,
    andwellness.document_class_role TO ohmage.document_class_role,
    andwellness.document_privacy_state TO ohmage.document_privacy_state,
    andwellness.document_role TO ohmage.document_role,
    andwellness.document_user_creator TO ohmage.document_user_creator,
    andwellness.document_user_role TO ohmage.document_user_role,
    andwellness.mobility TO ohmage.mobility,
    andwellness.mobility_extended TO ohmage.mobility_extended,
    andwellness.mobility_privacy_state TO ohmage.mobility_privacy_state,
    andwellness.preference TO ohmage.preference,
    andwellness.prompt_response TO ohmage.prompt_response,
    andwellness.prompt_response_annotation TO ohmage.prompt_response_annotation,
    andwellness.survey_response TO ohmage.survey_response,
    andwellness.survey_response_annotation TO ohmage.survey_response_annotation,
    andwellness.survey_response_privacy_state TO ohmage.survey_response_privacy_state,
    andwellness.url_based_resource TO ohmage.url_based_resource,
    andwellness.user TO ohmage.user,
    andwellness.user_class TO ohmage.user_class,
    andwellness.user_class_role TO ohmage.user_class_role,
    andwellness.user_personal TO ohmage.user_personal,
    andwellness.user_role TO ohmage.user_role,
    andwellness.user_role_campaign TO ohmage.user_role_campaign;
DROP DATABASE andwellness;

-- AFTER RUNNING THIS SCRIPT:
-- These operations must be run by a user that has enough permissions to drop
-- existing users.
-- 1) Remove the user 'andwellness'.
--      DROP USER 'andwellness'@'localhost';