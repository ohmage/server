package edu.ucla.cens.awserver.jee.servlet.glue;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * Builds an AwRequest for the "new" data point API feature.
 * 
 * @author selsky
 */
public class NewDataPointQueryAwRequestCreator implements AwRequestCreator {
	
	public NewDataPointQueryAwRequestCreator() {
		
	}
	
	public AwRequest createFrom(HttpServletRequest request) {

		String startDate = request.getParameter("start_date");
		String endDate = request.getParameter("end_date");
		String userList = request.getParameter("user_list");
		String client = request.getParameter("client");
		String campaignName = request.getParameter("campaign_name");
		String campaignVersion = request.getParameter("campaign_version");
		String authToken = request.getParameter("auth_token");
		String promptIdList = request.getParameter("prompt_id_list");
		String surveyIdList = request.getParameter("survey_id_list");
		String columnList = request.getParameter("column_list");
		String outputFormat = request.getParameter("output_format");
		String prettyPrint = request.getParameter("pretty_print");
		String suppressMetadata = request.getParameter("suppress_metadata");
		
		NewDataPointQueryAwRequest awRequest = new NewDataPointQueryAwRequest();
		
		awRequest.setStartDate(startDate);
		awRequest.setEndDate(endDate);
		awRequest.setUserToken(authToken);
		awRequest.setClient(client);
		awRequest.setCampaignName(campaignName);
		awRequest.setCampaignVersion(campaignVersion);
		awRequest.setUserListString(userList);
		awRequest.setPromptIdListString(promptIdList);
		awRequest.setSurveyIdListString(surveyIdList);
		awRequest.setColumnListString(columnList);
		awRequest.setOutputFormat(outputFormat);
		awRequest.setPrettyPrint(Boolean.valueOf(prettyPrint).booleanValue());
		awRequest.setSuppressMetadata(Boolean.valueOf(suppressMetadata).booleanValue());
		
        NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread _ this means that 
                                  // it will be in every log message for the current thread
		return awRequest;
	}
}
