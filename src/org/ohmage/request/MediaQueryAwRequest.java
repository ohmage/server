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

/**
 * State for image queries.
 * 
 * @author selsky
 */
public class MediaQueryAwRequest extends ResultListAwRequest {
	private String _userNameRequestParam;
	private String _client;
	private String _campaignUrn;
	private String _mediaId;
	private String _mediaUrl;
	private String _size;
	
	public String getUserNameRequestParam() {
		return _userNameRequestParam;
	}

	public void setUserNameRequestParam(String userNameRequestParam) {
		_userNameRequestParam = userNameRequestParam;
	}

	public String getClient() {
		return _client;
	}

	public void setClient(String client) {
		_client = client;
	}

	public String getCampaignUrn() {
		return _campaignUrn;
	}

	public void setCampaignUrn(String campaignUrn) {
		_campaignUrn = campaignUrn;
	}

	public String getMediaId() {
		return _mediaId;
	}

	public void setMediaId(String mediaId) {
		_mediaId = mediaId;
	}

	public String getMediaUrl() {
		return _mediaUrl;
	}

	public void setMediaUrl(String mediaUrl) {
		_mediaUrl = mediaUrl;
	}
	
	public String getSize() {
		return _size;
	}

	public void setSize(String size) {
		_size = size;
	}
}
