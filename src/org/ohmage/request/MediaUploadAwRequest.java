/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.request;

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
	private String _campaignCreationTimestamp;
	
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
	
	public String getCampaignCreationTimestamp() {
		return _campaignCreationTimestamp;
	}
	
	public void setCampaignCreationTimestamp(String campaignCreationTimestamp) {
		_campaignCreationTimestamp = campaignCreationTimestamp;
	}

	@Override
	public String toString() {
		return "MediaUploadAwRequest [_client=" + _client + ", _sessionId="
				+ _sessionId + ", _campaignUrn=" + _campaignUrn
				+ ", _campaignCreationTimestamp=" + _campaignCreationTimestamp
				+ ", _media=" + Arrays.toString(_media) + ", _mediaId="
				+ _mediaId + ", _mediaType=" + _mediaType + ", _startTime="
				+ _startTime + ", _duplicateIndexList=" + _duplicateIndexList
				+ "]";
	}
}

