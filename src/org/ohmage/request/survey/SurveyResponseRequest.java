package org.ohmage.request.survey;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.joda.time.DateTime;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.domain.campaign.SurveyResponse.SortParameter;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignServices;
import org.ohmage.service.SurveyResponseReadServices;
import org.ohmage.service.SurveyResponseServices;
import org.ohmage.validator.CampaignValidators;
import org.ohmage.validator.SurveyResponseValidators;

/**
 * This class is responsible for any request against survey responses.
 *
 * @author John Jenkins
 */
public abstract class SurveyResponseRequest extends UserRequest {
	private static final Logger LOGGER = 
			Logger.getLogger(SurveyResponseReadRequest.class);
	
	public static final String URN_SPECIAL_ALL = "urn:ohmage:special:all";
	public static final Collection<String> URN_SPECIAL_ALL_LIST;
	static {
		URN_SPECIAL_ALL_LIST = new HashSet<String>();
		URN_SPECIAL_ALL_LIST.add(URN_SPECIAL_ALL);
	}
	
	private static final int MAX_NUMBER_OF_USERS = 10;
	private static final int MAX_NUMBER_OF_SURVEYS = 10;
	private static final int MAX_NUMBER_OF_PROMPTS = 10;
	
	private final String campaignId;
	private final Collection<String> usernames;
	private final Collection<String> surveyIds;
	private final Collection<String> promptIds;
	private final Set<UUID> surveyResponseIds;
	private final DateTime startDate;
	private final DateTime endDate;
	private final SurveyResponse.PrivacyState privacyState;
	private final Set<String> promptResponseSearchTokens;
	
	private Campaign campaign;
	
	private List<SurveyResponse> surveyResponseList =
		new ArrayList<SurveyResponse>();
	private long surveyResponseCount = 0;
	
