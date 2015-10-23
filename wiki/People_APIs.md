API calls to perform CRUD operations on users and entities associated with users.

## API Calls

| | GET (read) | POST (create/update) | DELETE (delete)
| --- | --- | --- | ---| 
| `/people` | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-get-visible-people-ids">Get the list of visible people.</a> | Create a new person <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-create-a-person-via-credentials">via credentials</a> or <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-person-creation-via-provider">via provider</a>. | Not allowed.
| `/people/<user_id>` | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-get-information-about-a-person">Get the data about a person.</a> | Not allowed. | Delete a person <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-delete-a-person-via-credentials">via credentials</a> or <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-delete-a-person-via-provider">via provider</a>.
| `/people/<user_id>/current` | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#get-the-detailed-survey-and-stream-information-for-a-person">Get the data about a person with the latest versions of any streams and surveys.</a> | Not allowed. | Not allowed.
| `/people/<user_id>/password` |  Not allowed. | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-update-a-persons-password">Update a person's password.</a> | Not allowed.
| `/people/<user_id>/ohmlets` |  <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-get-ohmlets">Get the list of ohmlet references a person is following.</a> | Not allowed. | Not allowed.
| `/people/<user_id>/ohmlets/<ohmlet_id>` |  <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-get-a-specific-ohmlet-reference">Get a particular ohmlet reference.</a> | Not allowed. | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-leave-an-ohmlet">Leave an ohmlet.</a>
| `/people/<user_id>/ohmlets/<ohmlet_id>/ignored_streams` | Not allowed. | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-ignore-a-stream-that-an-ohmlet-references">Ignore a stream that a ohmlet references.</a> | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-stop-ignoring-a-stream-that-an-ohmlet-references">Stop ignoring a stream.</a>
| `/people/<user_id>/ohmlets/<ohmlet_id>/ignored_surveys` | Not allowed. | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-ignore-a-survey-that-an-ohmlet-references">Ignore a survey that a ohmlet references.</a> | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-stop-ignoring-a-survey-that-an-ohmlet-references">Stop ignoring a survey.</a>
| `/people/<user_id>/streams` | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-get-followed-streams">Get the list of streams that the person is following.</a> | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-follow-a-stream">Start following a stream.</a> | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-forget-stream">Stop following a stream.</a>
| `/people/<user_id>/surveys` | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-get-followed-surveys">Get the list of surveys that the person is following.</a> | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-follow-a-survey">Start following a survey.</a> | <a href="https://github.com/ohmage/server/wiki/3.x%20People%20APIs#wiki-forget-survey">Stop following a survey.</a>

### Create a Person via Credentials

Create a new ohmage account. This account is based on an email address as opposed to accounts managed by an external provider.

#### Path

    /people

#### Parameters

| Name | Description | Required | Format 
| --- | --- | --- | --- |
| password | The user's desired plaintext password. | Yes | Any string. Users should be instructed on the creation of strong passwords. | 
| user_invitation_id | A code given to the user to bypass email validation, which normally occurs during account creation. | No | Any string. | 

#### Request Body

A JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| email | The user's email address. | Yes | A valid email address. |
| full_name | The user's full name. | No | Any string. |

#### Example Request

    POST /people?password=secure_password HTTP/1.1
    Content-Type: application/json
   
     { "email":"user@example.com" }

#### Example Response

If a user invitation ID was not present in the request, an activation email will be sent to the email address provided in the request.

    HTTP/1.1 200 OK
    
    {
        "email": "user@example.com",
        "full_name": null,
        "providers": [],
        "ohmlets": [],
        "streams": [],
        "surveys": [],
        "ohmlet_invitations": []
    }

<a href="">↑ Back to Top</a>

### Person Creation via Provider

Create a new ohmage account. This account is backed by an account managed by an external provider.

#### Path

    /people

#### Parameters

| Name | Description | Required | Format 
| --- | --- | --- | --- |
| provider | The provider's name. | Yes | Any of the known providers: "google". | 
| access_token | An authenticated access token generated by the provider that can be used to validate the user's identity. | Yes | A string generated by the provider. | 

