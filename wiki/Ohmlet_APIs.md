# About

ohmlets have a JSON definition and contain surveys, survey reminders and streams. 

ohmlet is the name for the system entity. The name may be skinned to e.g., "project" by changing a configuration property. If the name is skinned, the change is only reflected in error messages, not URLs, parameters, etc. 

In general, "project" is preferred over "ohmlet" when discussing ohmage because "project" is more readily understood by a wider audience (as fun as it is to say "ohmlet").

Projects define the data that will be collected as part of a research study. Project "owners" create projects and invite other users to participate (these users become "members"). A project definition must contain at least one survey ID or stream ID in order to be valid.

The most basic project will contain one survey or stream ID and one user (who is also the owner). This defines a simple use case for self-experimentation. The more typical project structure is one of a researcher creating a project with multiple survey and/or stream references and then inviting participants to the project.

# Auto-Updating  

ohmlets auto-update on a user's phone when a survey or stream has a version number increase. This allows for quicker prototyping of projects as an owner makes changes to the surveys or streams on the server. It also allows studies to change over time. In order to facilitate the auto-update feature, defining ohmlets that include stream and survey versions is disallowed. For cases where an ohmlet owner does not own the survey or stream definitions being included in the ohmlet and the owner wishes to "lock" the versions, the solution is for the owner to clone the survey or streams they wish to lock and then add the cloned IDs to their ohmlet. 

# Inviting Participants

The invitation process requires the participant to have a valid email address. If the participant does not have an ohmage account, they are prompted to create one when accepting the email invitation. 

Once a user has been invited, the following steps allow them to participate in a project.

1. Download the ohmage mobile app to their phone.
2. Open the invitation email on their phone and click the invite link. For Android, the user must choose the option to allow the ohmage app to open the invitation link. For iOS, the invitation link is handled by ohmage by default.
3. Once the ohmage app opens, the user is prompted to create an account if they do not already have one. 
4. Login.

After login, the participant's projects will download to their phone. The participant may begin collecting data immediately or as the needs of the project dictate.

# API Calls