	/**
	 * Creates a survey responses request. The optional parameters limit the 
	 * results to only those that match the criteria.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters from the HTTP request.
	 * 
	 * @param campaignId The campaign's unique identifier. Required.
	 * 
	 * @param usernames A set of usernames.
	 * 
	 * @param surveyIds A set of survey IDs.
	 * 
	 * @param promptIds A set of prompt IDs.
	 * 
	 * @param surveyResponseIds A set of survey response IDs.
	 * 
	 * @param startDate Limits the responses to only those on or after this
	 * 					date.
	 * 
	 * @param endDate Limits the responses to only those on or before this 
	 * 				  date.
	 * 
	 * @param privacyState A survey response privacy state.
	 * 
	 * @param promptResponseSearchTokens A set of tokens which must match the
	 * 									 prompt responses.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 * 
	 * @throws IllegalArgumentException Thrown if a required parameter is 
	 * 									missing.
	 */
	protected SurveyResponseRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			boolean callClientRequester,
			final String campaignId,
			final Collection<String> usernames,
			final Collection<String> surveyIds,
			final Collection<String> promptIds,
			final Set<UUID> surveyResponseIds,
			final DateTime startDate,
			final DateTime endDate,
			final SurveyResponse.PrivacyState privacyState,
			final Set<String> promptResponseSearchTokens)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, false, TokenLocation.EITHER, parameters, callClientRequester);
		
		if(campaignId == null) {
			throw new IllegalArgumentException("The campaign ID is null.");
		}
		
		this.campaignId = campaignId;
		this.usernames = usernames;
		this.surveyIds = surveyIds;
		this.promptIds = promptIds;
		this.surveyResponseIds = surveyResponseIds;
		this.startDate = startDate;
		this.endDate = endDate;
		this.privacyState = privacyState;
		this.promptResponseSearchTokens = promptResponseSearchTokens;
	}

	/**
	 * Gathers all of the possible parameters for a survey response request.
	 * The only required parameter is the campaign ID. The others are not, a
	 * list of usernames, a list of survey IDs, a list of prompt IDs, a list of
	 * survey response IDs, a start date, an end date, a sort order, and a
	 * privacy state.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public SurveyResponseRequest(final HttpServletRequest httpRequest) throws IOException, InvalidRequestException {
		// Handle user-password or token-based authentication
		super(httpRequest, false, TokenLocation.EITHER, null);

		String tCampaignId = null;
		Set<String> tUsernames = null;

		Set<String> tSurveyIds = null;
		Set<String> tPromptIds = null;
		
		Set<UUID> tSurveyResponseIds = null;
		
		DateTime tStartDate = null;
		DateTime tEndDate = null;
		
		SurveyResponse.PrivacyState tPrivacyState = null;
		
		Set<String> tPromptResponseSearchTokens = null;
		
		if(! isFailed()) {
			String[] t;
			
			try {
				// Campaign ID
				t = getParameterValues(InputKeys.CAMPAIGN_URN);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID, 
							"The required campaign ID was not present: " + 
								InputKeys.CAMPAIGN_URN);
				}
				else if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CAMPAIGN_INVALID_ID, 
							"Multiple campaign IDs were found: " + 
								InputKeys.CAMPAIGN_URN);
				}
				else {
					tCampaignId = CampaignValidators.validateCampaignId(t[0]);
					
					if(tCampaignId == null) {
						throw new ValidationException(
								ErrorCode.CAMPAIGN_INVALID_ID, 
								"The required campaign ID was not present: " + 
									InputKeys.CAMPAIGN_URN);
					}
				}
				
				// User List
				t = getParameterValues(InputKeys.USER_LIST);
				if(t.length == 0) {
					throw new ValidationException(
							ErrorCode.SURVEY_MALFORMED_USER_LIST, 
							"The user list is missing: " + 
								InputKeys.USER_LIST);
				}
				else if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_MALFORMED_USER_LIST, 
							"Mutliple user lists were given: " + 
								InputKeys.USER_LIST);
				}
				else {
					tUsernames = 
							SurveyResponseValidators.validateUsernames(t[0]);
					
					if(tUsernames == null) {
						throw new ValidationException(
								ErrorCode.SURVEY_MALFORMED_USER_LIST, 
								"The user list is missing: " + 
									InputKeys.USER_LIST);
					}
					else if(tUsernames.size() > MAX_NUMBER_OF_USERS) {
						throw new ValidationException(
								ErrorCode.SURVEY_TOO_MANY_USERS, 
								"The user list contains more than " + 
									MAX_NUMBER_OF_USERS + 
									" users: " + 
									tUsernames.size());
					}
				}
				
				// Survey ID List
				t = getParameterValues(InputKeys.SURVEY_ID_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_MALFORMED_SURVEY_ID_LIST, 
							"Multiple survey ID lists were given: " + 
								InputKeys.SURVEY_ID_LIST);
				}
				else if(t.length == 1) {
					tSurveyIds = 
							SurveyResponseValidators.validateSurveyIds(t[0]);
					
					if((tSurveyIds != null) && (tSurveyIds.size() > MAX_NUMBER_OF_SURVEYS)) {
						throw new ValidationException(
								ErrorCode.SURVEY_TOO_MANY_SURVEY_IDS, 
								"More than " + 
									MAX_NUMBER_OF_SURVEYS + 
									" survey IDs were given: " + 
									tSurveyIds.size());
					}
				}
				
				// Prompt ID List
				t = getParameterValues(InputKeys.PROMPT_ID_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_MALFORMED_PROMPT_ID_LIST, 
							"Multiple prompt ID lists were given: " + 
								InputKeys.PROMPT_ID_LIST);
				}
				else if(t.length == 1) {
					tPromptIds = 
							SurveyResponseValidators.validatePromptIds(t[0]);
					
					if((tPromptIds != null) && (tPromptIds.size() > MAX_NUMBER_OF_PROMPTS)) {
						throw new ValidationException(
								ErrorCode.SURVEY_TOO_MANY_PROMPT_IDS, 
								"More than " + 
									MAX_NUMBER_OF_PROMPTS + 
									" prompt IDs were given: " + 
									tPromptIds.size());
					}
				}
				
				// Survey ID List and Prompt ID List Presence Check
				if(((tSurveyIds == null) || (tSurveyIds.size() == 0)) && 
				   ((tPromptIds == null) || (tPromptIds.size() == 0))) {
					throw new ValidationException(
							ErrorCode.SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY, 
							"A survey list (" + 
								InputKeys.SURVEY_ID_LIST + 
								") or prompt list (" + 
								InputKeys.PROMPT_ID_LIST + 
								") must be given.");
				}
				else if(((tSurveyIds != null) && (tSurveyIds.size() > 0)) && 
						((tPromptIds != null) && (tPromptIds.size() > 0))) {
					throw new ValidationException(
							ErrorCode.SURVEY_SURVEY_LIST_OR_PROMPT_LIST_ONLY, 
							"Both a survey list (" + 
								InputKeys.SURVEY_ID_LIST + 
								") and a prompt list (" + 
								InputKeys.PROMPT_ID_LIST + 
								") must be given.");
				}
				
				// The survey response's unique identifier.
				t = getParameterValues(InputKeys.SURVEY_RESPONSE_ID_LIST);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_SURVEY_ID,
							"Multiple survey response ID lists were given: " +
								InputKeys.SURVEY_RESPONSE_ID_LIST);
				}
				else if(t.length == 1) {
					tSurveyResponseIds = 
							SurveyResponseValidators.validateSurveyResponseIds(
									t[0]);
				}
				
				// The prompt response search string.
				t = getParameterValues(InputKeys.PROMPT_RESPONSE_SEARCH);
				if(t.length > 1) {
					throw new ValidationException(
						ErrorCode.SURVEY_INVALID_PROMPT_RESPONSE_SEARCH,
						"Multiple prompt response search strings were given: " +
							InputKeys.PROMPT_RESPONSE_SEARCH);
				}
				else if(t.length == 1) {
					tPromptResponseSearchTokens =
						SurveyResponseValidators
							.validatePromptResponseSearch(t[0]);
				}
				
				// Start Date
				t = getParameterValues(InputKeys.START_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple start dates were given: " + 
								InputKeys.START_DATE);
				}
				else if(t.length == 1) {
					tStartDate = 
							SurveyResponseValidators.validateStartDate(t[0]);
				}
				
				// End Date
				t = getParameterValues(InputKeys.END_DATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SERVER_INVALID_DATE, 
							"Multiple end dates were given: " + 
								InputKeys.END_DATE);
				}
				else if(t.length == 1) {
					tEndDate = SurveyResponseValidators.validateEndDate(t[0]);
				}
				
				// Privacy State
				t = getParameterValues(InputKeys.PRIVACY_STATE);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.SURVEY_INVALID_PRIVACY_STATE, 
							"Multiple privacy state values were given: " + 
								InputKeys.PRIVACY_STATE);
				}
				else if(t.length == 1) {
					tPrivacyState = 
							SurveyResponseValidators.validatePrivacyState(
									t[0]);
				}
			}
			catch (ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		campaignId = tCampaignId;
		usernames = tUsernames;
		
		surveyIds = tSurveyIds;
		promptIds = tPromptIds;
		
		surveyResponseIds = tSurveyResponseIds;

		startDate = tStartDate;
		endDate = tEndDate;

		privacyState = tPrivacyState;
		
		promptResponseSearchTokens = tPromptResponseSearchTokens;
	}

	/**
	 * Authenticates the parameters and makes the request against the database.
	 * 
	 * @param columns The columns to gather for each survey response.
	 * 
	 * @param promptType Only gather survey responses that contain prompt 
	 * 					 responses whose prompt type is this. Note, the survey
	 * 					 response may contain other prompt responses, but those
	 * 					 will _not_ be gathered.
	 * 
	 * @param collapse Whether or not to collapse the results.
	 * 
	 * @param numSurveyResponsesToSkip The number of survey responses to skip.
	 * 
	 * @param numSurveyResponsesToProcess The number of survey responses to	
	 * 									  process.
	 */
	public void service(
			final Collection<SurveyResponse.ColumnKey> columns,
			final String promptType,
			final List<SortParameter> sortOrder,
			final Boolean collapse,
			final long numSurveyResponsesToSkip,
			final long numSurveyResponsesToProcess) {
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
		    LOGGER.info("Retrieving campaign configuration.");
			campaign = CampaignServices.instance().getCampaign(campaignId);
			if(campaign == null) {
				throw
					new ServiceException(
						ErrorCode.CAMPAIGN_INVALID_ID,
						"The campaign does not exist.");
			}
			
			if((promptIds != null) && (! promptIds.isEmpty()) && (! URN_SPECIAL_ALL_LIST.equals(promptIds))) {
				LOGGER.info("Verifying that the prompt ids in the query belong to the campaign.");
				SurveyResponseReadServices.instance().verifyPromptIdsBelongToConfiguration(promptIds, campaign);
			}
			
			if((surveyIds != null) && (! surveyIds.isEmpty()) && (! URN_SPECIAL_ALL_LIST.equals(surveyIds))) {
				LOGGER.info("Verifying that the survey ids in the query belong to the campaign.");
				SurveyResponseReadServices.instance().verifySurveyIdsBelongToConfiguration(surveyIds, campaign);
			}
		    
			LOGGER.info("Dispatching to the data layer.");
			surveyResponseCount = 
					SurveyResponseServices.instance().readSurveyResponseInformation(
							campaign,
							getUser().getUsername(),
							surveyResponseIds,
							(URN_SPECIAL_ALL_LIST.equals(usernames) ? null : usernames), 
							startDate, 
							endDate, 
							privacyState, 
							(URN_SPECIAL_ALL_LIST.equals(surveyIds)) ? null : surveyIds, 
							(URN_SPECIAL_ALL_LIST.equals(promptIds)) ? null : promptIds,
							null,
							promptResponseSearchTokens,
							((collapse != null) && collapse && (! columns.equals(URN_SPECIAL_ALL_LIST))) ? columns : null,
							sortOrder,
							numSurveyResponsesToSkip,
							numSurveyResponsesToProcess,
							surveyResponseList
						);
			
			int numPromptResponses = 0;
			for(SurveyResponse surveyResponse : surveyResponseList) {
				numPromptResponses += surveyResponse.getResponses().size();
			}
			
			LOGGER.info(
					"Found " + 
						surveyResponseList.size() + 
						" results after filtering and paging a total of " + 
						surveyResponseCount + 
						" applicable responses, which contains " +
						numPromptResponses +
						" prompt responses.");
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}
	
	/**
	 * The campaign's unique identifier as supplied by the requester.
	 * 
	 * @return The campaign's unique identifier.
	 */
	public String getCampaignId() {
		return campaignId;
	}
	
	/**
	 * The campaign that was gathered while servicing this request or null if
	 * the request failed before gathering the campaign or if
	 * {@link #service(Collection, String, Boolean, long, long)} has not yet
	 * been called.
	 * 
	 * @return The Campaign or null.
	 */
	public Campaign getCampaign() {
		return campaign;
	}
	
	/**
	 * The survey IDs as requested by the user or null if no specific survey 
	 * IDs were requested.
	 * 
	 * @return The Collection of survey IDs.
	 */
	public Collection<String> getSurveyIds() {
		return surveyIds;
	}
	
	/**
	 * The prompt IDs as requested by the user or null if no specific prompt 
	 * IDs were requested.
	 * 
	 * @return The Collection of prompt IDs.
	 */
	public Collection<String> getPromptIds() {
		return promptIds;
	}

	/**
	 * The survey responses that matched the query.
	 * 
	 * @return An unmodifiable collection of the survey responses from the 
	 * 		   query.
	 */
	public Collection<SurveyResponse> getSurveyResponses() {
		return Collections.unmodifiableCollection(surveyResponseList);
	}
	
	/**
	 * The number of survey responses that matched the query without paging.
	 * 
	 * @return The number of survey responses that matched the query regardless
	 * 		   of paging.
	 */
	public long getSurveyResponseCount() {
		return surveyResponseCount;
	}
}