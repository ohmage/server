package edu.ucla.cens.awserver.request;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.User;

/**
 * Base interface for an AwRequest: a state object for inbound, outbound and processing parameters specific to an application 
 * feature. This interface contains all possible accessors and mutators for all AW features. The reason for this design
 * is that Java is not dynamically typed and the design of the main processing components (Services, Validators, Controllers, DAOs)
 * is greatly simplified by instances of those classes only needing an AwRequest without having to cast every time and without 
 * simply passing a Map around as an alternative. Casts are ugly and error-prone and a Map does not offer enough control (typing,  
 * descriptiveness) over what it can and cannot contain. It is recommended that instances of this interface throw an 
 * UnsupportedOperationException for methods that aren't appropriate for their feature context. 
 * 
 * @author selsky
 * @see AbstractAwRequest
 */
 public interface AwRequest {

	 boolean isFailedRequest();
	 void setFailedRequest(boolean isFailedRequest);
	
	 String getFailedRequestErrorMessage();
	 void setFailedRequestErrorMessage(String errorMessage);
	
	 String getStartDate();
	 void setStartDate(String startDate);
	
	 String getEndDate();
	 void setEndDate(String endDate);
	
	 List<?> getResultList();
	 void setResultList(List<?> resultList);

	 List<Integer> getDuplicateIndexList();
	 void setDuplicateIndexList(List<Integer> duplicateIndexList);
	
	 long getStartTime();
	 void setStartTime(long startTime);
	
	 String getSessionId();
	 void setSessionId(String sessionId);
	
	 String getClient();
	 void setClient(String client);
	
	 String getJsonDataAsString();
	 void setJsonDataAsString(String jsonDataAsString);
	
	 // A DataPacket is a single mobility record or a single survey response
	 List<DataPacket> getDataPackets();
	 void setDataPackets(List<DataPacket> dataPackets);
	 
	 int getCurrentMessageIndex();
	 void setCurrentMessageIndex(int currentMessageIndex);
	
	 JSONArray getJsonDataAsJsonArray();
	 void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray);
		
	 User getUser();
	 void setUser(User user);
	
	 String getUserToken();
	 void setUserToken(String token);
	 
	 String getRequestUrl();
	 void setRequestUrl(String requestUrl);
	
	 String getUserNameRequestParam();
	 void setUserNameRequestParam(String requestUrl);
	 
	 String getMediaId();
	 void setMediaId(String id);
	 
	 byte[] getMedia();
	 void setMedia(byte[] media);
	 
	 String getMediaType();
	 void setMediaType(String id);
	 
	 String getCampaignUrn();
	 void setCampaignUrn(String campaignUrn);
	 
	 Map<String, Object> getToValidate();
	 boolean existsInToValidate(String key);
	 Object getToValidateValue(String key);
	 void addToValidate(String key, Object value, boolean overwrite); 
	 
	 Map<String, Object> getToProcess();
	 boolean existsInToProcess(String key);
	 Object getToProcessValue(String key);
	 void addToProcess(String key, Object value, boolean overwrite);
	 
	 Map<String, Object> getToReturn();
	 boolean existsInToReturn(String key);
	 Object getToReturnValue(String key);
	 void addToReturn(String key, Object value, boolean overwrite);
}
