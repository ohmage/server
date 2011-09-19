package org.ohmage.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;
import nu.xom.XPathException;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.CampaignPrivacyStateCache;
import org.ohmage.cache.CampaignRoleCache;
import org.ohmage.cache.CampaignRunningStateCache;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.domain.User;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class contains the services that pertain to campaigns.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public class CampaignServices {
	private static final String PATH_CAMPAIGN_URN = "/campaign/campaignUrn";
	private static final String PATH_CAMPAIGN_NAME = "/campaign/campaignName";
	private static final String PATH_ICON_URL = "/campaign/iconUrl";
	private static final String PATH_AUTHORED_BY = "/campaign/authoredBy";
	
	/**
	 * Default constructor. Private to prevent instantiation.
	 */
	private CampaignServices() {}
	
	/**
	 * Class for handling campaign ID and name combinations.
	 * 
	 * @author John Jenkins
	 */
	public static final class CampaignMetadata {
		private final String id;
		private final String name;
		private final String iconUrl;
		private final String authoredBy;
		
		/**
		 * Creates a new ID-name association.
		 * 
		 * @param campaignId The campaign's unique identifier.
		 * 
		 * @param campaignName The campaign's name.
		 * 
		 * @param iconUrl The campaign's icon's URL. Optional.
		 * 
		 * @param authoredBy The name of the organization/person that authored
		 * 					 this campaign.
		 */
		public CampaignMetadata(String campaignId, String campaignName, String iconUrl, String authoredBy) {
			id = campaignId;
			name = campaignName;
			this.iconUrl = iconUrl;
			this.authoredBy = authoredBy;
		}
		
		/**
		 * Returns the campaign's unique identifier.
		 * 
		 * @return The campaign's unique identifier.
		 */
		public String getCampaignId() {
			return id;
		}
		
		/**
		 * Returns the campaign's name.
		 * 
		 * @return The campaign's name.
		 */
		public String getCampaignName() {
			return name;
		}
		
		/**
		 * Returns the campaign's icon's URL if it exists.
		 * 
		 * @return The campaign's icon's URL or null if it doesn't exist.
		 */
		public String getIconUrl() {
			return iconUrl;
		}
		
		/**
		 * Returns the name of the organization or person that authored this 
		 * campaign.
		 * 
		 * @return The campaign's author or null if it doesn't exist.
		 */
		public String getAuthoredBy() {
			return authoredBy;
		}
	}
	
	/**
	 * Creates a new campaign.
	 * 
	 * @param request The request that is creating the campaign.
	 * 
	 * @param campaignId The new campaign's unique identifier.
	 * 
	 * @param name The new campaign's name.
	 * 
	 * @param xml The new campaign's XML.
	 * 
	 * @param description The new campaign's description.
	 * 
	 * @param runningState The new campaign's initial running state.
	 * 
	 * @param privacyState The new campaign's initial privacy state.
	 * 
	 * @param classIds A List of class identifiers for classes that are going
	 * 				   to be initially associated with the campaign.
	 * 
	 * @param creatorUsername The username of the user that will be set as the
	 * 						  author.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void createCampaign(Request request, String campaignId, String name, String xml, String description, 
			String iconUrl, String authoredBy, 
			CampaignRunningStateCache.RunningState runningState, 
			CampaignPrivacyStateCache.PrivacyState privacyState, 
			List<String> classIds, String creatorUsername) throws ServiceException {
		try {
			CampaignDaos.createCampaign(campaignId, name, xml, description, iconUrl, authoredBy, runningState, privacyState, classIds, creatorUsername);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if a campaign already exists or not based on the 'shouldExist'
	 * flag.
	 * 
	 * @param request The request that is performing this check.
	 * 
	 * @param campaignId The unique identifier of a campaign whose existence is
	 * 					 being checked.
	 * 
	 * @param shouldExist Whether or not the campaign should already exist.
	 * 
	 * @throws ServiceException Thrown if there is an error, if the campaign
	 * 							exists and it shouldn't, or if the campaign
	 * 							doesn't exist and it should.
	 */
	public static void checkCampaignExistence(Request request, String campaignId, boolean shouldExist) throws ServiceException {
		try {
			if(CampaignDaos.getCampaignExists(campaignId)) {
				if(! shouldExist) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, "A campaign with the same ID already exists: " + campaignId);
					throw new ServiceException("The campaign already exists.");
				}
			}
			else {
				if(shouldExist) {
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The campaign does not exist: " + campaignId);
					throw new ServiceException("The campaign does not exist.");
				}
			}
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Checks if the existence of every campaign in a List of campaign IDs
	 * matches the parameterized 'shouldExist'.
	 * 
	 * @param request The request that is performing this service.
	 * 
	 * @param campaignIds A List of campaign IDs to check.
	 * 
	 * @param shouldExist Whether or not every campaign in the List should 
	 * 					  exist or not.
	 * 
	 * @throws ServiceException Thrown if any of the campaigns exist and they
	 * 							shouldn't or if any of the campaigns don't 
	 * 							exist and they should.
	 */
	public static void checkCampaignsExistence(Request request, Collection<String> campaignIds, boolean shouldExist) throws ServiceException {
		for(String campaignId : campaignIds) {
			checkCampaignExistence(request, campaignId, shouldExist);
		}
	}
	
	/**
	 * Retrieves the XML for a campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return Returns the XML for the campaign. If the campaign doens't exist,
	 * 		   null is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static String getCampaignXml(Request request, String campaignId) throws ServiceException {
		try {
			return CampaignDaos.getXml(campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Retrieves the name of a campaign.
	 * 
	 * @param request The Request performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @return Returns the name of the campaign. If the campaign doesn't exist
	 * 		   null is returned.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static String getCampaignName(Request request, String campaignId) throws ServiceException {
		try {
			return CampaignDaos.getName(campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}

	/**
	 * Ensures that the prompt ID exists in the campaign XML of the campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign whose XML is 
	 * 					 being checked.
	 * 
	 * @param promptId A prompt ID that is unique to the campaign's XML and is
	 * 				   being checked for existance.
	 * 
	 * @throws ServiceException Throws a 
	 * 							{@value org.ohmage.annotator.ErrorCodes#CAMPAIGN_NOT_FOUND}
	 * 							if the campaign doesn't exist or a
	 * 							{@value org.ohmage.annotator.ErrorCodes##CAMPAIGN_UNKNOWN_PROMPT_ID}
	 * 							if the prompt ID doesn't exist in the 
	 * 							campaign's XML. Also, thrown if there is an 
	 * 							error.
	 */
	public static void ensurePromptExistsInCampaign(Request request, String campaignId, String promptId) throws ServiceException {
		// Get the XML.
		String xml;
		try {
			xml = CampaignDaos.getXml(campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
		if(xml == null) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "There is no such campaign with the campaign ID: " + campaignId);
			throw new ServiceException("There is no such campaign with the campaign ID: " + campaignId);
		}
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(xml));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			request.setFailed();
			throw new ServiceException("Unable to read XML.", e);
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			request.setFailed();
			throw new ServiceException("Invalid XML.", e);
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			request.setFailed();
			throw new ServiceException("XML cannot be parsed.", e);
		}
		
		// Find all the prompt IDs with the parameterized promptId.
		Element root = document.getRootElement();
		Nodes nodes = root.query("/campaign/surveys/survey/contentList/prompt[id='" + promptId + "']");
		if(nodes.size() == 0) {
			request.setFailed(ErrorCodes.SURVEY_INVALID_PROMPT_ID, "The following prompt ID is not part of the campaign's XML: " + promptId);
			throw new ServiceException("The following prompt ID is not part of the campaign's XML: " + promptId);
		}
	}
	
	/**
	 * Gets the campaign's URN, name, and icon URL, if it exists, from the 
	 * campaign XML.<br />
	 * <br />
	 * Note: The campaign should have been validated before this point.
	 * 
	 * @param request The request that desires the campaign's URN.
	 * 
	 * @param xml The XML definition of this campaign.
	 * 
	 * @return A CampaignMetadata object with the campaign's URN and name.
	 * 
	 * @throws ServiceException Thrown if there is an error parsing the XML. 
	 * 							This should never happen as the XML should have
	 * 							been validated before this call is made.
	 */
	public static CampaignMetadata getCampaignMetadataFromXml(Request request, String xml) throws ServiceException {
		// Generate a builder that will build the XML Document.
		Builder builder;
		try {
			builder = new Builder();
		}
		catch(XMLException e) {
			request.setFailed();
			throw new ServiceException("No satisfactory XML parser is installed on the system!", e);
		}
		
		// Build the XML Document that we will parse for the campaign name.
		Document xmlDocument;
		try {
			xmlDocument = builder.build(new StringReader(xml));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			request.setFailed();
			throw new ServiceException("The XML String being passed into this function was unreadable.", e);
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			request.setFailed();
			throw new ServiceException("Validation failed, but XML validation shouldn't have been enabled here as it should have already been done.", e);
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			request.setFailed();
			throw new ServiceException("The XML is not well-formed, but it should have been validated before reaching this point.", e);
		}
		
		// Get the campaign's URN.
		String campaignUrn;
		try {
			campaignUrn = xmlDocument.getRootElement().query(PATH_CAMPAIGN_URN).get(0).getValue(); 
		}
		catch(XPathException e) {
			request.setFailed();
			throw new ServiceException("The PATH to get the campaign urn is invalid.", e);
		}
		catch(IndexOutOfBoundsException e) {
			request.setFailed();
			throw new ServiceException("There is no campaign URN field in the XML, but it should have already been validated.", e);
		}
		
		// Get the campaign's name.
		String campaignName;
		try {
			campaignName = xmlDocument.getRootElement().query(PATH_CAMPAIGN_NAME).get(0).getValue();
		}
		catch(XPathException e) {
			request.setFailed();
			throw new ServiceException("The PATH to get the campaign name is invalid.", e);
		}
		catch(IndexOutOfBoundsException e) {
			request.setFailed();
			throw new ServiceException("There is no campaign name field in the XML, but it should have already been validated.", e);
		}
		
		// Get the campaign's icon URL if it exists.
		String iconUrl = null;
		try {
			iconUrl = xmlDocument.getRootElement().query(PATH_ICON_URL).get(0).getValue();
		}
		catch(XPathException e) {
			request.setFailed();
			throw new ServiceException("The PATH to get the campaign icon URL is invalid.", e);
		}
		catch(IndexOutOfBoundsException e) {
			// There is no campaign icon URL which is acceptable.
		}
		
		// Get the campaign's author if it exists.
		String authoredBy = null;
		try {
			authoredBy = xmlDocument.getRootElement().query(PATH_AUTHORED_BY).get(0).getValue();
		}
		catch(XPathException e) {
			request.setFailed();
			throw new ServiceException("The PATH to get the campaig's author is invalid.", e);
		}
		catch(IndexOutOfBoundsException e) {
			// There is no campaign icon URL which is acceptable.
		}
		
		return new CampaignMetadata(campaignUrn, campaignName, iconUrl, authoredBy);
	}
	
	/**
	 * Verifies that the user belongs to a campaign specified by the campaign id and that the campaign referenced by the id 
	 * has a value that matches the allowed running state.
	 * 
	 * @param request The request that will be failed if the validation does not pass.
	 * @param user The user containing campaign and running state metadata to validate against.
	 * @param campaignId The campaign id in question.
	 * @param allowedRunningState The allowed running state for the above campaign.
	 * @throws ServiceException
	 */
	public static void verifyAllowedRunningState(Request request, User user, String campaignId, CampaignRunningStateCache.RunningState allowedRunningState)
		throws ServiceException {
		
		if(user.getCampaignsAndRoles() == null) { // logical error
			request.setFailed();
			throw new ServiceException("The User in the Request has not been populated with his or her associated campaigns and roles", true);
		}
		
		if(! user.getCampaignsAndRoles().containsKey(campaignId)) { // at this point (service-layer) this is also a logical error
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "User does not belong to campaign.");
			throw new ServiceException("The User in the Request does not belong to the campaign " + campaignId);
		}
		
		if(! user.getCampaignsAndRoles().get(campaignId).getCampaign().getRunningState().equals(allowedRunningState)) {
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_RUNNING_STATE, "Campaign does not have the allowed running state: " + allowedRunningState);
			throw new ServiceException("Campaign does not have the allowed running state: " + allowedRunningState);
		}
	}
	
	/**
	 * Verifies that the campaign ID and name in some XML file are the same as
	 * the ones we have on record.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign whose XML is 
	 * 					 being changed.
	 * 
	 * @param newXml The new XML that may replace the old XML for some 
	 * 				 campaign.
	 * 
	 * @throws ServiceException Thrown if the ID or name are different than
	 *		   what we currently have on record or if there is an error.
	 */
	public static void verifyTheNewXmlIdAndNameAreTheSameAsTheCurrentIdAndName(Request request, String campaignId, String newXml) throws ServiceException {
		try {
			// Retrieve the ID and name from the current XML.
			CampaignMetadata newCampaignIdAndName = getCampaignMetadataFromXml(request, newXml);
			
			// We check the XML's ID against the given ID and the XML's name
			// against what the DAO reports as the name. We do not check 
			// against the actual saved XML as that would be less efficient. 
			// The only time these would not be the same is when there was an
			// integrity issue in the database.
			if(! newCampaignIdAndName.getCampaignId().equals(campaignId)) {
				request.setFailed(ErrorCodes.CAMPAIGN_XML_HEADER_CHANGED, "The campaign's ID in the new XML must be the same as the original XML.");
				throw new ServiceException("The campaign's ID in the new XML must be the same as the original XML.");
			}
			
			if(! newCampaignIdAndName.getCampaignName().equals(CampaignDaos.getName(campaignId))) {
				request.setFailed(ErrorCodes.CAMPAIGN_XML_HEADER_CHANGED, "The campaign's name in the new XML must be the same as the original XML.");
				throw new ServiceException("The campaign's name in the new XML must be the same as the original XML.");
			}
			
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that the user belongs to a campaign specified by the campaign
	 * id and that the campaign referenced by the id has a timestamp value 
	 * that matches the campaign creation timestamp.
	 * 
	 * @param request The request that will be failed if the validation does
	 * not pass.
	 * @param user The user containing campaign and campaign creation timestmap
	 * metadata to validate against.
	 * @param campaignId The campaign id in question.
	 * @param allowedRunningState The campaign creation timestamp provided to the
	 * API call.
	 * @throws ServiceException If the user does not contain a CampaignAndRoles
	 * object, or if the user does not belong to the campaign, or the campaign
	 * creation timestamp does not match what is currently stored for the 
	 * campaign. 
	 */
	public static void verifyCampaignCreationTimestamp(Request request, User user, String campaignId, String campaignCreationTimestamp)
		throws ServiceException {
		
		if(user.getCampaignsAndRoles() == null) { // logical error
			request.setFailed();
			throw new ServiceException("The User in the Request has not been populated with his or her associated campaigns and roles", true);
		}
		
		if(! user.getCampaignsAndRoles().containsKey(campaignId)) { // at this point (service-layer) this is also a logical error
			request.setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "User does not belong to campaign.");
			throw new ServiceException("The User in the Request does not belong to the campaign " + campaignId);
		}
		
		// equals() is used here because any timestamp that is not equal to the timestamp stored in the db is invalid
		// e.g., if the timestamp provided to this method was newer than the timestamp stored with the campaign, it 
		// would mean a logical error on the part of some client
		if(! user.getCampaignsAndRoles().get(campaignId).getCampaign().getCampaignCreationTimestamp().equals(campaignCreationTimestamp)) {
			request.setFailed(ErrorCodes.CAMPAIGN_OUT_OF_DATE, "Campaign does not have the timestamp: " + campaignCreationTimestamp);
			throw new ServiceException("Campaign does not have the timestamp: " + campaignCreationTimestamp);
		}
	}
	
	/**
	 * Updates a campaign. The 'request' and 'campaignId' are required; 
	 * however, the remaining parameters may be null indicating that they 
	 * should not be updated.
	 * 
	 * @param request The Request that is performing this service.
	 *  
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param xml The new XML for the campaign or null if the XML should not be
	 * 			  updated.
	 * 
	 * @param description The new description for the campaign or null if the
	 * 					  description should not be updated.
	 * 
	 * @param runningState The new running state for the campaign or null if 
	 * 					   the running state should not be updated.
	 * 
	 * @param privacyState The new privacy state for the campaign or null if 
	 * 					   the privacy state should not be updated.
	 * 
	 * @param usersAndRolesToAdd A map of usernames to a list of roles that the
	 * 							 users should be granted in the campaign or 
	 * 							 null if no users should be granted any new 
	 * 							 roles.
	 * 
	 * @param usersAndRolesToRemove A map of usernames to a list of roles that
	 * 								should be revoked from the user in the
	 * 								campaign or null if no users should have 
	 * 								any of their roles revoked.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void updateCampaign(Request request, 
			String campaignId, String xml, String description, 
			CampaignRunningStateCache.RunningState runningState, 
			CampaignPrivacyStateCache.PrivacyState privacyState, 
			Collection<String> classIds, 
			Map<String, Set<CampaignRoleCache.Role>> usersAndRolesToAdd, 
			Map<String, Set<CampaignRoleCache.Role>> usersAndRolesToRemove) throws ServiceException {
		try {
			CampaignDaos.updateCampaign(campaignId, xml, description, runningState, privacyState, classIds, usersAndRolesToAdd, usersAndRolesToRemove);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Finds the configuration for the campaign identified by the campaign id.
	 * 
	 * @param campaignId The campaign id to use for lookup.
	 * @return a Configuration instance created from the XML for the campaign.
	 * @throws ServiceException If an error occurred in the data layer.
	 */
	public static Configuration findCampaignConfiguration(Request request, String campaignId) throws ServiceException {
		try {
			
			return CampaignDaos.findCampaignConfiguration(campaignId);
		}
		catch(DataAccessException e) {
				request.setFailed();
				throw new ServiceException(e);
		}
	}
		
	/**
	 * Deletes a campaign.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param campaignId The unique identifier for the campaign.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void deleteCampaign(Request request, String campaignId) throws ServiceException {
		try {
			CampaignDaos.deleteCampaign(campaignId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}