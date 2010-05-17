function AwDataCreator() {
	
}

AwDataCreator.create_ema_aw_data = function(json_data) {
	var awData = new EmaAwData();
	awData.setData(json_data);
	return awData;
}
