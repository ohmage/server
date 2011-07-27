package org.ohmage.request;

import java.util.Collections;
import java.util.List;

import org.ohmage.util.StringUtils;

/**
 * Abstract superclass for all visuzliation requests.
 * 
 * @author John Jenkins
 */
public abstract class VisualizationRequest extends AbstractAwRequest {
	public static final String VISUALIZATION_REQUEST_RESULT = "visualization_request_result";
	
	private final String _userToken;
	private List<?> _resultList;
	
	private String _startDate;
	private String _endDate;
	
	/**
	 * Creates a visualization request.
	 * 
	 * @param token The user's token.
	 * 
	 * @param width The desired width of the resulting image.
	 * 
	 * @param height The desired height of the resulting image.
	 * 
	 * @param campaignId The ID of the campaign whose information is requested.
	 * 
	 * @param privacyState The privacy state of the responses to be queried. If
	 * 					   null, it will be ignored.
	 * 
	 * @param startDate The start date of all survey responses to be queried. 
	 * 					If this is null, it will be ignored.
	 * 
	 * @param endDate The end date of all survey responses to be queried. If
	 * 				  this is null, it will be ignored.
	 */
	public VisualizationRequest(String token, String width, String height, String campaignId, String privacyState, String startDate, String endDate) {
		super();
		
		_userToken = token;
		
		addToValidate(InputKeys.VISUALIZATION_WIDTH, width, true);
		addToValidate(InputKeys.VISUALIZATION_HEIGHT, height, true);
		
		setCampaignUrn(campaignId);
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			addToValidate(InputKeys.PRIVACY_STATE, privacyState, true);
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(startDate)) {
			addToValidate(InputKeys.START_DATE, startDate, true);
			// HACK ATTACK! This code is dying. This is done because the 
			// DateValidator doesn't know which key to use to store the start
			// date and the end date when validating them.
			addToProcess(InputKeys.START_DATE, startDate, true);
			_startDate = startDate;
		}
		else {
			_startDate = null;
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(endDate)) {
			addToValidate(InputKeys.END_DATE, endDate, true);
			// HACK ATTACK! This code is dying. This is done because the 
			// DateValidator doesn't know which key to use to store the start
			// date and the end date when validating them.
			addToProcess(InputKeys.END_DATE, endDate, true);
			_endDate = endDate;
		}
		else {
			_endDate = null;
		}
		
		_resultList = Collections.emptyList();
	}
	
	/**
	 * Returns the authentication / session token.
	 * 
	 * @return The authentication / session token.
	 */
	@Override
	public String getUserToken() {
		return _userToken;
	}
	
	/**
	 * Returns the result list that was set by a DAO.
	 * 
	 * @return The result list that was set by a DAO.
	 */
	@Override
	public List<?> getResultList() {
		return _resultList;
	}

	/**
	 * Sets the result list. This should only be used by DAOs to return values
	 * to services.
	 * 
	 * @param resultList The result list set by a DAO to return (a) value(s) to
	 * 					 the calling service.
	 */
	@Override
	public void setResultList(List<?> resultList) {
		_resultList = resultList;
	}
	
	/**
	 * Gets the stored start date to facilitate some antiquated method that is
	 * dying.
	 */
	public String getStartDate() {
		return _startDate;
	}
	
	/**
	 * Gets the stored end date to facilitate some antiquated method that is
	 * dying.
	 */
	public String getEndDate() {
		return _endDate;
	}
}