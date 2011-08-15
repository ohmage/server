package org.ohmage.domain.upload;

import java.util.Collections;
import java.util.List;

/**
 * Immutable bean-style wrapper for converted survey upload JSON messages. This
 * class exists in order to decouple the data layer from JSON (otherwise known
 * as the DataTransferObject pattern). Some of the properties of this class are
 * JSON masquerading as Strings, but that is only because the data is 
 * ultimately stored in a schema-less manner via the data layer. 
 * 
 * @author Joshua Selsky
 * @see org.ohmage.domain.upload.PromptResponse
 * @see org.ohmage.domain.configuration.Configuration
 */
public class SurveyResponse {
	private String date;
	private long epochTime;
	private String timezone;
	private String locationStatus;
	private String location;
	private String survey;
	private String surveyId;
	private String launchContext;
	private List<PromptResponse> promptResponses;
	
	/**
	 * Default constructor. Assumes all arguments have been previously
	 * validated.
	 * @param date  The date property from the metadata portion of an upload.
	 * @param epochTime The time property from the metadata portion of an
	 * upload.
	 * @param timezone The timezone property from the metadata portion of an
	 * upload
	 * @param locationStatus The location_status property from the metadata 
	 * portion of an upload.
	 * @param location The location property from the metadata portion of an 
	 * upload. The location is stored as raw JSON.
	 * @param survey The entire survey from an upload. The survey is stored
	 * as raw JSON.
	 * @param surveyId The survey_id property from an upload.
	 * @param launchContext The survey_launch_context from an upload. Stored
	 * as raw JSON.
	 * @param promptResponses A List of PromptUploads.
	 */
	public SurveyResponse(String date, long epochTime, String timezone, String locationStatus, String location, String survey,
		String surveyId, String launchContext, List<PromptResponse> promptResponses) {
		
		this.date = date;
		this.epochTime = epochTime;
		this.timezone = timezone;
		this.locationStatus = locationStatus;
		this.location = location;
		this.survey = survey;
		this.surveyId = surveyId;
		this.launchContext = launchContext;
		this.promptResponses = promptResponses; // TODO deep copy: http://javatechniques.com/blog/faster-deep-copies-of-java-objects/
	}

	public String getDate() {
		return date;
	}

	public long getEpochTime() {
		return epochTime;
	}

	public String getTimezone() {
		return timezone;
	}

	public String getLocationStatus() {
		return locationStatus;
	}

	public String getLocation() {
		return location;
	}

	public String getSurvey() {
		return survey;
	}

	public String getSurveyId() {
		return surveyId;
	}

	public String getLaunchContext() {
		return launchContext;
	}

	public List<PromptResponse> getPromptResponses() {
		return Collections.unmodifiableList(promptResponses);
	}

	@Override
	public String toString() {
		return "SurveyResponse [date=" + date + ", epochTime=" + epochTime
				+ ", timezone=" + timezone + ", locationStatus="
				+ locationStatus + ", location=" + location + ", survey="
				+ survey + ", surveyId=" + surveyId + ", launchContext="
				/* Omitting the prompt responses to avoid huge error messages */
				+ launchContext + /*", promptResponses=" + promptResponses +*/ "]";
	}
}
