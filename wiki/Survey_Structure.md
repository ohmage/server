A survey is structured as a list of "survey items" where a survey item can be a question requiring a response or a simple message that is displayed. Survey items can be shown or hidden using conditions, which are statements that evaluate to true (show the survey item) or false (do not show the item). Conditions allow for complex branching logic based on user responses to questions.

#### Prompt Types

If you are familiar with the [XML survey format](https://github.com/ohmage/server/wiki/Campaign-Definition) in ohmage 2.x, the new JSON format is very similar. In 3.x, the options for choice prompt types have been extended, but the core set of types is the same as that in 2.x. Also in 3.x, surveys are defined outside of ohmlets (campaigns in 2.x parlance) in order to enable survey reuse.

| Type | Definition |
| --- | --- |
| `message` | Shows an informational message. Useful for introductory text, instructions, or motivational or progress messages. |
| `audio_prompt` | Capture an audio response using the device's audio recorder. | 
| `image_prompt` | Capture an image response using the device's camera. | 
| `video_prompt` | Capture a video response using the device's camera. | 
| `number_prompt` | Capture a numeric value using a number picker. |
| `remote_activity_prompt` | Launch an application and capture its response using ohmage. |
| `number_single_choice_prompt` | Capture a single numeric choice from a list. |
| `string_single_choice_prompt` | Capture a single string choice from a list. |
| `number_multi_choice_prompt` | Capture zero or more numeric choices from a list. |
| `string_multi_choice_prompt` | Capture zero or more string choices from a list. |
| `text_prompt` | Capture free-form text from a text input box. |
| `timestamp_prompt` | Capture a date and a time. |

##### Example Survey

    {
        "name": "Complete Survey",
        "description": "A survey containing all prompt types.",
        "icon_id": null,
        "survey_items": [
            {
                "survey_item_type": "message",
                "survey_item_id": "myMessage",
                "condition": null,
                "text": "This is a sample message."
            },
            {
                "survey_item_type": "audio_prompt",
                "survey_item_id": "myAudio",
                "condition": null,
                "display_type": "recorder",
                "text": "Record an audio sample.",
                "display_label": "Audio",
                "skippable": false,
                "default_response": null,
                "max_duration": null
            },
            {
                "survey_item_type": "image_prompt",
                "survey_item_id": "myImage",
                "condition": null,
                "display_type": "camera",
                "text": "Take a picture.",
                "display_label": "Image",
                "skippable": false,
                "default_response": null,
                "max_dimension": null
            },
            {
                "survey_item_type": "video_prompt",
                "survey_item_id": "myVideo",
                "condition": null,
                "display_type": "camera",
                "text": "Take a video.",
                "display_label": "Video",
                "skippable": false,
                "default_response": null,
                "max_duration": null
            },
            {
                "survey_item_type": "number_prompt",
                "survey_item_id": "myNumber",
                "condition": null,
                "display_type": "picker",
                "text": "Choose a number.",
                "display_label": "Number",
                "skippable": false,
                "default_response": null,
                "min": null,
                "max": null,
                "whole_numbers_only": false
            },
            {
                "survey_item_type": "remote_activity_prompt",
                "survey_item_id": "myRemoteActivity",
                "condition": null,
                "display_type": "launcher",
                "text": "Launch the remote activity.",
                "display_label": "Remote Activity",
                "skippable": false,
                "default_response": null,
                "uri": "http://www.google.com/",
                "definition": {
                    "type": "object",
                    "optional": false,
                    "fields": []
                },
                "apps": []
            },
            {
                "survey_item_type": "number_single_choice_prompt",
                "survey_item_id": "myNumberSingleChoicePrompt",
                "condition": null,
                "display_type": "list",
                "text": "Choose a number.",
                "display_label": "Number Single Choice",
                "skippable": false,
                "default_response": null,
                "choices": [
                    {
                        "text": "The text to show.",
                        "value": 1
                    }
                ]
            },
            {
                "survey_item_type": "string_single_choice_prompt",
                "survey_item_id": "myStringSingleChoicePrompt",
                "condition": null,
                "display_type": "list",
                "text": "Choose a string.",
                "display_label": "String Single Choice",
                "skippable": false,
                "default_response": null,
                "choices": [
                    {
                        "text": "The text to show.",
                        "value": "foo"
                    }
                ]
            },
            {
                "survey_item_type": "number_multi_choice_prompt",
                "survey_item_id": "myNumberMultiChoicePrompt",
                "condition": null,
                "display_type": "list",
                "text": "Choose multiple numbers.",
                "display_label": "Number Multi Choice",
                "skippable": false,
                "default_response": null,
                "choices": [
                    {
                        "text": "More text to show.",
                        "value": 2
                    },
                    {
                        "text": "The text to show.",
                        "value": 1
                    }
                ],
                "min_choices": null,
                "max_choices": null
            },
            {
                "survey_item_type": "string_multi_choice_prompt",
                "survey_item_id": "myStringMultiChoicePrompt",
                "condition": null,
                "display_type": "list",
                "text": "Choose multiple strings.",
                "display_label": "String Multi Choice",
                "skippable": false,
                "default_response": null,
                "choices": [
                    {
                        "text": "More text to show.",
                        "value": "bar"
                    },
                    {
                        "text": "The text to show.",
                        "value": "foo"
                    }
                ],
                "min_choices": null,
                "max_choices": null
            },
            {
                "survey_item_type": "text_prompt",
                "survey_item_id": "myText",
                "condition": null,
                "display_type": "textbox",
                "text": "Enter some text.",
                "display_label": "Text",
                "skippable": false,
                "default_response": null,
                "min": null,
                "max": null
            },
            {
                "survey_item_type": "timestamp_prompt",
                "survey_item_id": "myTimestamp",
                "condition": null,
                "display_type": "picker",
                "text": "Choose a date and time.",
                "display_label": "Timestamp",
                "skippable": false,
                "default_response": null
            }
        ]
    }

#### Conditions

Prompts may be defined with conditions which when evaluated to true cause the prompt to be displayed. Conditions are statements which contain comparisons based on previous user input. 

##### Concepts

Conditions are defined by a simple grammar described by this BNF. 

    condition ::= <fragment> 

    fragment ::= '(' <fragment> ')'
               | <not> <fragment>
               | <terminal> <comparator> <terminal>
               | <fragment> <conjunction> <fragment> 
               | <terminal> 

    not ::= '!'
 
    comparator ::= '==' | '!=' | '<' | '<=' | '>' | '>=' 

    conjunction ::= 'AND' | 'OR'

    terminal ::= 'NOT_DISPLAYED' | 'SKIPPED' | <string> | <number> | <prompt_id>

    string ::= Any double quoted value.

    number ::= Any rational number. 

    prompt_id ::= A non-quoted string representing a prompt ID that is defined before the prompt with this condition.

##### Examples

Only show this prompt if the user chose 3. This will work for a single choice number, multiple choice number, or a number prompt.

    somePreviousPromptId == 3

Only show this prompt if the user entered "Hello, world.". This will work for a single choice string, multiple choice string, or text prompt.

    anotherPreviousPromptId == "Hello, world."

Only show this prompt if the user entered 3 for one prompt and "Hello, world." for another. These three conditions are equivalent.

    somePreviousPromptId == 3 AND anotherPreviousPromptId == "Hello, world."

    (somePreviousPromptId == 3) AND anotherPreviousPromptId == "Hello, world."    

    (somePreviousPromptId == 3 AND somePreviousPromptId == "Hello, world.")

Conditions are evaluated from left to right and short circuit evaluation is performed.

This condition 

    a == 3 AND b != 4 OR c > 5
    
is converted into tree form

               AND
             /     \
           ==        OR
          /  \     /    \
         a    3   !=     >
                 / \    / \
                b   4  c   5
           
and the tree's left branches are evaluated before the right branches.

`!` ("not") can be used to invert the meaning of any fragment. The following two examples are equivalent.

    somePreviousPromptId > 3 

    ! (somePreviousPromptId <= 3)

A prompt ID can be used by itself to indicate whether or not a prompt has a response. The following two examples are equivalent.

    somePromptId

    (somePromptId != SKIPPED) AND (somePromptId != NOT_DISPLAYED) 

When `SKIPPED` and `NOT_DISPLAYED` are not part of a comparator, they will always evaluate to false. If the value for `somePreviousPromptId` is not equal to 3, this condition will evaluate to false.

    somePreviousPromptId == 3 OR SKIPPED
    
When strings are not part of a comparator, they will always evaluate to true. When numbers are not part of a comparator, they will evaluate to true if non-zero.
