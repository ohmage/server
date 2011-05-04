package edu.ucla.cens.awserver.jee.servlet.glue;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;

/**
 * Builds an AwRequest for /app/survey_response/read.
 * 
 * @author selsky
 */
public class SurveyResponseReadAwRequestCreator implements AwRequestCreator {
	
	public SurveyResponseReadAwRequestCreator() {
		
	}
	
	public AwRequest createFrom(HttpServletRequest request) {

		String startDate = request.getParameter("start_date");
		String endDate = request.getParameter("end_date");
		String userList = request.getParameter("user_list");
		String client = request.getParameter("client");
		String campaignUrn = request.getParameter("campaign_urn");
		String authToken = request.getParameter("auth_token");
		String promptIdList = request.getParameter("prompt_id_list");
		String surveyIdList = request.getParameter("survey_id_list");
		String columnList = request.getParameter("column_list");
		String outputFormat = request.getParameter("output_format");
		String prettyPrint = request.getParameter("pretty_print");
		String suppressMetadata = request.getParameter("suppress_metadata");
		String privacyState = request.getParameter("privacy_state");
		String returnId = request.getParameter("return_id");
		String sortOrder = request.getParameter("sort_order");
		
		SurveyResponseReadAwRequest awRequest = new SurveyResponseReadAwRequest();
		
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		awRequest.setCampaignUrn(campaignUrn);
		awRequest.setUserListString(userList);
		awRequest.setPromptIdListString(promptIdList);
		awRequest.setSurveyIdListString(surveyIdList);
		awRequest.setColumnListString(columnList);
		awRequest.setOutputFormat(outputFormat);
		awRequest.setPrettyPrintAsString(prettyPrint);
		awRequest.setSuppressMetadataAsString(suppressMetadata);
		awRequest.setReturnIdAsString(returnId);
		awRequest.setSortOrderString(sortOrder);
		awRequest.setPrivacyState(privacyState);
		
		// temporarily using this frankenstein approach before migrating completely to toValidat()
		Map<String, Object> toValidate = new HashMap<String, Object>();
		toValidate.put("suppress_metadata", suppressMetadata);
		toValidate.put("pretty_print", prettyPrint);
		toValidate.put("return_id", returnId);
		awRequest.setToValidate(toValidate);
		
        NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread _ this means that 
                                  // it will be in every log message for the current thread
		return awRequest;
	}
}
