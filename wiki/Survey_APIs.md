Surveys have a [JSON definition](https://github.com/ohmage/server/wiki/3.x-Survey-Structure) which defines the ordering and flow of prompts (questions) as well as metadata like the response type and prompt text. 

#### API Calls

| | GET (read) | POST (update) | DELETE (delete)
| --- | --- | --- | ---| 
| `/surveys` | Get the IDs of the visible surveys. | [Create a survey](https://github.com/ohmage/server/wiki/3.x-Survey-APIs#create-a-survey). | Not allowed.
| `surveys/<id>` | Get the versions of a survey. | Update the survey with a new version. | Not allowed.
| `/surveys/<id>/<version>` | Get the definition of a survey. | Not allowed. | Not allowed.
| `/surveys/<id>/<version>/data` | Get the visible data for a survey. | [Upload new survey data](https://github.com/ohmage/server/wiki/3.x-Survey-APIs#upload-survey-responses). | Not allowed.
| `/surveys/<id>/<version>/data/<point_id>` | Get the point's data. | Not allowed. |  Delete the point. 

### Create a Survey

Create a new survey that will be owned by the logged-in user. The owner will be the only person who can make changes to the survey. 

#### Path

    /surveys

#### Parameters

None.

#### Request Body

A JSON object with the following key-value pairs.

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `name` | A descriptive name for the survey. | Yes | Any string. |
| `description` | The user's full name. | Yes | Any string. |
| `icon_id` | An ID that must match the filename of an attached image. | No | Any string. |
| `omh_visible` | If true, this survey will be available through the Open mHealth API calls. | No | Either `true` or `false`.
| `survey_items` | A list of survey items which define the survey content. | Yes | A JSON array of JSON objects that are survey items.

##### Survey Item Properties Common to All Survey Items

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `survey_item_type` | The type of the survey item. | Yes | A string that must be one of: `message`, `audio_prompt`, `image_prompt`, `video_prompt`, `number_prompt`, `remote_activity_prompt`, `number_single_choice_prompt`, `string_single_choice_prompt`, `number_multi_choice_prompt`, `string_multi_choice_prompt`, `text_prompt`, `timestamp_prompt`. 
| `survey_item_id` | An ID unique to this survey. | Yes | Any string containing upper or lower cases letters only. 
| `condition` | A valid [condition string](https://github.com/ohmage/server/wiki/3.x-Survey-Structure#conditions). | Yes. | A valid condition string or `null`. 
| `text` | The text that will be shown to the user. | Yes. | Any string. 
| `display_type` | A display type that is relevant for the survey item type being defined. See below for restrictions on this value for specific survey item types. This property is currently unused by the 3.0 Android app. | Yes | A valid display type or `null`. 
| `display_label` | A short description of this survey item that will be shown in visualizations. | Yes. | Any string or `null`. 
| `skippable` | Whether the user can skip this survey item. | Yes | `true` or `false`. 
| `default_response` | A default response that will be shown the first time the survey item is shown. Media prompt types do not allow default responses. | Yes | A valid value for the survey item type or `null`. 

##### Survey Item Properties for Media Prompts

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `display_type` | For audio prompts, the only allowed value is `recorder`. For image and video prompts, the only allowed value is `camera`. | Yes | A string that is an allowed value for the survey item type.
| `max_duration` | For audio prompts, the maximum duration in milliseconds. For video prompts, the maximum duration in seconds. | Yes | A positive integer. 
| `max_dimension` | For image prompts, the maximum dimension allowed for the recorded image. | Yes | A positive integer. 

##### Survey Item Properties for Number Prompts

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `display_type` | One of `list`, `picker`, `slider`, or `textbox`. | Yes | One of the allowed strings.
| `min` | The minimum allowed value. | Yes | A number or `null` to indicate no minimum.
| `max` | The maximum allowed value. | Yes | A number or `null` to indicate no minimum.
| `whole_numbers_only` |  Allow only whole numbers. | Yes | `true` or `false`.

##### Survey Item Properties for Remote Activity Prompts

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `display_type` | The only allowed value is `launcher`. | Yes | The string `launcher`.
| `uri` | A URI that will cause ohmage to launch the app. | Yes | A valid URI.
| `definition` | A JSON object that is a Concordia definition defining the output produced by this remote activity. | Yes | A valid Concordia schema.
| `apps` |  A JSON object with two sub-objects. One object, `ios`, defines properties specific to iOS and the other, `android`, defines properties specific to Android. | Yes. | See the following table.

###### Remote Activity Apps

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `app_uri` | A URL to a downloadable app. | Yes | A valid URL. 
| `authorization_uri` | A URI to authorization for apps that require a user to grant access to another app. | No. | A valid authorization URI.
| `package` | Android only: The unique package name for this app. | Yes | A dot-separated string (a Java package name).
| `version` | Android only: The version of the app to download. | Yes | A valid app version.

##### Survey Item Properties for Choice Prompts 

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `display_type` | Currently unused. | Yes | Any string.
| `choices` | A JSON array of JSON objects that contain a `text` and `value` property. The text is what is shown to the user. The value is what is submitted as the user's response. For the string choice types, values must be strings. For the number choice types, values must be numbers. | Yes | A JSON array containing at least one text-value JSON object.
| `min_choices` | For multi_choice types, requires the user to select at least this many choices. | Yes | `null` or an integer.
| `max_choices` | For multi_choice types, requires the user to select at most this many choices. | Yes | `null` or an integer.

##### Survey Item Properties for Text Prompts 

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `display_type` | The only allowed value is `textbox`. | Yes | The string `textbox`.
| `min` | The minimum length of the text string the user can enter. | Yes | `null` or an integer.
| `max` | The maximum length of the text string the user can enter. | Yes | `null` or an integer.


#### Example Request With an Icon

    POST /surveys
    Content-Type: multipart/form-data; boundary=------------------------------123456789abc
    Authorization: ohmage a9d50d7a-1d8d-4698-824a-668c50f9e912

    ------------------------------123456789abc
    Content-Disposition: form-data; name="definition"
    Content-Type: application/json

    {
        "name": "Message Survey",
        "description": "A survey containing a single message.",
        "icon_id": "myicon",
        "survey_items": [
            {
                "survey_item_type": "message",
                "survey_item_id": "myMessage",
                "condition": null,
                "text": "Hello, world."
            }
        ]
    }

    ------------------------------123456789abc
    Content-Disposition: form-data; name="icon"; filename="myicon.jpg"
    Content-Type: image/jpeg

    < ... binary image data ...>
    ------------------------------123456789abc   

#### Example Request Without an Icon

As above, but the icon_id property is set to null and the icon part should not be present in the POST.

#### Example Response

    HTTP/1.1 200 OK
    
    {
        "schema_id": "86b1acc7-3c4e-4884-9666-128e0c68a921",
        "schema_version": 0,
        "name": "Message Survey",
        "description": "A survey containing a single message.",
        "owner": "6a161f22-2617-4b5a-a2de-0e212276da73",
        "icon_id": null,
        "survey_items": [
            {
                "survey_item_type": "message",
                "survey_item_id": "myMessage",
                "condition": null,
                "text": "Hello, world."
            }
        ],
        "definition": {
            "type": "object",
            "doc": "A survey containing a single message.",
            "optional": false,
            "name": "86b1acc7-3c4e-4884-9666-128e0c68a921",
            "fields": []
        }
    }

<a href="">↑ Back to Top</a>

### Upload Survey Responses

Saves one or more survey responses for a single user.

#### Path

    /surveys/<survey_id>/<survey_version>/data

#### Parameters

None. An authentication token must be present in the HTTP headers.

#### Request Body

The request body is a multi-part form POST containing one or many parts. One part must be a JSON array of JSON objects representing survey responses. Any other part must be an attached file containing one of the allowed media types (audio, image, video).

##### Survey Upload JSON Format

Each survey response is represented an object that must contain a metadata object and a data object. 

##### Metadata

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `id` | A client-generated ID. This ID must be unique for the user-survey ID-survey version triple. | Yes | Any string.
| `timestamp` | A timestamp that indicates the time at which this survey response was generated on the client. The timestamp must be one of the [W3C formats of the ISO8601 date standard](http://www.w3.org/TR/NOTE-datetime). | No | A valid W3C ISO8601 timestamp.
| `timestamp_millis` | The number of milliseconds since the Unix epoch indicating when this survey response was generated on the client. | No | A long.
| `location` | A JSON object containing location information. | No | A valid location object. See the following table.

###### Location

| Key | Description | Required | Format 
| --- | --- | --- | --- |
| `latitude` | The latitude at which this point was generated. | Yes | A double representing a valid latitude. 
| `longitude` | The longitude at which this point was generated. | Yes | A double representing a valid longitude. 
| `accuracy` | The accuracy in meters of this latitude-longitude pair. | Yes | A double. 
| `time` | The number of milliseconds since the Unix epoch indicating when this location was generated on the client. | Yes | A double. 

##### Data

A JSON object containing responses for the survey definition identified by survey ID-version pair in the URI path to this request. The JSON object is a series of key-value pairs where each key is a `survey_item_id` and its value is a valid value for the ID's `survey_item_type`.

###### General Rules

If a `survey_item` is marked as `skippable` and the user skipped the survey, the survey item should not be included in the upload. The server application will set the value as skipped if necessary.

If a `survey_item` was not displayed to the user because it contained a condition that evaluated to false, the survey item should not be included in the upload. The server application will set the value as not displayed if necessary.

###### Specific Survey Item Types

| Type | Value |
| --- | --- | 
| `message` | No response should be included in the data object. |
| `audio_prompt` | A string ID unique to this survey upload which must equal a filename present in one of the multipart parts. |
| `image_prompt` | A string ID unique to this survey upload which must equal a filename present in one of the multipart parts. |
| `video_prompt` | A string ID unique to this survey upload which must equal a filename present in one of the multipart parts. |
| `number_prompt` | A number between `min` and `max` inclusive if min and max are defined for the survey item. Must be a whole number if `whole_numbers_only` is true. |
| `remote_activity_prompt` | A JSON object which must adhere to the Concordia schema defined for the remote activity. |
| `number_single_choice_prompt` | A number that must equal one of the values defined for this prompt. |
| `string_single_choice_prompt` | A string that must equal one of the values defined for this prompt. |
| `number_multi_choice_prompt` | An array of numbers where each number must equal one of the values defined for this prompt. |
| `string_multi_choice_prompt` | An array of strings where each number must equal one of the values defined for this prompt. |
| `text_prompt` | A string with a length that must be within `min` and `max` inclusive if min and max are defined. |
| `timestamp_prompt` | A string that must be one of the [W3C formats](http://www.w3.org/TR/NOTE-datetime) of the ISO8601 date-time standard.  |

#### Example Request

For the survey definition ...

    {
       "name":"Image Survey",
       "description":"A survey with all of the combinations of image prompts.",
       "owner":"29350aa0-462a-4448-a507-d2f2d448cb9e",
       "omh_visible":true,
       "survey_items":[
          {
             "survey_item_type":"image_prompt",
             "survey_item_id":"basic",
             "display_type":1,
             "text":"Take a picture.",
             "display_label":"Basic",
             "skippable":true
          },
          {
             "survey_item_type":"image_prompt",
             "survey_item_id":"dimension",
             "display_type":1,
             "text":"Take an image with a maximum duration of 500 pixels.",
             "display_label":"Dimension",
             "skippable":true,
             "max_dimension":500
          }
       ]
    }

... an HTTP POST is as follows.

    POST /ohmage/surveys/986d601b-7dd4-4216-8797-a9e5ed8f07c1/1/data
     HTTP/1.1
    Authorization: ohmage e5e824be-0bee-4dc6-be5f-791b558898d4
   
    Content-Type: multipart/form-data; boundary=----------------------------d1cde89e05e3
    ------------------------------d1cde89e05e3
    Content-Disposition: form-data; name="data"
    Content-Type: application/json

    [{"meta_data":{"id":"af3dd05a-5790-4487-a163-bc4fc1e96934","timestamp":"2014-03-06T16:13:31.679-05:00",
    "timestamp_millis":1394140411679},"data":{"basic":"0daad9d9-3a02-4920-acfb-dcd5a230fc35",
    "dimension":"42d64cb5-2b68-428a-bc4c-892bece680f8"}}]
     
    ------------------------------d1cde89e05e3
    Content-Disposition: form-data; name="media"; filename="0daad9d9-3a02-4920-acfb-dcd5a230fc35"
    Content-Type: image/jpeg
    
    <binary image data>

    ------------------------------d1cde89e05e3
    Content-Disposition: form-data; name="media"; filename="42d64cb5-2b68-428a-bc4c-892bece680f8"
    Content-Type: image/jpeg
    
    <binary image data>


#### Example Response

The server echoes the upload in the response.

    HTTP/1.1 200 OK
    
    [
        {"owner":"6a161f22-2617-4b5a-a2de-0e212276da73","meta_data":{"id":"af3dd05a-5790-4487-a163-bc4fc1e96934",
        "timestamp":"2014-03-06T16:13:31.679-05:00"},"data":{"dimension":"42d64cb5-2b68-428a-bc4c-892bece680f8",
        "basic":"0daad9d9-3a02-4920-acfb-dcd5a230fc35"}}
    ]


<a href="">↑ Back to Top</a>