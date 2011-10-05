package org.ohmage.validator;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * Class to contain the validators for campaign parameters.
 * 
 * @author John Jenkins
 */
public final class CampaignValidators {
	private static final Logger LOGGER = Logger.getLogger(CampaignValidators.class);
	
	public static enum OutputFormat { SHORT, LONG, XML }; 
	
	/**
	 * Default constructor. Made private to prevent instantiation.
	 */
	private CampaignValidators() {}
	
	/**
	 * Validates that a campaign ID is a valid campaign identifier even if the
	 * campaign may not exist.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param campaignId The campaign identifier being validated.
	 * 
	 * @return If the campaign ID is null or whitespace, null is returned.
	 * 		   Otherwise, the campaign ID is returned.
	 * 
	 * @throws ValidationException Thrown if the campaign ID is not null, not
	 * 							   whitespace only, and not a valid campaign
	 * 							   identifier.
	 */
	public static String validateCampaignId(Request request, String campaignId) throws ValidationException {
		LOGGER.info("Validating a campaign ID.");
		
		// If the value is null or whitespace only, return null.
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignId)) {
			return null;
		}
		
		// If the value is a valid URN, meaning that it is a plausible campaign 
		// ID, return the campaign ID back to the caller.
		if(StringUtils.isValidUrn(campaignId.trim())) {
			return campaignId.trim();
		}
		// If the campaign ID is not null, not whitespace only, and not a valid
		// URN, set the request as failed and throw a ValidationException to
		// warn the caller.
		else {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign identifier is invalid: " + campaignId);
			throw new ValidationException("The campaign identifier is invalid: " + campaignId);
		}
	}
	
	/**
	 * Validates that a String list of campaign identifiers is a valid list and
	 * each campaign identifier in the list is a valid identifier.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param campaignIds A String list of campaign identifiers where each
	 * 					  identifier is separated by a 
	 * 					  {@value org.ohmage.request.InputKeys#LIST_ITEM_SEPARATOR}.
	 * 
	 * @return If the campaign IDs String list is null or whitespace only, null
	 * 		   is returned. Otherwise, a List of Strings is returned where each
	 * 		   String in the list represents a campaign identifier.
	 * 
	 * @throws ValidationException Thrown if the campaign ID list String is not
	 * 							   null, not whitespace only, and either cannot
	 * 							   be parced or can be parced but one of the 
	 * 							   values in the list is not a valid campaign
	 * 							   identifier. 
	 */
	public static List<String> validateCampaignIds(Request request, String campaignIds) throws ValidationException {
		LOGGER.info("Validating a list of campaign identifiers.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(campaignIds)) {
			return null;
		}
		
		Set<String> resultSet = new HashSet<String>();
		
		String[] campaignIdArray = campaignIds.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < campaignIdArray.length; i++) {
			String campaignId = validateCampaignId(request, campaignIdArray[i].trim());
			
			if(campaignId != null) {
				resultSet.add(campaignId);
			}
		}
		
		if(resultSet.size() == 0) {
			return null;
		}
		else {
			return new ArrayList<String>(resultSet);
		}
	}
	
	/**
	 * Checks that a running state is a valid campaign running state. If the 
	 * running state is null or whitespace only, then null is returned. If it
	 * isn't a valid running state, a ValidationException is thrown.
	 * 
	 * @param request The request that is validating this running state.
	 * 
	 * @param runningState The running state to validate.
	 * 
	 * @return Returns null if the running state is null or whitespace only. 
	 * 		   Otherwise, it returns the original running state.
	 * 
	 * @throws ValidationException Thrown if the running state is not null nor
	 * 							   whitespace only and isn't a known campaign
	 * 							   running state.
	 */
	public static Configuration.RunningState validateRunningState(Request request, String runningState) throws ValidationException {
		LOGGER.info("Validating a campaign running state.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(runningState)) {
			return null;
		}
		
		try {
			return Configuration.RunningState.getValue(runningState);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_RUNNING_STATE, "The running state is unknown.");
			throw new ValidationException("The running state is unknown: " + runningState);
		}
	}
	
	/**
	 * Checks that a privacy state is a valid campaign privacy state. If the 
	 * privacy state is null or whitespace only, then null is returned. If it
	 * isn't a valid privacy state, a ValidationException is thrown.
	 * 
	 * @param request The request that is validating this privacy state.
	 * 
	 * @param privacyState The privacy state to validate.
	 * 
	 * @return Returns null if the privacy state is null or whitespace only. 
	 * 		   Otherwise, it returns the original privacy state.
	 * 
	 * @throws ValidationException Thrown if the privacy state is not null nor
	 * 							   whitespace only and isn't a known campaign
	 * 							   privacy state.
	 */
	public static Configuration.PrivacyState validatePrivacyState(Request request, String privacyState) throws ValidationException {
		LOGGER.info("Validating a campaign privacy state.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		try {
			return Configuration.PrivacyState.getValue(privacyState);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_PRIVACY_STATE, "The privacy state is unknown.");
			throw new ValidationException("The privacy state is unknown: " + privacyState);
		}
	}
	
	/**
	 * Validates a campaign's XML. If it is null or whitespace, this returns
	 * null. If it is invalid, this throws a ValidationException.
	 * 
	 * @param request The request that is validating this XML.
	 * 
	 * @param xml The campaign XML to be validated.
	 * 
	 * @return Returns null if the XML is null or whitespace only. Otherwise, 
	 * 		   it attempts to validate the XML, and if successful, it returns
	 * 		   the original XML. If unsuccessful, it will throw a 
	 * 		   ValidationException.
	 * 
	 * @throws ValidationException Thrown if the XML is not null, not 
	 * 							   whitespace only, and is invalid.
	 */
	public static String validateXml(Request request, String xml) throws ValidationException {
		LOGGER.info("Validating a campaign's XML.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(xml)) {
			return null;
		}
		
		try {
			Configuration.validateXml(xml);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException("The XML was invalid.", e);
		}
		
		return xml;
	}

	/**
	 * Validates that a campaign description is valid by ensuring that it does
	 * not contain any profanity.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param description The description to validate.
	 * 
	 * @return Returns null if the description is null or whitespace only;
	 * 		   otherwise, it returns the description.
	 * 
	 * @throws ValidationException Thrown if the description contains 
	 * 							   profanity.
	 */
	public static String validateDescription(Request request, String description) throws ValidationException {
		LOGGER.info("Validating a campaign description.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(description)) {
			return null;
		}
		
		if(StringUtils.isProfane(description.trim())) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_DESCRIPTION, "The campaign description contains profanity: " + description);
			throw new ValidationException("The campaign description contains profanity: " + description);
		}
		else {
			return description.trim();
		}
	}
	
	/**
	 * Validates that the output format is a valid output format.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param outputFormat The output format String to be validated.
	 * 
	 * @return Returns null if the output format is null or whitespace only;
	 * 		   otherwise, it returns a new OutputFormat representing the output
	 * 		   format.
	 * 
	 * @throws ValidationException Thrown if the output format is not null, not
	 * 							   whitespace only, and not a valid output 
	 * 							   format.
	 */
	public static OutputFormat validateOutputFormat(Request request, String outputFormat) throws ValidationException {
		LOGGER.info("Validating a campaign's output format.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(outputFormat)) {
			return null;
		}
		
		try {
			return OutputFormat.valueOf(outputFormat.trim().toUpperCase());
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_OUTPUT_FORMAT, "Unknown output format: " + outputFormat);
			throw new ValidationException("Unknown output format: " + outputFormat, e);
		}
	}
	
	/**
	 * Validates that a start date is a valid date and returns a Calendar 
	 * object representing that date.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param startDate The date to be validated.
	 * 
	 * @return Returns null if the start date is null or whitespace only;
	 * 		   otherwise, it returns a Calendar representing the start date.
	 * 
	 * @throws ValidationException Thrown if the start date isn't a decodable
	 * 							   date.
	 */
	public static Calendar validateStartDate(Request request, String startDate) throws ValidationException {
		LOGGER.info("Validating a start date.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			return null;
		}
		
		Date date = StringUtils.decodeDate(startDate);
		if(date == null) {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The start date is invalid: " + startDate);
			throw new ValidationException("The start date is invalid: " + startDate);
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		return calendar;
	}
	
	/**
	 * Validates that an end date is a valid date and returns a Calendar 
	 * object representing that date.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param endDate The date to be validated.
	 * 
	 * @return Returns null if the end date is null or whitespace only;
	 * 		   otherwise, it returns a Calendar representing the end date.
	 * 
	 * @throws ValidationException Thrown if the end date isn't a decodable
	 * 							   date.
	 */
	public static Calendar validateEndDate(Request request, String endDate) throws ValidationException {
		LOGGER.info("Validating an end date.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			return null;
		}
		
		Date date = StringUtils.decodeDate(endDate);
		if(date == null) {
			request.setFailed(ErrorCodes.SERVER_INVALID_DATE, "The end date is invalid: " + endDate);
			throw new ValidationException("The end date is invalid: " + endDate);
		}
		
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		
		return calendar;
	}
	
	/**
	 * Validates that a campaign role is a valid campaign role.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param role The role to be validated.
	 * 
	 * @return Returns null if the role is null or whitespace only; otherwise,
	 * 		   the role is returned.
	 * 
	 * @throws ValidationException Thrown if the role is not a valid campaign
	 * 							   role.
	 */
	public static Configuration.Role validateRole(Request request, String role) throws ValidationException {
		LOGGER.info("Validating a campaign role.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			return null;
		}
		
		try {
			return Configuration.Role.getValue(role);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ROLE, "The campaign role is unknown: " + role);
			throw new ValidationException("The campaign role is unknown: " + role, e);
		}
	}

	/**
	 * Validates that a campaign's XML's prompt ID follows our conventions. It
	 * does _not_ validate that the prompt ID is exists in the XML.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param promptId The prompt ID to be validated.
	 * 
	 * @return Returns null if the prompt ID is null or whitespace only; 
	 * 		   otherwise, it returns the prompt ID.
	 * 
	 * @throws ValidationException Thrown if the prompt ID is not null, not
	 * 							   whitespace only, and doesn't pass syntactic
	 * 							   validation.
	 */
	public static String validatePromptId(Request request, String promptId) throws ValidationException {
		LOGGER.info("Validating a campaign's prompt ID.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(promptId)) {
			return null;
		}
		
		return promptId.trim();
	}
	
	/**
	 * Validates that a string uploaded by a client is a valid JSONArray of
	 * JSONObjects. It does no validation of the individual survey responses.
	 * 
	 * @param request The Request performing this validation.
	 * 
	 * @param uploadValue The string uploaded by the client.
	 * 
	 * @return A list of the survey responses as JSONObjects.
	 * 
	 * @throws ValidationException Thrown if the response was not valid JSON.
	 */
	public static List<JSONObject> validateUploadedJson(Request request, String uploadValue) throws ValidationException {
		LOGGER.info("Validating the uploaded JSON.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(uploadValue)) {
			return null;
		}
		
		JSONArray surveyResponseJson;
		try {
			surveyResponseJson = new JSONArray(uploadValue);
		}
		catch(JSONException e) {
			throw new ValidationException("The uploaded JSON was not a JSONArray.", e);
		}
		int numResponses = surveyResponseJson.length();
		
		List<JSONObject> result = new ArrayList<JSONObject>(numResponses);
		
		for(int i = 0; i < numResponses; i++) {
			try {
			result.add(surveyResponseJson.getJSONObject(i));
			}
			catch(JSONException e) {
				throw new ValidationException("One of the survey responses was not valid JSON.", e);
			}
		}
		
		return result;
	}
}