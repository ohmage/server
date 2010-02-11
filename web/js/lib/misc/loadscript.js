/* 
 * Simple function to load js files from other js files, adds a script
 * element to the main html page.
 */
function LoadScript(){
	var source = Array.apply(null, arguments);
	var script = document.createElement('script');
	script.setAttribute('src', source.shift());
	script.setAttribute('type', 'text/javascript');
	document.getElementsByTagName('head')[0].appendChild(script);
	if(source.length)arguments.callee.apply(null, source);
}

/*
 * jQuery version of LoadScript to be IE compatible
 * Pass in the name of the script to include in the main HTML file
 */
function loadScriptJQuery() {
	// Grab the file name to include
	var source = Array.apply(null, arguments);
	// Create a new script element
	$.getScript(source);
}