-- Move the Email address field to the user table.
ALTER TABLE user ADD COLUMN email_address VARCHAR(320);
UPDATE user, user_personal SET user.email_address = user_personal.email_address WHERE user.id = user_personal.user_id;
ALTER TABLE user_personal DROP COLUMN email_address;

-- Drop the JSON data column from the personal information.
ALTER TABLE user_personal DROP COLUMN json_data;

-- Create the public class.
INSERT INTO class(urn, name, description)
    VALUES ('urn:class:public', 'Public Class', 'This is the public class for all self-registered users.');

-- Create the table responsible for user registration.
CREATE TABLE user_registration(
    -- A unique key for each request.
    id int unsigned NOT NULL auto_increment,
    -- A reference to the user.
    user_id int unsigned NOT NULL,
    -- The registration ID.
    registration_id VARCHAR(128) NOT NULL,
    -- The time at which the registration request was made.
    request_timestamp BIGINT UNSIGNED NOT NULL,
    -- The time at which the registration was accepted.
    accepted_timestamp BIGINT UNSIGNED DEFAULT NULL,
    -- The ID is the primary key.
    PRIMARY KEY (id),
    -- Guarantees that multiple requests for the same user cannot exist.
    UNIQUE (user_id),
    -- Link the user table.
    CONSTRAINT user_id FOREIGN KEY (user_id) REFERENCES user (id) ON UPDATE CASCADE ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8;

-- This is being self-referenced now and doesn't need to be a preference in the
-- database.
DELETE FROM preference WHERE p_key='properties_file';

-- Need to update the preference table to remove an overly unnecessary UNIQUE 
-- constraint and add it only on the key.
DROP INDEX p_key ON preference;
ALTER TABLE preference ADD UNIQUE unique_p_key (p_key);
ALTER TABLE preference MODIFY p_value TEXT NOT NULL;

-- Add the new preference information.
INSERT INTO preference VALUES 
    ('recaptcha_public_key', ''),
    ('recaptcha_private_key', ''),
    ('self_registration_allowed', 'false'),
    ('terms_of_service', 'We will collect as much data as is required by our system and that you offer to our system, no more, no less. We will keep the data as secure as possible, but you recognize that unforeseeable incidents may happen that cause data to be lost or stolen. We make every effort to keep our systems as secure as possible.

Any website collects information about its users. This typically includes data about the device being used (for example, a web browser or a smartphone application), the IP address of the system making requests, HTTP cookies, request timestamps and timezones, and any data (including images) being provided to interact with the various application functions. In addition to the standard HTTP headers and payloads just described, ohmage also collects location information, system analytics regarding smartphone usage, personal data streams, and your email address, if provided. By using the ohmage smartphone application, you acknowledge that this information is being collected about you and your activities. If you are a software developer, the ohmage server APIs are fully documented on GitHub [1].

Why We Collect This Data

ohmage is designed for the analysis of scripted self-report (survey) data, smartphone analytics data, and personal data streams such as user activity data. All of these data streams can be combined together to provide useful information to end users, researchers, clinicians and students from a wide variety of application domains. If you are a participant in a study in a domain of research around health behaviors, you will be given a very thorough explanation about the study and the information collected. This process is handled by the IRB (Institutional Review Board) from various institutions [2]. Each research study has its own IRB application that is thoroughly vetted and approved by an IRB before the study can begin.

If you are a participant in a research study, you always have the option to opt-out and discontinue your participation.

Your Privacy

In cases where you have self-registered with ohmage, it is up to you to choose a username and password. You are also asked to provide your email address. We will only use your email address to contact you to complete the registration process and reset your password should it be necessary. CENS-MobilizeLabs does not share or sell your email address or personal information with any third-party.

In ohmage, you have control over the privacy settings of your self-report data. Each self-report may be marked as "shared" or "private". When a self-report is marked as private, only you, system administrators, and research supervisors can view that data. A research supervisor is a person who is managing a population of participants in order to perform research. As a participant, you may also delete your self-report responses.

For passively collected data such as smartphone analytics and personal data streams, the data is private to you, system administrators and research supervisors. This information is used to analyze self-report data along different axes. For example, your activity may be compared to your self-reports about sleep in a sleep study.

One of our goals is to put you in control of your data. It is also important for system administrators and research supervisors to have access to your data in order to interpret patterns, perform research, and fix system problems. The ohmage team will put your privacy first and we will never share your data with a third-party.

Final Notes

We are constantly striving to build a great software and a pleasing experience to our end users. We think our app is awesome and we hope you enjoy using it!

1. https://github.com/cens/ohmageServer/wiki
2. The UCLA IRB information can be found here: http://ohrpp.research.ucla.edu/pages/about-irb

    '),
    ('mail_registration_sender_address', 'no-reply@ohmage.org'),
    ('mail_registration_subject', 'ohmage: New Account Request'),
    ('mail_registration_text', '<h3>Registration Activation</h3><p>Thank you for creating an account. To activate your account, follow the link at the end of this message. By following the link you agree to our terms of service.</p><_TOS_><br /><_REGISTRATION_LINK_>'),
    ('mail_password_reset_sender_address', 'no-reply@ohmage.org'),
    ('mail_password_reset_subject', 'ohmage: Password Reset'),
    ('mail_password_reset_text', '<h3>Password Reset</h3><p>Your password has been reset. Please attempt to login with your new password below at which time you will be prompted to change your password.</p>'),
    ('cors-lenient-mode', 'false');
    
-- The key in the classifier JSON for Mobility points for the N95 variance has
-- been changed from "N95Variance" to "n95variance".
UPDATE mobility_extended SET features=REPLACE(features, 'N95Variance', 'n95variance');

-- Add creation and update time tracking to the annotation table
ALTER TABLE annotation ADD COLUMN last_modified_timestamp timestamp default current_timestamp on update current_timestamp;
ALTER TABLE annotation ADD COLUMN creation_timestamp datetime NOT NULL;
-- Add the annotation owner's user_id for handling delete and update functionality
ALTER TABLE annotation ADD COLUMN user_id int unsigned NOT NULL;
ALTER TABLE annotation ADD FOREIGN KEY (user_id) REFERENCES user (id) ON DELETE CASCADE ON UPDATE CASCADE;

ALTER TABLE mobility ADD INDEX index_time (epoch_millis);