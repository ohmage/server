package edu.ucla.cens.awserver.domain;

public abstract class AbstractSurveyItem implements SurveyItem {
	protected String _id;
	
	public AbstractSurveyItem(String id) {
		_id = id;
	}
	
	@Override
	public String getId() {
		return _id;
	}
}
