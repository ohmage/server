/*
 * Holds a list of objects defining the question/response types for
 * this campaign.  Taken from: http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts
 */

 var response_list = [
 	{group_id:0, prompt_id:0, text:'Saliva sample time', type:4},
	{group_id:0, prompt_id:1, text:'In the twenty minutes before this sample did you: brush your teeth, eat, and drink?', type:1, y_labels:['brush', 'eat', 'drink']},
	{group_id:1, prompt_id:0, text:'What time did you go to bed last night?', type:0},
	{group_id:1, prompt_id:1, text:'How long did it take you to fall asleep last night? (in minutes)', type:2, y_labels:['10','20','30','40','50','60+']},
	{group_id:1, prompt_id:2, text:'What time did you wake up this morning?', type:0},
	{group_id:1, prompt_id:3, text:'How many hours of actual sleep did you get?', type:2, y_labels:['4','5','6','7','8','8+']},
	{group_id:1, prompt_id:4, text:'How would you rate your sleep quality?', type:2, y_labels:['Very Bad','Bad','Good','Very Good']},
	{group_id:2, prompt_id:1, text:'Sad?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:2, text:'Inspired?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:3, text:'Calm?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:4, text:'Upset?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:5, text:'Nervous?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:6, text:'Energetic?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:7, text:'Happy?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:8, text:'Blue?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:9, text:'Anxious?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:10, text:'Relaxed?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:11, text:'Tired?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:12, text:'Cheerful?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:13, text:'Enthusiastic?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:2, prompt_id:14, text:'Stressed?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:3, prompt_id:1, text:'Little interest or pleasure in doing things?', type:3},
	{group_id:3, prompt_id:2, text:'Feeling down, depressed, or hopeless?', type:3},
	{group_id:3, prompt_id:3, text:'Feeling bad about yourself, feeling that you are a failure, or feeling that you have let yourself or your family down?', type:3},
	{group_id:3, prompt_id:4, text:'How often did you feel that you were unable to control the important things in your life today?', type:2, y_labels:['Never','Almost never','Sometimes','Fairly often','Very often']},
	{group_id:3, prompt_id:5, text:'How often did you feel confident about your ability to handle your personal problems today?', type:2, y_labels:['Never','Almost never','Sometimes','Fairly often','Very often']},
	{group_id:3, prompt_id:6, text:'How often did you feel that things were going your way today?', type:2, y_labels:['Never','Almost never','Sometimes','Fairly often','Very often']},
	{group_id:3, prompt_id:7, text:'How often did you find that you could not cope with all the things that you had to do today?', type:2, y_labels:['Never','Almost never','Sometimes','Fairly often','Very often']},
	{group_id:3, prompt_id:8, text:'How often did you feel difficulties were piling up so high that you could not overcome them?', type:2, y_labels:['Never','Almost never','Sometimes','Fairly often','Very often']},
	{group_id:3, prompt_id:9, text:'Did you plan to exercise today?', type:3},
	{group_id:3, prompt_id:10, text:'Did you exercise today?', type:3},
	{group_id:3, prompt_id:11, text:'If yes, what type of exercise did you today?', type:2, y_labels:['none', 'light', 'moderate', 'vigorous']},
	{group_id:3, prompt_id:12, text:'If you exercised, for how many minutes did you exercise?', type:2, y_labels:['10','20','30','40','50','60+']},
	{group_id:3, prompt_id:13, text:'Did any of the following interfere with your plan to exercise today?', type:1, y_labels:['lack of time','lack of self-discipline','fatigue','procrastination','lack of interest','family work or responsibilities']},
	{group_id:3, prompt_id:14, text:'How many cigarettes did you smoke today?', type:2, y_labels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{group_id:3, prompt_id:15, text:'How many alcoholic beverages did you have today?', type:2, y_labels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{group_id:3, prompt_id:16, text:'How many caffeinated beverages did you have today?', type:2, y_labels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{group_id:3, prompt_id:17, text:'Did you have any high sugar food or drinks today? (soft drinks, candy, etc)', type:3},
	{group_id:3, prompt_id:18, text:'Did anything happen today that was stressful or difficult for you?', type:3},
	{group_id:3, prompt_id:19, text:'If yes, how stressful was this?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
	{group_id:3, prompt_id:20, text:'Did anything happen today that was enjoyable or felt good to you?', type:3},
	{group_id:3, prompt_id:21, text:'If yes, how enjoyable was this?', type:2, y_labels:['(Not at all) 0','1','(Slightly) 2','3','(Moderately) 4','5','(Extremely) 6']},
 ];

// Maps group id to group title
var group_list = [
 	'Saliva',
	'Sleep',
	'Emotional State',
	'Diary'
];

