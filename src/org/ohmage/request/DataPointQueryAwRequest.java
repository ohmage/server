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

import org.ohmage.domain.Configuration;



/**
 * State for data point API queries.
 * 
 * @author selsky
 */
public class DataPointQueryAwRequest extends ResultListAwRequest {
	private String _startDate;
	private String _endDate;
	private String _userNameRequestParam;
	private String _client;
	private String _campaignUrn;
	private Configuration _configuration;
	
	private String[] _dataPointIds;
	// private String _authToken; see userToken in parent class
	private List<String> _metadataPromptIds;
	
	public Configuration getConfiguration() {
		return _configuration;
	}

	public void setConfiguration(Configuration configuration) {
		_configuration = configuration;
	}
	
	public String getStartDate() {
		return _startDate;
	}
	
	public void setStartDate(String startDate) {
		_startDate = startDate;
	}
	
	public String getEndDate() {
		return _endDate;
	}
	
	public void setEndDate(String endDate) {
		_endDate = endDate;
	}
	
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
	
	public String[] getDataPointIds() {
		return _dataPointIds;
	}

	public void setDataPointIds(String[] dataPointIds) {
		_dataPointIds = dataPointIds;
	}
	
	public List<String> getMetadataPromptIds() {
		return _metadataPromptIds;
	}

	public void setMetadataPromptIds(List<String> metadataPromptIds) {
		_metadataPromptIds = metadataPromptIds; 
	}

	@Override
	public String toString() {
		return "DataPointQueryAwRequest [_startDate=" + _startDate
				+ ", _endDate=" + _endDate + ", _userNameRequestParam="
				+ _userNameRequestParam + ", _client=" + _client
				+ ", _campaignUrn=" + _campaignUrn + ", _configuration="
				+ _configuration + ", _dataPointIds="
				+ Arrays.toString(_dataPointIds) + ", _metadataPromptIds="
				+ _metadataPromptIds + "]";
	}
}
