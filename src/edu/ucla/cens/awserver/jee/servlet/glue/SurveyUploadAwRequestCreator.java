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
//  private static Logger _logger = Logger.getLogger(SensorUploadAwRequestCreator.class);
    
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
        
        String userName = parameterMap.get("u")[0];
        String campaignName = parameterMap.get("c")[0];
        String password = parameterMap.get("p")[0];
        String client = parameterMap.get("ci")[0];
        String campaignVersion = parameterMap.get("cv")[0];
        String jsonData = parameterMap.get("d")[0]; 
        
        UserImpl user = new UserImpl();
        user.setUserName(userName);
        user.setPassword(password);
                
        SurveyUploadAwRequest awRequest = new SurveyUploadAwRequest();

        awRequest.setStartTime(System.currentTimeMillis());
        awRequest.setSessionId(sessionId);
        awRequest.setUser(user);
        awRequest.setClient(client);
        awRequest.setJsonDataAsString(jsonData);
        awRequest.setCampaignVersion(campaignVersion);
        awRequest.setCampaignUrn(campaignName);

        String requestUrl = request.getRequestURL().toString();
        if(null != request.getQueryString()) {
            requestUrl += "?" + request.getQueryString(); 
        }
        
        awRequest.setRequestUrl(requestUrl); // output in reponse in case of error, logged to filesystem
        
        NDC.push("ci=" + client); // push the client string into the Log4J NDC for the currently executing thread - this means that 
                                  // it will be in every log message for the thread
        
        return awRequest;
    }
}

