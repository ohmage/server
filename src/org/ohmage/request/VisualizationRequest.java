package org.ohmage.request;

import java.util.Collections;
import java.util.List;

/**
 * Abstract superclass for all visuzliation requests.
 * 
 * @author John Jenkins
 */
public abstract class VisualizationRequest extends AbstractAwRequest {
	public static final String VISUALIZATION_REQUEST_RESULT = "visualization_request_result";
	
	private final String _userToken;
	private List<?> _resultList;
	
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
	 */
	public VisualizationRequest(String token, String width, String height, String campaignId) {
		super();
		
		_userToken = token;
		
		addToValidate(InputKeys.VISUALIZATION_WIDTH, width, true);
		addToValidate(InputKeys.VISUALIZATION_HEIGHT, height, true);
		
		setCampaignUrn(campaignId);
		
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
}
