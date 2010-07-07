/*
 * Holds a list of objects defining the question/response types for
 * this campaign.  Taken from: http://www.lecs.cs.ucla.edu/wikis/andwellness/index.php/AndWellness-Prompts
 */

 var responseList = [
 	{groupId:0, promptId:0, text:'Take a saliva sample now and enter time.', type:4},
	{groupId:1, promptId:0, text:'Sleep', type:5, sleepLabels:['<10 minutes', '10-20 minutes', '20-30 minutes', '30-40 minutes', '40-50 minutes', '50+ minutes']},
	{groupId:2, promptId:1, text:'Sad', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:2, text:'Relaxed', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:3, text:'Anxious', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:4, text:'Tired', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:5, text:'Happy', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:6, text:'Upset', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:7, text:'Energetic', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:8, text:'Irritable', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:9, text:'Calm', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:2, promptId:10, text:'Enjoyment/fun', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:3, promptId:0, text:'Overall, how stressed did you feel today?', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:3, promptId:2, text:'Little interest or pleasure in doing things?', type:3},
	{groupId:3, promptId:3, text:'Feeling down, depressed, or hopeless?', type:3},
	{groupId:3, promptId:4, text:'Feeling bad about yourself, feeling that you are a failure, or feeling that you have let yourself or your family down?', type:3},
	{groupId:3, promptId:5, text:'Did you exercise today?', type:3},
	{groupId:3, promptId:6, text:'If yes, what type of exercise did you today?', type:2, yLabels:['none', 'light', 'moderate', 'vigorous']},
	{groupId:3, promptId:7, text:'If you exercised, for how many minutes did you exercise?', type:2, yLabels:['10','20','30','40','50','60+']},
	{groupId:3, promptId:8, text:'If you exercised, did you enjoy exercising?', type:2, yLabels:['Not at all', 'Slightly', 'Moderately', 'Very']},
	{groupId:3, promptId:10, text:'How many alcoholic beverages did you have today?', type:2, yLabels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{groupId:3, promptId:11, text:'How many caffeinated beverages did you have today?', type:2, yLabels:['0','1','2','3','4','5','6','7','8','9','10+']},
	{groupId:3, promptId:12, text:'Did you have any high sugar food or drinks today? (soft drinks, candy, etc)', type:3},
	{groupId:4, promptId:1, text:'Sad', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:2, text:'Relaxed', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:3, text:'Anxious', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:4, text:'Tired', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:5, text:'Happy', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:6, text:'Upset', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:7, text:'Energetic', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:8, text:'Irritable', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:9, text:'Calm', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
	{groupId:4, promptId:10, text:'Enjoyment/fun', type:2, yLabels:['(Not at all) 0','(Slightly) 1','(Moderately) 2','(Extremely) 3']},
];

// Maps group id to group title
var groupList = [
 	'Saliva',
	'Sleep',
	'Emotional State',
	'Diary',
	'Emotional State'
];

var mobilityModes = [
	'still',
	'walk',
	'run',
	'bike',
	'drive'];

