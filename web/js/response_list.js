/*
 * Holds a list of objects defining the question/response types for
 * this campaign.  Taken from: http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts
 */

 var response_list = [
 	{group_id:0, prompt_id:0, text:'Take a saliva sample now and enter time.', type:4},
	{group_id:1, prompt_id:0, text:'Sleep', type:5, sleep_labels:['<10 minutes', '10-20 minutes', '20-30 minutes', '30-40 minutes', '40-50 minutes', '50+ minutes']},
	{group_id:2, prompt_id:1, text:'Sad', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:2, text:'Relaxed', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:3, text:'Anxious', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:4, text:'Tired', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:5, text:'Happy', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:6, text:'Upset', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:7, text:'Energetic', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:8, text:'Irritable', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:9, text:'Calm', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:2, prompt_id:10, text:'Enjoyment/fun', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:3, prompt_id:0, text:'Overall, how stressed did you feel today?', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:3, prompt_id:2, text:'Little interest or pleasure in doing things?', type:3},
	{group_id:3, prompt_id:3, text:'Feeling down, depressed, or hopeless?', type:3},
	{group_id:3, prompt_id:4, text:'Feeling bad about yourself, feeling that you are a failure, or feeling that you have let yourself or your family down?', type:3},
	{group_id:3, prompt_id:5, text:'Did you exercise today?', type:3},
	{group_id:3, prompt_id:6, text:'If yes, what type of exercise did you today?', type:2, y_labels:['none', 'light', 'moderate', 'vigorous']},
	{group_id:3, prompt_id:7, text:'If you exercised, for how many minutes did you exercise?', type:2, y_labels:['10','20','30','40','50','60+']},
	{group_id:3, prompt_id:8, text:'If you exercised, did you enjoy exercising?', type:2, y_labels:['Not at all', 'Slightly', 'Moderately', 'Very']},
	{group_id:3, prompt_id:10, text:'How many alcoholic beverages did you have today?', type:2, y_labels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{group_id:3, prompt_id:11, text:'How many caffeinated beverages did you have today?', type:2, y_labels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{group_id:3, prompt_id:12, text:'Did you have any high sugar food or drinks today? (soft drinks, candy, etc)', type:3},
	{group_id:4, prompt_id:1, text:'Sad', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:2, text:'Relaxed', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:3, text:'Anxious', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:4, text:'Tired', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:5, text:'Happy', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:6, text:'Upset', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:7, text:'Energetic', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:8, text:'Irritable', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:9, text:'Calm', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{group_id:4, prompt_id:10, text:'Enjoyment/fun', type:2, y_labels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
];

// Maps group id to group title
var group_list = [
 	'Saliva',
	'Sleep',
	'Emotional State',
	'Diary',
	'Emotional State'
];

var mobility_modes = [
	'still',
	'walk',
	'run',
	'bike',
	'drive'];