#### Request Body

A JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| email | The user's email address. | Yes | A valid email address. |
| full_name | The user's full name. | No | Any string. |

#### Example Request

    POST /people?provider=google&access_token=foo HTTP/1.1
    Content-Type: application/json
   
#### Example Response

The response has the same format as non-provider user account creation.

<a href="">↑ Back to Top</a>

### Get Visible People IDs

Gets the list of user identifiers that are visible to the logged-in user.

#### Path

    /people

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
   
#### Example Response

    HTTP/1.1 200 OK
    
     [ "79e3d297-8fb6-45f3-a891-00ecf2452bad" ]

<a href="">↑ Back to Top</a>

### Get Information about a Person

Gets the account information for the requested person.

#### Path

    /people/<user_id>

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people/57d7b7ea-2949-498d-8886-0406e788c612 HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
   
#### Example Response

    HTTP/1.1 200 OK
    
    { 
        "id":"57d7b7ea-2949-498d-8886-0406e788c612",
        "email_address":"user@example.com", 
        "ohmlets": [ 
            {
                "ohmlet_id":"b389700e-24f0-4a5f-8c97-51b63f7091a3",
                "ignored_streams": [
                    {
                        "schema_id":"d3f5d145-d9b7-4c1b-a84d-3bc597fa3283"
                    }, ...   
                ],
                "ignored_surveys": [
                    {
                        "schema_id":"e26100df-8416-4959-8a5d-f163e4446a68"
                    }, ...   
                ]
            },
            ... 
        ],
        "streams": [
            {
                "schema_id":"38936134-37f2-463a-a1cc-3baec2ac1248",
                "schema_version":1
            }, ...   
        ]
        "surveys": [
            {
                "schema_id":"d97fcd34-5774-4523-8a02-fd355d8d9019",
                "schema_version":1
            },
            {
                "schema_id":"4245af54-a6e1-4729-a5ae-30992833394b",
                "schema_version":null
            }, ...      
        ]
    }

<a href="">↑ Back to Top</a>

### Get the Detailed Survey and Stream Information for a Person

Gets the account information and ensures the stream and survey versions are provided. For those references where the version is null because the user is following the latest (i.e., following only the ID), the latest version of the stream or survey will be filled in.

#### Path

    /people/<user_id>/current

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people/57d7b7ea-2949-498d-8886-0406e788c612/current HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
   
#### Example Response

    HTTP/1.1 200 OK
    
    { 
        "id":"57d7b7ea-2949-498d-8886-0406e788c612",
        "email_address":"user@example.com", 
        "ohmlets": [ 
            {
                "ohmlet_id":"b389700e-24f0-4a5f-8c97-51b63f7091a3",
                "ignored_streams": [
                    {
                        "schema_id":"d3f5d145-d9b7-4c1b-a84d-3bc597fa3283"
                    }, ...   
                ],
                "ignored_surveys": [
                    {
                        "schema_id":"e26100df-8416-4959-8a5d-f163e4446a68"
                    }, ...   
                ],
                "streams": [
                    {
                        "schema_id":"7a61fb13-bd7a-4793-8792-4af5d429ff4f",
                        "schema_version":1
                    }, ...   
                ],
                "surveys": [
                    {
                        "schema_id":"79196485-116e-4cc8-9e15-ead9e307e113",
                        "schema_version":1
                    }, ...   
                ],
            },
            ... 
        ],
        "streams": [
            {
                "schema_id":"38936134-37f2-463a-a1cc-3baec2ac1248",
                "schema_version":1
            }, ...   
        ]
        "surveys": [
            {
                "schema_id":"d97fcd34-5774-4523-8a02-fd355d8d9019",
                "schema_version":1
            },
            {
                "schema_id":"4245af54-a6e1-4729-a5ae-30992833394b",
                "schema_version":10
            }, ...      
        ]
    }

<a href="">↑ Back to Top</a>

### Delete a Person via Credentials

