package edu.ucla.cens.awserver.request;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptType;
import edu.ucla.cens.awserver.domain.User;

/**
 * Base interface for an AwRequest: a data transfer object for inbound, outbound and processing parameters specific
 * to an application feature. This interface contains all possible accessors and mutators for all AW features. The reason
 * for this design is that Java is not dynamically typed and the design of the main processing components (Services, Validators,
 * Controllers, DAOs) is greatly simplified by instances of those classes only needing an AwRequest without having to cast 
 * everytime and without simply passing a Map around as an alternative. Casts are ugly and error-prone and a Map does not offer 
 * enough control over what it can and cannot contain. It is up to instances of this interface to throw an
 * UnsupportedOperationException for methods that aren't appropriate for their feature context. 
 * 
 * @author selsky
 * @see AbstractAwRequest
 */
public interface AwRequest {

	public boolean isFailedRequest();
	public void setFailedRequest(boolean isFailedRequest);
	
	public String getFailedRequestErrorMessage();
	public void setFailedRequestErrorMessage(String errorMessage);
	
	public String getStartDate();
	public void setStartDate(String startDate);
	
	public String getEndDate();
	public void setEndDate(String endDate);
	
	public List<?> getResultList();
	public void setResultList(List<?> resultList);

	public List<Integer> getDuplicateIndexList();
	public void setDuplicateIndexList(List<Integer> duplicateIndexList);
	
	public int getCampaignPromptGroupId();
	public void setCampaignPromptGroupId(int campaignPromptGroupId);
	
	public List<PromptType> getPromptTypeRestrictions();
	public void setPromptTypeRestrictions(List<PromptType> promptTypeRestrictions);
	
	public Map<Integer, List<Integer>> getDuplicatePromptResponseMap();
	public void setDuplicatePromptResponseMap(Map<Integer, List<Integer>> duplicatePromptResponseMap);
	
	public int getCampaignPromptVersionId();
	public void setCampaignPromptVersionId(int campaignPromptVersionId);
	
	public long getStartTime();
	public void setStartTime(long startTime);
	
	public String getSessionId();
	public void setSessionId(String sessionId);
	
	public String getRequestType();
	public void setRequestType(String requestType);
	
	public String getPhoneVersion();
	public void setPhoneVersion(String phoneVersion);
	
	public String getProtocolVersion();
	public void setProtocolVersion(String protocolVersion);
	
	public String getJsonDataAsString();
	public void setJsonDataAsString(String jsonDataAsString);
	
	public List<DataPacket> getDataPackets();
	public void setDataPackets(List<DataPacket> dataPackets);
	
	public int getCurrentMessageIndex();
	public void setCurrentMessageIndex(int currentMessageIndex);
	
	public int getCurrentPromptId();
	public void setCurrentPromptId(int currentPromptId);
	
	public JSONArray getJsonDataAsJsonArray();
	public void setJsonDataAsJsonArray(JSONArray jsonDataAsJsonArray);
	
	public String getGroupId();
	public void setGroupId(String groupId);
	
	public int[] getPromptIdArray();
	public void setPromptIdArray(int[] promptIdArray);
	
	public String getVersionId();
	public void setVersionId(String versionId);
	
	public User getUser();
	public void setUser(User user);
	
	public String getSubdomain();
	public void setSubdomain(String subdomain);
	
	public String getRequestUrl();
	public void setRequestUrl(String requestUrl);

}
