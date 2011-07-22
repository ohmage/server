package org.ohmage.service;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XMLException;
import nu.xom.XPathException;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.dao.CampaignDaos;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.Request;

/**
 * This class contains the services that pertain to campaigns.
 * 
 * @author John Jenkins
 */
public class CampaignServices {
	private static final String PATH_CAMPAIGN_URN = "/campaign/campaignUrn";
	private static final String PATH_CAMPAIGN_NAME = "/campaign/campaignName";
	
	/**
	 * Default constructor. Private to prevent instantiation.
	 */
	private CampaignServices() {}
	
	/**
	 * Class for handling campaign ID and name combinations.
	 * 
	 * @author John Jenkins
	 */
	public static final class CampaignIdAndName {
		private final String id;
		private final String name;
		
		/**
		 * Creates a new ID-name association.
		 * 
		 * @param campaignId The campaign's unique identifier.
		 * 
		 * @param campaignName The campaign's name.
		 */
		public CampaignIdAndName(String campaignId, String campaignName) {
			id = campaignId;
			name = campaignName;
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
			String runningState, String privacyState, List<String> classIds, String creatorUsername) throws ServiceException {
		try {
			CampaignDaos.createCampaign(campaignId, name, xml, description, runningState, privacyState, classIds, creatorUsername);
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
					request.setFailed(ErrorCodes.CAMPAIGN_INVALID_XML, "The campaign already exists: " + campaignId);
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
	 * Gets the campaign's URN and name from the campaign XML.<br />
	 * <br />
	 * Note: The campaign should have been validated before this point.
	 * 
	 * @param request The request that desires the campaign's URN.
	 * 
	 * @param xml The XML definition of this campaign.
	 * 
	 * @return A CampaignIdAndName object with the campaign's URN and name.
	 * 
	 * @throws ServiceException Thrown if there is an error parsing the XML. 
	 * 							This should never happen as the XML should have
	 * 							been validated before this call is made.
	 */
	public static CampaignIdAndName getCampaignUrnAndNameFromXml(Request request, String xml) throws ServiceException {
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
		
		return new CampaignIdAndName(campaignUrn, campaignName);
	}
}