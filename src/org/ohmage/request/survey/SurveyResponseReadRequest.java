package org.ohmage.request.survey;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.SurveyResponsePrivacyStateCache;
import org.ohmage.dao.SurveyResponseReadDao;
import org.ohmage.domain.configuration.Configuration;
import org.ohmage.domain.survey.read.SurveyResponseReadResult;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseReadServices;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.validator.DateValidators;
import org.ohmage.validator.SurveyResponseReadValidators;

/**
 * <p>A request to read survey response data</p>
 * 
 * TODO Javadoc
 *  
 * @author Joshua Selsky
 */
public final class SurveyResponseReadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(SurveyResponseReadRequest.class);
	
	private final Date startDate;
	private final Date endDate;
	private final String campaignUrn;
	private final List<String> userList;
	private final List<String> promptIdList;
	private final List<String> surveyIdList;
	private final List<String> columnList;
	private final String outputFormat;
	private final Boolean prettyPrint;
	private final Boolean suppressMetadata;
	private final Boolean returnId;
	private final String sortOrder;
	private final String privacyState;
	private final Boolean collapse;
	
	private Configuration configuration;
	
	private static final List<String> ALLOWED_COLUMN_URN_LIST;
	private static final List<String> ALLOWED_OUTPUT_FORMAT_LIST;
	private static final List<String> ALLOWED_SORT_ORDER_LIST;
	
	public static final String URN_SPECIAL_ALL = "urn:ohmage:special:all";
	public static final List<String> URN_SPECIAL_ALL_LIST;
	
	public static final String URN_CONTEXT_CLIENT = "urn:ohmage:context:client";
	public static final String URN_CONTEXT_TIMESTAMP = "urn:ohmage:context:timestamp";
	public static final String URN_CONTEXT_TIMEZONE = "urn:ohmage:context:timezone";
	public static final String URN_CONTEXT_UTC_TIMESTAMP = "urn:ohmage:context:utc_timestamp";
	public static final String URN_CONTEXT_LAUNCH_CONTEXT_LONG = "urn:ohmage:context:launch_context:long";
	public static final String URN_CONTEXT_LAUNCH_CONTEXT_SHORT = "urn:ohmage:context:launch_context:short";
	public static final String URN_CONTEXT_CONTEXT_LOCATION_STATUS = "urn:ohmage:context:location:status";
	public static final String URN_CONTEXT_LOCATION_LATITUDE = "urn:ohmage:context:location:latitude";
	public static final String URN_CONTEXT_LOCATION_LONGITUDE = "urn:ohmage:context:location:longitude";
	public static final String URN_CONTEXT_LOCATION_TIMESTAMP = "urn:ohmage:context:location:timestamp";
	public static final String URN_CONTEXT_LOCATION_ACCURACY = "urn:ohmage:context:location:accuracy";
	public static final String URN_CONTEXT_LOCATION_PROVIDER = "urn:ohmage:context:location:provider";
	public static final String URN_USER_ID = "urn:ohmage:user:id";
	public static final String URN_SURVEY_ID = "urn:ohmage:survey:id";
	public static final String URN_SURVEY_TITLE = "urn:ohmage:survey:title";
	public static final String URN_SURVEY_DESCRIPTION = "urn:ohmage:survey:description";
	public static final String URN_SURVEY_PRIVACY_STATE = "urn:ohmage:survey:privacy_state";
	public static final String URN_REPEATABLE_SET_ID = "urn:ohmage:repeatable_set:id";
	public static final String URN_REPEATABLE_SET_ITERATION = "urn:ohmage:repeatable_set:iteration";
	public static final String URN_PROMPT_RESPONSE = "urn:ohmage:prompt:response";
	
	// TODO - should these be in InputKeys?
	public static final String OUTPUT_FORMAT_JSON_ROWS = "json-rows";
	public static final String OUTPUT_FORMAT_JSON_COLUMNS = "json-columns";
	public static final String OUTPUT_FORMAT_CSV = "csv";
	
	static {
		ALLOWED_COLUMN_URN_LIST = Arrays.asList(new String[] {
			URN_CONTEXT_CLIENT, URN_CONTEXT_TIMESTAMP, URN_CONTEXT_TIMEZONE, URN_CONTEXT_UTC_TIMESTAMP,
			URN_CONTEXT_LAUNCH_CONTEXT_LONG, URN_CONTEXT_LAUNCH_CONTEXT_SHORT, URN_CONTEXT_CONTEXT_LOCATION_STATUS,
			URN_CONTEXT_LOCATION_LATITUDE, URN_CONTEXT_LOCATION_LONGITUDE, URN_CONTEXT_LOCATION_TIMESTAMP,
			URN_CONTEXT_LOCATION_ACCURACY, URN_CONTEXT_LOCATION_PROVIDER, URN_USER_ID, URN_SURVEY_ID,
			URN_SURVEY_TITLE, URN_SURVEY_DESCRIPTION, URN_SURVEY_PRIVACY_STATE, URN_REPEATABLE_SET_ID,
			URN_REPEATABLE_SET_ITERATION, URN_PROMPT_RESPONSE
		});
		
		ALLOWED_OUTPUT_FORMAT_LIST = Arrays.asList(new String[] {OUTPUT_FORMAT_JSON_ROWS, OUTPUT_FORMAT_JSON_COLUMNS, OUTPUT_FORMAT_CSV});
		
		ALLOWED_SORT_ORDER_LIST = Arrays.asList(new String[] {InputKeys.SORT_ORDER_SURVEY, InputKeys.SORT_ORDER_TIMESTAMP, InputKeys.SORT_ORDER_USER});
		
		URN_SPECIAL_ALL_LIST = Collections.unmodifiableList(Arrays.asList(new String[]{URN_SPECIAL_ALL}));
	}
	
	/**
	 * Creates a survey response read request.
	 * 
	 * @param httpRequest  The request to retrieve parameters from.
	 */
	public SurveyResponseReadRequest(HttpServletRequest httpRequest) {
		// Handle user-password or token-based authentication
		super(httpRequest, TokenLocation.EITHER, false);
		
		String tStartDate = httpRequest.getParameter(InputKeys.START_DATE);
		Date tStartDateAsDate = null;
		String tEndDate = httpRequest.getParameter(InputKeys.END_DATE);
		Date tEndDateAsDate = null;
		String tCampaignUrn = httpRequest.getParameter(InputKeys.CAMPAIGN_URN);
		String tUserList = httpRequest.getParameter(InputKeys.USER_LIST);
		List<String> tUserListAsList = null;
		String tPromptIdList = httpRequest.getParameter(InputKeys.PROMPT_ID_LIST);
		List<String> tPromptIdListAsList = null;
		String tSurveyIdList = httpRequest.getParameter(InputKeys.SURVEY_ID_LIST);
		List<String> tSurveyIdListAsList = null;
		String tColumnList = httpRequest.getParameter(InputKeys.COLUMN_LIST);
		List<String> tColumnListAsList = null;
		String tOutputFormat = httpRequest.getParameter(InputKeys.OUTPUT_FORMAT);
		String tPrettyPrint = httpRequest.getParameter(InputKeys.PRETTY_PRINT);
		Boolean tPrettyPrintAsBoolean = null;
		String tSuppressMetadata = httpRequest.getParameter(InputKeys.SUPPRESS_METADATA);
		Boolean tSuppressMetadataAsBoolean = null;
		String tReturnId = httpRequest.getParameter(InputKeys.RETURN_ID);
		Boolean tReturnIdAsBoolean = null;
		String tSortOrder = httpRequest.getParameter(InputKeys.SORT_ORDER);
		String tPrivacyState = httpRequest.getParameter(InputKeys.PRIVACY_STATE);
		String tCollapse = httpRequest.getParameter(InputKeys.COLLAPSE);
		Boolean tCollapseAsBoolean = null;
		
		if(! isFailed()) {
		
			LOGGER.info("Creating a survey response read request.");
			
			try {
				// FIXME constant-ify the hard-coded strings, maybe even for
				// logging messages (possibly AOP-ify logging)
				// Also, move validation to the SurveyResponseReadValidators
				// And check for multiple copies of params
				
				LOGGER.info("Making sure campaign_urn parameter is present.");
				
				if(tCampaignUrn == null) {
					setFailed(ErrorCodes.CAMPAIGN_INVALID_ID, "The required campaign URN was not present.");
					throw new ValidationException("The required campaign URN was not present.");
				}
				
				LOGGER.info("Validating start_date and end_date parameters.");
					
				try {
					tStartDateAsDate = DateValidators.validateISO8601Date(tStartDate);
					tEndDateAsDate = DateValidators.validateISO8601Date(tEndDate);
					
					if((tStartDateAsDate != null && tEndDateAsDate == null) || (tStartDateAsDate == null && tEndDateAsDate != null)) {
						setFailed(ErrorCodes.SERVER_INVALID_DATE, "Missing start_date or end_date");
					}
				} 
				catch (ValidationException e) { // FIXME the DateValidators methods should take a Request parameter to fail
					setFailed(ErrorCodes.SERVER_INVALID_DATE, "Invalid start_date or end_date");
					throw e;
				}
				
				LOGGER.info("Validating privacy_state parameter.");
					
				if(! SurveyResponsePrivacyStateCache.instance().getKeys().contains(tPrivacyState)) {
					setFailed(ErrorCodes.SURVEY_INVALID_PRIVACY_STATE, "Found unknown privacy_state: " + tPrivacyState);
					throw new ValidationException("Found unknown privacy_state: " + tPrivacyState);
				}
				
				LOGGER.info("Validating user_list parameter.");
				
				tUserListAsList = SurveyResponseReadValidators.validateUserList(this, tUserList);
				
				LOGGER.info("Validating prompt_id_list and survey_id_list parameters.");
				
				List<String> tList = SurveyResponseReadValidators.validatePromptIdSurveyIdLists(this, tPromptIdList, tSurveyIdList);
				
				// Now check whether it's a prompt id list or a survey id list
				if(tPromptIdList == null) {
					tSurveyIdListAsList = tList;
					tPromptIdListAsList = Collections.emptyList();
				}
				else {
					tSurveyIdListAsList = Collections.emptyList();
					tPromptIdListAsList = tList;
				}
				
				LOGGER.info("Validating column_list parameter.");
				
				tColumnListAsList = SurveyResponseReadValidators.validateColumnList(this, tColumnList, ALLOWED_COLUMN_URN_LIST);
				
				LOGGER.info("Validating output_format parameter.");
				
				tOutputFormat = SurveyResponseReadValidators.validateOutputFormat(this, tOutputFormat, ALLOWED_OUTPUT_FORMAT_LIST);

				LOGGER.info("Validating sort_order parameter.");
				
				tSortOrder = SurveyResponseReadValidators.validateSortOrder(this, tSortOrder, ALLOWED_SORT_ORDER_LIST); 
				
				LOGGER.info("Validating suppress_metadata parameter.");
				
				tSuppressMetadataAsBoolean = SurveyResponseReadValidators.validateSuppressMetadata(this, tSuppressMetadata);
				
				LOGGER.info("Validating pretty_print parameter.");
				
				tPrettyPrintAsBoolean = SurveyResponseReadValidators.validatePrettyPrint(this, tPrettyPrint);

				LOGGER.info("Validating return_id parameter.");
				
				tReturnIdAsBoolean = SurveyResponseReadValidators.validateReturnId(this, tReturnId);

				LOGGER.info("Validating collapse parameter.");
				
				tCollapseAsBoolean = SurveyResponseReadValidators.validateCollapse(this, tCollapse);
			} 
			
			catch (ValidationException e) {
				
				LOGGER.info(e);
				
			}
		}
		
		this.campaignUrn = tCampaignUrn;
		this.collapse = tCollapseAsBoolean;
		this.columnList = tColumnListAsList;
		this.endDate = tEndDateAsDate;
		this.outputFormat = tOutputFormat;
		this.prettyPrint = tPrettyPrintAsBoolean;
		this.privacyState = tPrivacyState;
		this.promptIdList = tPromptIdListAsList;
		this.returnId = tReturnIdAsBoolean;
		this.sortOrder = tSortOrder;
		this.startDate = tStartDateAsDate;
		this.suppressMetadata = tSuppressMetadataAsBoolean;
		this.surveyIdList = tSurveyIdListAsList;
		this.userList = tUserListAsList;
	}
	
	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		
		LOGGER.info("Servicing a survey response read request.");
		
		if(! authenticate(false)) {
			return;
		}
		
		try {
			
			LOGGER.info("Populating the requester with their associated campaigns and roles.");
			UserCampaignServices.populateUserWithCampaignRoleInfo(this, this.getUser());
			
			LOGGER.info("Verifying that requester belongs to the campaign specified by campaign ID.");
		    UserCampaignServices.campaignExistsAndUserBelongs(this, this.getUser(), this.campaignUrn);
		    
		    LOGGER.info("Verifying that the requester has a role that allows reading of survey responses.");
		    UserCampaignServices.requesterCanViewUsersSurveyResponses(this, this.campaignUrn, this.getUser().getUsername(), null);
			
		    if(! this.userList.equals(URN_SPECIAL_ALL_LIST)) {
		    	LOGGER.info("Checking the user list to make sure all of the users belong to the campaign ID.");
		    	UserCampaignServices.verifyUsersExistInCampaign(this, this.campaignUrn, this.userList);
		    }
		    
		    LOGGER.info("Retrieving campaign configuration.");
			this.configuration = CampaignServices.findCampaignConfiguration(this, this.campaignUrn);
		    
			if(this.promptIdList != null && ! this.promptIdList.equals(URN_SPECIAL_ALL_LIST)) {
				LOGGER.info("Verifying that the prompt ids in the query belong to the campaign.");
				SurveyResponseReadServices.verifyPromptIdsBelongToConfiguration(this, this.promptIdList, this.configuration);
			}
			
			if(this.surveyIdList != null && ! this.surveyIdList.equals(URN_SPECIAL_ALL_LIST)) {
				LOGGER.info("Verifying that the survey ids in the query belong to the campaign.");
				SurveyResponseReadServices.verifySurveyIdsBelongToConfiguration(this, this.surveyIdList, this.configuration);
			}
		    
			LOGGER.info("Dispatching to the data layer.");
			List<SurveyResponseReadResult> surveyResponseList = SurveyResponseReadDao.retrieveSurveyResponses(this, this.userList,
					this.campaignUrn, this.promptIdList, this.surveyIdList, this.startDate, this.endDate, this.sortOrder, 
					this.configuration);
			
			LOGGER.info("Filtering survey response results according to our privacy rules and the requester's role.");
			SurveyResponseReadServices.performPrivacyFilter(this.getUser(), this.campaignUrn, surveyResponseList, this.privacyState);
		}
		
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
		catch(DataAccessException e) {
			e.logException(LOGGER);
		}
	}

	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		
		super.respond(httpRequest, httpResponse, null);

		
		
		
		
	}
}