Removes a person from the system using their password. Users can only delete themselves.

#### Path

    /people/<user_id>

#### Parameters

| Name | Description | Required | Format 
| --- | --- | --- | --- |
| password | The user's password. | Yes | The valid password. | 

#### Request Body

None.

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612?password=strong_password HTTP/1.1
   
#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>
    
### Delete a Person via Provider

Removes a person from the system using their provider credentials. Users can only delete themselves.

#### Path

    /people/<user_id>

#### Parameters

| Name | Description | Required | Format 
| --- | --- | --- | --- |
| provider | The provider. | Yes | Any of the known providers: "google". | 
| access_token | An access token generated by the provider. | Yes | A valid access token. | 

#### Request Body

None.

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612?provider=google&access_token=36172493-bf99-4675-9fde-2c40cf347d0e HTTP/1.1
   
#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Update a Person's Password

Allows users to change their password. This API call is only applicable for users with ohmage credentials.

#### Path

    /people/<user_id>/password

#### Parameters

| Name | Description | Required | Format 
| --- | --- | --- | --- |
| password | The original password. | Yes | The user's valid password. | 

#### Request Body

The user's new password.

#### Example Request

    POST /people/57d7b7ea-2949-498d-8886-0406e788c612/password?password=original_password HTTP/1.1
    Content-Type: text/plain
    
    new_password

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Get Ohmlets

Returns the list of ohmlets that the user is associated with and any schemas (streams or surveys) from that ohmlet that the user is explicitly ignoring.

#### Path

    /people/<user_id>/ohmlets

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
#### Example Response

    HTTP/1.1 200 OK

    [
        {
            "ohmlet_id":"8039e82b-5e7c-4c93-806e-7e74bd61eb64",
            "ignored_streams":[
                { 
                    "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243",
                    "schema_version":1
                }
            ],
            "ignored_surveys":[
                { 
                    "schema_id":"c5f02a77-90ce-474a-ab35-15d1aff1d17f",
                    "schema_version":1
                },
                { 
                    "schema_id":"8934a366-3b3d-4ae2-bdc2-878a52cd7a28"
                }
            ]
        }
    ]

<a href="">↑ Back to Top</a>

### Get a Specific Ohmlet Reference 

Returns a specific ohmlet reference from the user's list of followed ohmlets.

#### Path

    /people/<user_id>/ohmlets/<ohmlet_id>

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets/b90d77ea-433f-4991-87d9-8c491dffb578 HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
#### Example Response

    HTTP/1.1 200 OK

    {
        "ohmlet_id":"8039e82b-5e7c-4c93-806e-7e74bd61eb64",
        "ignored_streams":[
            { 
                "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243",
                "schema_version":1
            }
        ],
        "ignored_surveys":[
            { 
                "schema_id":"c5f02a77-90ce-474a-ab35-15d1aff1d17f",
                "schema_version":1
            },
            { 
                "schema_id":"8934a366-3b3d-4ae2-bdc2-878a52cd7a28"
            }
        ]
    }


<a href="">↑ Back to Top</a>

### Leave an Ohmlet

Leave an ohmlet which removes the reference from the user's ohmlet list. The user's data will no longer be shared with the ohmlet and previously shared data will no longer be visible.

#### Path

    /people/<user_id>/ohmlets/<ohmlet_id>

#### Parameters

None.

#### Request Body

None.

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets/b90d77ea-433f-4991-87d9-8c491dffb578 HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Ignore a Stream that an Ohmlet References

Stops sharing of a stream's data with an ohmlet of which the user is a member.

#### Path

    /people/<user_id>/ohmlets/<ohmlet_id>/ignored_streams

#### Parameters

None.

#### Request Body

One of the stream references from the ohmlet definition as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The stream's unique identifier. | Yes | Any string. |
| schema_version | The stream's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    POST /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets/b90d77ea-433f-4991-87d9-8c491dffb578/ignored_streams HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Stop Ignoring a Stream that an Ohmlet References

Resume sharing stream data to the ohmlet for a specific stream.

