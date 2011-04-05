package edu.ucla.cens.awserver.request;

import java.util.Arrays;
import java.util.List;


/**
 * Represents state for uploads of media data.
 * 
 * @author selsky
 */
public class MediaUploadAwRequest extends ResultListAwRequest {
	private String _client;
	private String _sessionId;
	private String _campaignUrn;
	
	private byte[] _media;
	private String _mediaId;
	private String _mediaType;
	
	private long _startTime;
	private List<Integer> _duplicateIndexList;
	
	/**
	 * Default no-arg constructor.	
	 */
	public MediaUploadAwRequest() {
		
	}
	
	public String getCampaignUrn() {
		return _campaignUrn;
	}
	
	public long getStartTime() {
		return _startTime;
	}
	
	public void setStartTime(long startTime) {
		_startTime = startTime;
	}
	
	public String getSessionId() {
		return _sessionId;
	}
	
	public void setSessionId(String sessionId) {
		_sessionId = sessionId;
	}
	
	public String getClient() {
		return _client;
	}
	
	public void setClient(String client) {
		_client = client;
	}

	public byte[] getMedia() {
		return _media;
	}

	public void setMedia(byte[] media) {
		_media = media;
	}

	public String getMediaId() {
		return _mediaId;
	}

	public void setMediaId(String mediaId) {
		_mediaId = mediaId;
	}
	
	public String getMediaType() {
		return _mediaType;
	}

	public void setMediaType(String mediaType) {
		_mediaType = mediaType;
	}
	
	public List<Integer> getDuplicateIndexList() {
		return _duplicateIndexList;
	}
	
	public void setDuplicateIndexList(List<Integer> duplicateIndexList) {
		_duplicateIndexList = duplicateIndexList;
	}
	
	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}

	@Override
	public String toString() {
		return "MediaUploadAwRequest [_campaignUrn=" + _campaignUrn
				+ ", _client=" + _client + ", _duplicateIndexList="
				+ _duplicateIndexList + ", _media=" + Arrays.toString(_media)
				+ ", _mediaId=" + _mediaId + ", _mediaType=" + _mediaType
				+ ", _sessionId=" + _sessionId + ", _startTime=" + _startTime
				+ "]";
	}
}