| | GET (read) | POST (create/update) | DELETE (delete)
| --- | --- | --- | --- | 
| `/ohmlets` | [Get the definitions of the visible ohmlets.](https://github.com/ohmage/server/wiki/3.x-Ohmlet-APIs#get-the-visible-ohmlet-definitions) | [Create an ohmlet](https://github.com/ohmage/server/wiki/3.x-Ohmlet-APIs#create-an-ohmlet). | Not allowed.
| `/ohmlets/<ohmlet_id>` | Get the definition of the ohmlet. | Update the ohmlet. | Delete the ohmlet. 
| `/ohmlets/<ohmlet_id>/people` | Not allowed. | Update a user's role. | Not allowed.
| `/ohmlets/<ohmlet_id>/people/<user_id>` | Not allowed. | Not allowed. | Remove the user from the ohmlet. 
| `/ohmlets/<ohmlet_id>/people/invitations` | Not allowed. | Send user invitations. | Not allowed.

## Create an ohmlet

Create a new ohmlet.

### Path

    /ohmlets

### Parameters

None.

### Request Body

A JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `name` | The name of the ohmlet. | Yes | A short user-friendly string. |
| `description` | The description of the ohmlet. | Yes | A description of the data collected by this ohmlet. This description is presented when a user is invited to an ohmlet. |
| `streams` | A list of stream IDs. | No | A JSON array of strings that are stream IDs. |
| `surveys` | A list of survey IDs. | No | A JSON array of strings that are survey IDs. |
| `reminders` | A list of reminders. | No | A JSON array of JSON objects that are reminder definitions. |
| `privacy_state` | One of the allowed privacy states. | Yes | A string representing one of the privacy states. |
| `invite_role` | The minimum required role to invite others to the ohmlet. | Yes | A string representing one of the ohmlet roles. |
| `visibility_role` | The minimum required role to view others' data for the streams and surveys referenced in the ohmlet. | Yes | A string representing one of the ohmlet roles. |
| `icon_id` | A unique identifier for the attached image filename. | No | A string representing the filename. If an icon is being supplied, the request must be a multipart request. This JSON definition must be one part and have the name "definition". The image contents must be another part with the name "icon" and the filename of this identifier.  |

#### Privacy States

Privacy states control how users are joined to ohmlets.

| Privacy State | Description
| --- | --- | 
| `public` | Anyone can join this ohmlet at any time. | 
| `invite_only` | Anyone can request an invite, but may only join once a user with the invite permission invites them. | 
| `private` | Users may not request an invite and can only join once invited. | 


#### ohmlet Roles

The `requested` and `invited` roles are for managing how users join ohmlets. The next three roles -- `member`, `moderator`, `owner` -- relate to data collection and visibility. While any of the three roles can be used for the `visibility_role` permission, the general use case is that users who collect data are assigned the `member` role and the `visibility_role` permission is set to `moderator`. This way moderators and owners can access the collected data. 

| Role | Description
| --- | --- | 
| `requested` | A user with this permission wants to join the ohmlet. | 
| `invited` | A user with this permission has the ability to raise their own role to member, effectively joining the ohmlet. | 
| `member` | This is the lowest membership role in an ohmlet. Members collect data and generally do not have permissions to invite or view other users' data. | 
| `moderator` | This is the middle membership role in the ohmlet. The reason for this role is to give someone visibility and/or invite permissions without full control over the ohmlet definition. | 
| `owner` | This is the highest membership role in the ohmlet and is the only role allowed to update the ohmlet. It is the default role given to an ohmlet creator. | 

#### Example Privacy, Invite and Visibility Role Configurations

| Use Case | Configuration |
| --- | --- |
| Self-exploration | Set the visibility role to `owner`, the invite role to `owner` and the privacy state to `private`. |
| Share data with a team | Set the visibility role to `member`. All members will be able to view other members' data. If the privacy state is public, anyone can join. To restrict team size, set the privacy role to `invite_only` or `private`. | 
| Share data with research assistants | Set the visibility role to `owner` and when researchers create accounts, update their role to `owner`. For a non-public research study, set the privacy role to `invite_only` or `private`. |
| Share data with select research assistants | Set the visibility role to `owner` and when researchers create accounts, update their role to `owner`. For researchers who are not part of an IRB, change their role to `moderator`. For a non-public research study, set the privacy role to `invite_only` or `private`. |

#### Example Request

This will create an ohmlet with one survey and one stream that will be automatically updated if their associated definitions are updated.

The permissions are set to the values used in a typical clinical study use case. Users must be invited before they can join the ohmlet due to the privacy state setting. Only owners (in this case research assistants) may invite users because the invite role is set to owner. Owners and moderators (in this case data analysts) can see the survey and stream data because the visibility role is set to moderator. When a data analyst is also a research assistant, their ohmlet role should be set to the most permissive role of the two: owner. 

##### Example Request without an Icon

    POST /ohmlets HTTP/1.1
    Content-Type: application/json
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
   
     { 
         "name":"Example ohmlet", 
         "description":"This is an example ohmlet with a user friendly description.",
         "privacy_state":"invite_only", 
         "invite_role":"owner", 
         "visibility_role":"moderator", 
         "surveys": [
             "schema_id":"710dea20-97bb-4166-8a3f-c0ed4f803528"
         ],
         "streams": [
             "schema_id":"cbd0c6c9-88f0-4711-a36f-f4170a5859e0"
         ]
     }

##### Example Request with an Icon

The same request as above, but with an icon image.

    POST /ohmlets HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912

    Content-Type: multipart/form-data; boundary=------------------------------123456789abc

    
    ------------------------------123456789abc
    Content-Disposition: form-data; name="definition"
    Content-Type: application/json

     { 
         "name":"Example ohmlet", 
         "description":"This is an example ohmlet with a user friendly description.",
         "privacy_state":"invite_only", 
         "invite_role":"owner", 
         "visibility_role":"moderator", 
         "surveys": [
             "schema_id":"710dea20-97bb-4166-8a3f-c0ed4f803528"
         ],
         "streams": [
             "schema_id":"cbd0c6c9-88f0-4711-a36f-f4170a5859e0"
         ]
     }
     ------------------------------123456789abc
     Content-Disposition: form-data; name="icon"; filename="myicon.jpg"
     Content-Type: image/jpeg

     < ... binary image data ...>
     ------------------------------123456789abc

#### Example Response

The `icon_id` will only be present if the request contained an icon.

    HTTP/1.1 200 OK
    
     {
         "id":"75012835-e853-4f14-9835-df13a7f5c6ad", 
         "icon_id":"cf84e0ab-6ab6-433f-807d-af9c99bcc0b6",
         "name":"Example ohmlet", 
         "description":"This is an example ohmlet with a user friendly description.",
         "privacy_state":"invite_only", 
         "invite_role":"owner", 
         "visibility_role":"moderator", 
         "surveys": [
             {
                 "schema_id":"710dea20-97bb-4166-8a3f-c0ed4f803528",
                 "version":2
             } 
         ],
         "streams": [
             {
                 "schema_id":"cbd0c6c9-88f0-4711-a36f-f4170a5859e0",
                 "version":1
             } 
         ],
         "members": [
             {
                 "member_id":"5bb8b48e-b230-4635-b7a9-0465ba0be3fd",
                 "role":"owner"
             } 
         ]
     }

     
<a href="">↑ Back to Top</a>

## Get the Visible Ohmlet Definitions

Returns a list of ohmlet definitions that are visible to the logged-in user. These include public ohmlets as well as private ohmlets that the logged-in user owns.

### Path

    /ohmlets

### Parameters

The [standard paging parameters](https://github.com/ohmage/server/wiki/3.x-Documentation#paging-semantics) are allowed.

### Request Body

None.

#### Example Request

    GET /ohmlets
    Authorization: ohmage d2d02f59-3e3a-4d6b-8ec2-ca1c9455dc53

#### Example Response

A JSON array of JSON objects that contain ohmlets is returned.

    HTTP/1.1 200 OK
    
    [
        {
            "ohmlet_id": "ad21ce42-e497-4f4c-b1db-524529a7b133",
            "name": "Tester",
            "description": "Tester Ohmlet",
            "streams": [],
            "surveys": [
                {
                    "schema_id": "005a244c-32b0-4f20-8fe8-d052ed4c3f79",
                    "version": 1
                }
            ],
            "reminders": [],
            "people": [
                {
                    "member_id": "6a161f22-2617-4b5a-a2de-0e212276da73",
                    "role": "owner"
                }
            ],
            "privacy_state": "public",
            "invite_role": "member",
            "visibility_role": "member",
            "icon_id": null
        }
    ]
     
<a href="">↑ Back to Top</a>