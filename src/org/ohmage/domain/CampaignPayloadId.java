package org.ohmage.domain;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.request.survey.SurveyResponseRequest;
import org.ohmage.util.StringUtils;

/**
 * This class represents a campaign-specific payload ID.
 *
 * @author John Jenkins
 */
public class CampaignPayloadId implements PayloadId {
	private final String campaignId;
	private final String surveyId;
	private final String promptId;
	
	/**
	 * Defines a campaign payload ID that contains a campaign ID and survey ID
	 * and, optionally, a prompt ID.
	 * 
	 * @param campaignId The campaign ID.
	 * 
	 * @param surveyId The survey ID.
	 * 
	 * @param promptId The prompt ID. Optionally, null, but not only 
	 * 				   whitespace.
	 * 
	 * @throws DomainException The campaign ID or survey ID is null or 
	 * 						   whitespace only or the prompt ID is whitespace
	 * 						   only.
	 */
	public CampaignPayloadId(
			final String campaignId,
			final String surveyId,
			final String promptId)
			throws DomainException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			throw 
				new DomainException(
					"The campaign ID is null or only whitespace.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(surveyId)) {
			throw 
				new DomainException(
					"The survey ID is null or only whitespace.");
		}
		else if((promptId != null) && (promptId.trim().length() == 0)) {
			throw
				new DomainException(
					"The prompt ID is only whitespace.");
		}
		
		this.campaignId = campaignId;
		this.surveyId = surveyId;
		this.promptId = promptId;
	}

	/**
	 * Returns the campaign ID.
	 * 
	 * @return The campaign ID.
	 */
	public String getCampaignId() {
		return campaignId;
	}
	
	/**
	 * Returns the survey ID.
	 * 
	 * @return The survey ID.
	 */
	public String getSurveyId() {
		return surveyId;
	}
	
	/**
	 * Returns the prompt ID.
	 * 
	 * @return The prompt ID, which may be null.
	 */
	public String getPromptId() {
		return promptId;
	}

	/**
	 * Creates a survey_response/read request.
	 * 
	 * @return A survey_response/read request.
	 */
	@Override
	public SurveyResponseReadRequest generateSubRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			boolean callClientRequester,
			final long version,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws DomainException {
		
		Collection<String> surveyIds = null;
		Collection<String> promptIds = null;
		if(promptId == null) {
			surveyIds = new ArrayList<String>(1);
			surveyIds.add(surveyId);
		}
		else {
			promptIds = new ArrayList<String>(1);
			promptIds.add(promptId);
		}
		
		try {
			return
				new SurveyResponseReadRequest(
					httpRequest,
					parameters,
					true,
					campaignId,
					SurveyResponseRequest.URN_SPECIAL_ALL_LIST,
					surveyIds,
					promptIds,
					null,
					startDate,
					endDate,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					null,
					numToSkip,
					numToReturn);
		}
		catch(IOException e) {
			throw new DomainException(
				"There was an error reading the HTTP request.",
				e);
		}
		catch(InvalidRequestException e) {
			throw new DomainException(
				"Error parsing the parameters.",
				e);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException(
				"One of the parameters was invalid.",
				e);
		}
	}
}
