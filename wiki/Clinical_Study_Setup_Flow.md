This page describes the standard setup flow for setting up clinical or research study using ohmage 3.0.

## Research Coordinator Flow

### Researcher

With the help of an ohmage technical coordinator:

1. Create an account or use an existing account.
2. Create an ohmlet. 
3. Invite participants via their email addresses. This may be done anytime a participant needs to be added. 

### Server

When a user is invited to an ohmlet by email address, the following occurs on the server.

1. For any email address that already has an account, the account will be invited to the ohmlet.
2. For email addresses without accounts, an email is sent to each invitee with:
    * links to install the application (both Google Play and iTunes)
    * a link that the invitee will need to follow on their device

## Study Participant Flow

On their smartphone, the participant:

1. Opens the invitation email.
2. Follows the ohmage install link and installs the app.
3. Follows the ohmlet invitation link, which will launch ohmage. ohmage then records the following parameters from the link:
    * The email address and verification code, which will be used in step 4 to seed account creation and automatically verify an email address.
    * The invitation code, which will be used in step 5 to join the ohmlet.
4. The user will be prompted to login or create an account if they aren't already logged in.
    * If creating an account, the email address is pre-filled with the invited email address. If the invitee wishes to change their email address, the new address will need to be verified and a new verification email will be sent. The user will not be able to proceed with steps 5 and 6 until their new email address is verified.
5. The phone app will join the ohmlet using the invitation code.
6. The participant will now be able to view and generate data for the streams and surveys present in the ohmlet.