#### Path

    /people/<user_id>/ohmlets/<ohmlet_id>/ignored_streams

#### Parameters

None.

#### Request Body

One of the stream references from the ohmlet definition as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The stream's unique identifier. | Yes | Any string. |
| schema_version | The stream's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets/b90d77ea-433f-4991-87d9-8c491dffb578/ignored_streams HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Ignore a Survey that an Ohmlet References

Stops sharing of a survey's data with an ohmlet of which the user is a member.

#### Path

    /people/<user_id>/ohmlets/<ohmlet_id>/ignored_surveys

#### Parameters

None.

#### Request Body

One of the survey references from the ohmlet definition as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The survey's unique identifier. | Yes | Any string. |
| schema_version | The survey's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    POST /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets/b90d77ea-433f-4991-87d9-8c491dffb578/ignored_surveys HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Stop Ignoring a Survey that an Ohmlet References

Resume sharing survey data to the ohmlet for a specific survey.

#### Path

    /people/<user_id>/ohmlets/<ohmlet_id>/ignored_surveys

#### Parameters

None.

#### Request Body

One of the survey references from the ohmlet definition as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The survey's unique identifier. | Yes | Any string. |
| schema_version | The survey's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612/ohmlets/b90d77ea-433f-4991-87d9-8c491dffb578/ignored_surveys HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Follow a Stream

Saves the user's state indicating that the user would like to collect data for a stream. Does not imply that the user will actually collect data.

#### Path

    /people/<user_id>/streams

#### Parameters

None.

#### Request Body

A reference to a stream as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The stream's unique identifier. | Yes | Any string. |
| schema_version | The stream's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    POST /people/57d7b7ea-2949-498d-8886-0406e788c612/streams HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Get Followed Streams

Returns the references for streams the user has chosen to follow. Clients should use this as a hint for which applications to install and/or data sources to monitor.

#### Path

    /people/<user_id>/streams

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people/57d7b7ea-2949-498d-8886-0406e788c612/streams HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
#### Example Response

    HTTP/1.1 200 OK

    [
        { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }
    ]

<a href="">↑ Back to Top</a>

### Forget Stream

Saves the user's state indicating that the user would like to stop following a stream.

#### Path

    /people/<user_id>/streams

#### Parameters

None.

#### Request Body

A reference to a stream as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The stream's unique identifier. | Yes | Any string. |
| schema_version | The stream's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612/streams HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Follow a Survey

Saves the user's state indicating that the user would like to collect data for a survey. Does not imply that the user will actually collect data.

#### Path

    /people/<user_id>/surveys

#### Parameters

None.

#### Request Body

A reference to a stream as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The survey's unique identifier. | Yes | Any string. |
| schema_version | The survey's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    POST /people/57d7b7ea-2949-498d-8886-0406e788c612/surveys HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>

### Get Followed Surveys

Returns the references for surveys the user has chosen to follow. Clients should use this as a hint for which applications to install and/or data sources to monitor.

#### Path

    /people/<user_id>/surveys

#### Parameters

None.

#### Request Body

None.

#### Example Request

    GET /people/57d7b7ea-2949-498d-8886-0406e788c612/surveys HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
#### Example Response

    HTTP/1.1 200 OK

    [
        { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }
    ]

<a href="">↑ Back to Top</a>

### Forget Survey

Saves the user's state indicating that the user would like to stop following a survey.

#### Path

    /people/<user_id>/surveys

#### Parameters

None.

#### Request Body

A reference to a stream as a JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| schema_id | The survey's unique identifier. | Yes | Any string. |
| schema_version | The survey's version or null. | Yes | A number or the JSON null literal. |

#### Example Request

    DELETE /people/57d7b7ea-2949-498d-8886-0406e788c612/surveys HTTP/1.1
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912
    
    { "schema_id":"9dd3b7dd-3808-49bb-98b6-3ac6594c2243", "schema_version":1 }

#### Example Response

    HTTP/1.1 200 OK

<a href="">↑ Back to Top</a>