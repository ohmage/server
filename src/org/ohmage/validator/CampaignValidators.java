package org.ohmage.validator;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.CampaignValidator;
import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;
import org.xml.sax.SAXException;

/**
 * Class to contain the validators for campaign parameters.
 * 
 * @author John Jenkins
 */
public final class CampaignValidators {
	private static final Logger LOGGER = Logger.getLogger(CampaignValidators.class);
	
	private static final String CAMPAIGN_XML_SCHEMA_FILENAME = "/opt/aw/conf/configuration.xsd";
	
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
		if(StringUtils.isValidUrn(campaignId)) {
			return campaignId;
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
			String campaignId = validateCampaignId(request, campaignIdArray[i]);
			
			if(campaignId != null) {
				resultSet.add(campaignId);
			}
		}
		
		return new ArrayList<String>(resultSet);
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
	public static String validateRunningState(Request request, String runningState) throws ValidationException {
		LOGGER.info("Validating a campaign running state.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(runningState)) {
			return null;
		}
		
		if(CampaignRunningStateCache.instance().getKeys().contains(runningState)) {
			return runningState;
		}
		else {
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
	public static String validatePrivacyState(Request request, String privacyState) throws ValidationException {
		LOGGER.info("Validating a campaign privacy state.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		if(CampaignPrivacyStateCache.instance().getKeys().contains(privacyState)) {
			return privacyState;
		}
		else {
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
			(new CampaignValidator()).run(xml, CAMPAIGN_XML_SCHEMA_FILENAME);
		}
		catch(ValidityException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, e.getMessage());
			throw new ValidationException("The XML was invalid.", e);
		} 
		catch(SAXException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, e.getMessage());
			throw new ValidationException("The XML was invalid.", e);
		}
		catch(ParsingException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, e.getMessage());
			throw new ValidationException("The XML was invalid.", e);
		}
		catch(IllegalStateException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, e.getMessage());
			throw new ValidationException("The XML was invalid.", e);
		}
		catch(IllegalArgumentException e) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, e.getMessage());
			throw new ValidationException("The XML was invalid.", e);
		}
		
		return xml;
	}
}