-- This script needs to be run by a database administrator.

-- Create the 'ohmage' database.
CREATE DATABASE ohmage;

-- Create the 'ohmage' user.
CREATE USER 'ohmage'@'localhost' IDENTIFIED BY '&!sickly';

-- Give the new 'ohmage' user all permissions on the 'ohmage' database.
GRANT ALL PRIVILEGES ON ohmage.* TO 'ohmage'@'localhost';

-- Move the tables in the old database to the new database.
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

-- Delete the old 'andwellness' user.
DROP USER 'andwellness'@'localhost';