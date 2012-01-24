package org.ohmage.request.visualization;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.service.UserCampaignServices;
import org.ohmage.service.VisualizationServices;
import org.ohmage.validator.VisualizationValidators;

/**
 * <p>A request for the number of private survey responses for a campaign. This 
 * specific request requires no additional parameters.<br />
 * <br />
 * See {@link org.ohmage.request.visualization.VisualizationRequest} for other 
 * required parameters.</p>
 * 
 * @author John Jenkins
 */
public class VizSurveyResponsePrivacyStateRequest extends VisualizationRequest {
	private static final Logger LOGGER = Logger.getLogger(VizSurveyResponsePrivacyStateRequest.class);
	
	private static final String REQUEST_PATH = "sharedplot/png";
	
	private final Integer aggregate;
	
	/**
	 * Creates a survey response privacy state visualization request.
	 * 
	 * @param httpRequest An HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public VizSurveyResponsePrivacyStateRequest(HttpServletRequest httpRequest) {
		super(httpRequest);
		
		LOGGER.info("Creating a survey response privacy state visualization request.");

		Integer tAggregate = null;
		
		try {
			String[] t;
			
			t = getParameterValues(InputKeys.VISUALIZATION_AGGREGATE);
			if(t.length > 1) {
				throw new ValidationException(
						ErrorCode.VISUALIZATION_INVALID_AGGREGATE_VALUE,
						"Multiple values given for the same parameter: " +
								InputKeys.VISUALIZATION_AGGREGATE);
			}
			else if(t.length == 1) {
				tAggregate = VisualizationValidators.validateAggregate(t[0]);
			}
		}
		catch(ValidationException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
		
		aggregate = tAggregate;
	}

	/**
	 * Services this request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the survey response privacy state visualization request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		super.service();
		
		if(isFailed()) {
			return;
		}
		
		try {
			LOGGER.info("Verifying the user is able to read survey responses about other users.");
			UserCampaignServices.instance().requesterCanViewUsersSurveyResponses(getCampaignId(), getUser().getUsername());
			
			Map<String, String> parameters = getVisualizationParameters();
			parameters.remove(VisualizationServices.PARAMETER_KEY_PRIVACY_STATE);
			
			if(aggregate != null) {
				parameters.put(
						VisualizationServices.PARAMETER_KEY_AGGREGATE, 
						aggregate.toString());
			}
			
			LOGGER.info("Making the request to the visualization server.");
			setImage(VisualizationServices.sendVisualizationRequest(REQUEST_PATH, getUser().getToken(), 
					getCampaignId(), getWidth(), getHeight(), parameters));
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}
}