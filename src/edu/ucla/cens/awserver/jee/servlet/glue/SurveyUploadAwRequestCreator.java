package edu.ucla.cens.awserver.jee.servlet.glue;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.NDC;

import edu.ucla.cens.awserver.domain.UserImpl;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyUploadAwRequest;

/**
 * Transformer for creating an AwRequest for the upload feature.
 * 
 * @author selsky
 */
public class SurveyUploadAwRequestCreator implements AwRequestCreator {
  //private static Logger _logger = Logger.getLogger(SurveyUploadAwRequestCreator.class);
    
    /**
     * Default no-arg constructor. Simply creates an instance of this class.
     */
    public SurveyUploadAwRequestCreator() {
        
    }
    
    /**
     * Creates an AwRequest from the validatedParamterMap found in the HttpServletRequest.
     */
    public AwRequest createFrom(HttpServletRequest request) {
    	@SuppressWarnings("unchecked")
		Map<String, String[]> parameterMap = (Map<String, String[]>) request.getAttribute("validatedParameterMap");
		
        String sessionId = request.getSession(false).getId(); // for upload logging to connect app logs to uploads
        
        String userName = parameterMap.get("user")[0];
        String campaignUrn = parameterMap.get("campaign_urn")[0];
        String password = parameterMap.get("password")[0];
        String client = parameterMap.get("client")[0];
        String jsonData = parameterMap.get("data")[0];
        String campaignCreationTimestamp = parameterMap.get("campaign_creation_timestamp")[0]; 
        
        UserImpl user = new UserImpl();
        user.setUserName(userName);
        user.setPassword(password);
                
        SurveyUploadAwRequest awRequest = new SurveyUploadAwRequest();

        awRequest.setStartTime(System.currentTimeMillis());
        awRequest.setSessionId(sessionId);
        awRequest.setUser(user);
        awRequest.setClient(client);
        awRequest.setJsonDataAsString(jsonData);
        awRequest.setCampaignUrn(campaignUrn);
        awRequest.setCampaignCreationTimestamp(campaignCreationTimestamp);

        String requestUrl = request.getRequestURL().toString();
        if(null != request.getQueryString()) {
            requestUrl += "?" + request.getQueryString(); 
        }
        
        awRequest.setRequestUrl(requestUrl); // output in reponse in case of error, logged to filesystem
        
        NDC.push("client=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                      // it will be in every log message for the thread
        
        return awRequest;
    }
}

