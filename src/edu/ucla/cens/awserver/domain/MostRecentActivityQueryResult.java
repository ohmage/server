package edu.ucla.cens.awserver.domain;


/**
 * Data transfer object for returning the time since either the most recent prompt response upload or most recent mobility upload
 * occurred. The value in this class represents that time in hours.
 * 
 * @author selsky
 */
public class MostRecentActivityQueryResult {
	private double _hoursSinceLastActivity;
	private String _maxFieldLabel;
	private String _userName; // a user name is kept here in addition to the user name found in the PromptActivityQueryResult and 
	                          // the MobilityActivityQueryResult because there is a case boht of those query result objects
	                          // will be null (i.e., no records found for either)

	private PromptActivityQueryResult _promptActivityQueryResult;
	private MobilityActivityQueryResult _mobilityActivityQueryResult;

	public String getUserName() {
		return _userName;
	}

	public void setUserName(String userName) {
		_userName = userName;
	}
	
	public String getMaxFieldLabel() {
		return _maxFieldLabel;
	}

	public void setMaxFieldLabel(String maxFieldLabel) {
		_maxFieldLabel = maxFieldLabel;
	}

	public double getHoursSinceLastActivity() {
		return _hoursSinceLastActivity;
	}
	
	public void setHoursSinceLastActivity(double hoursSinceLastActivity) {
		_hoursSinceLastActivity = hoursSinceLastActivity;
	}
	
	public PromptActivityQueryResult getPromptActivityQueryResult() {
		return _promptActivityQueryResult;
	}
	
	public void setPromptActivityQueryResult(PromptActivityQueryResult promptActivityQueryResult) {
		_promptActivityQueryResult = promptActivityQueryResult;
	}
	
	public MobilityActivityQueryResult getMobilityActivityQueryResult() {
		return _mobilityActivityQueryResult;
	}
	
	public void setMobilityActivityQueryResult(MobilityActivityQueryResult mobilityActivityQueryResult) {
		_mobilityActivityQueryResult = mobilityActivityQueryResult;
	}
}
