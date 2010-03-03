package edu.ucla.cens.awserver.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Data packet implementation for the storage of a prompt response group.
 * 
 * @author selsky
 */
public class PromptDataPacket extends MetadataDataPacket {
	private int _groupId; // the group_id sent by the phone, not the id on the campaign_prompt_group table
	private List<PromptResponseDataPacket> _responses;
	
	public List<PromptResponseDataPacket> getResponses() {
		return _responses;
	}

	public void setResponses(List<PromptResponseDataPacket> responses) {
		_responses = responses;
	}
	
	public int getGroupId() {
		return _groupId;
	}
	
	public void setGroupId(int groupId) {
		_groupId = groupId;
	}
	
	@Override
	public String toString() {
		return "PromptDataPacket [_groupId=" + _groupId + ",_responses=" + (null != _responses ? Arrays.toString(_responses.toArray()) : "null") + ", toString()="
				+ super.toString() + "]";
	}

	public class PromptResponseDataPacket implements DataPacket {
		private int _promptConfigId; // the prompt_id sent by the phone (not the primary key to the db table)
		private String _response;    // a JSON Object converted to a string
		
		public PromptResponseDataPacket() { }
		
		public int getPromptConfigId() {
			return _promptConfigId;
		}
		
		public void setPromptConfigId(int promptConfigId) {
			_promptConfigId = promptConfigId;
		}
		
		public String getResponse() {
			return _response;
		}
		
		public void setResponse(String response) {
			_response = response;
		}
		
		@Override
		public String toString() {
			return "PromptResponseDataPacket [_promptConfigId=" + _promptConfigId
					+ ", _response=" + _response + "]";
		}
	}
	
}
