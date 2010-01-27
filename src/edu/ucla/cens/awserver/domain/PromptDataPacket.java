package edu.ucla.cens.awserver.domain;

import java.util.Arrays;
import java.util.List;

/**
 * Data packet implementation for the storage of a single prompt response.
 * 
 * @author selsky
 */
public class PromptDataPacket extends MetadataDataPacket {
	
	private List<PromptResponseDataPacket> _responses;
	
	public List<PromptResponseDataPacket> getResponses() {
		return _responses;
	}

	public void setResponses(List<PromptResponseDataPacket> responses) {
		_responses = responses;
	}
	
	@Override
	public String toString() {
		return "PromptDataPacket [_responses=" + Arrays.toString(_responses.toArray()) + ", toString()="
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
			return "PromptDataPacket [_promptConfigId=" + _promptConfigId
					+ ", _response=" + _response + "]";
		}
	}
	
}
