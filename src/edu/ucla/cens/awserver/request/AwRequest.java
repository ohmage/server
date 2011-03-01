package edu.ucla.cens.awserver.request;

import java.util.List;

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
	
//	 int getCampaignPromptGroupId();
//	 void setCampaignPromptGroupId(int campaignPromptGroupId);
	
//	 List<PromptType> getPromptTypeRestrictions();
//	 void setPromptTypeRestrictions(List<PromptType> promptTypeRestrictions);
	
//	 Map<Integer, List<Integer>> getDuplicatePromptResponseMap();
//	 void setDuplicatePromptResponseMap(Map<Integer, List<Integer>> duplicatePromptResponseMap);
	
//	 int getCampaignPromptVersionId();
//	 void setCampaignPromptVersionId(int campaignPromptVersionId);
	
	 long getStartTime();
	 void setStartTime(long startTime);
	
	 String getSessionId();
	 void setSessionId(String sessionId);
	
//	 String getRequestType();
//	 void setRequestType(String requestType);
	
	 String getClient();
	 void setClient(String client);
	
//	 String getProtocolVersion();
//	 void setProtocolVersion(String protocolVersion);
	
	 String getJsonDataAsString();
	 void setJsonDataAsString(String jsonDataAsString);
	
	 // A DataPacket is a single mobility record or a single survey response
	 List<DataPacket> getDataPackets();
	 void setDataPackets(List<DataPacket> dataPackets);
	
//	 List<SurveyDataPacket> getSurveys();
//	 void setSurveys(List<SurveyDataPacket> surveys);
	 
	 int getCurrentMessageIndex();
	 void setCurrentMessageIndex(int currentMessageIndex);
	
//	 int getCurrentPromptId();
//	 void setCurrentPromptId(int currentPromptId);
	
	 JSONArray getJsonDataAsJsonArray();
	 void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray);
	
//	 String getGroupId();
//	 void setGroupId(String groupId);
//	
//	 int[] getPromptIdArray();
//	 void setPromptIdArray(int[] promptIdArray);
//	
//	 String getVersionId();
//	 void setVersionId(String versionId);
	
	 User getUser();
	 void setUser(User user);
	
	 String getUserToken();
	 void setUserToken(String token);
	 
	 String getRequestUrl();
	 void setRequestUrl(String requestUrl);
	
	 String getUserNameRequestParam();
	 void setUserNameRequestParam(String requestUrl);
	 
	 String getCampaignVersion();
	 void setCampaignVersion(String campaignVersion);
	 
//	 List<DuplicateSurveyUpload> getDuplicateSurveyUploads();
//	 void setDuplicateSurveyUploads(List<DuplicateSurveyUpload> duplicates);
	 
	 String getMediaId();
	 void setMediaId(String id);
	 
	 byte[] getMedia();
	 void setMedia(byte[] media);
	 
	 String getMediaType();
	 void setMediaType(String id);
	 
	 String getCampaignName();
	 void setCampaignName(String campaignName);
}
