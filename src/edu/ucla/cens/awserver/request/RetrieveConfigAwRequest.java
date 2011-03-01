package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.domain.ConfigQueryResult;

/**
 * @author selsky
 */
public class RetrieveConfigAwRequest extends UploadAwRequest {
	
	private ConfigQueryResult _configQueryResult;

	public ConfigQueryResult get_configQueryResult() {
		return _configQueryResult;
	}

	public void set_configQueryResult(ConfigQueryResult configQueryResult) {
		_configQueryResult = configQueryResult;
	}
	
	
//	private String _outputUserRole;
//	private String _outputConfigXml;
//	private List<String> _outputUserList;
//	private List<String> _outputSpecialIdList;
//	
////	public String getOutputUserRole() {
////		return _outputUserRole;
////	}
////	
////	public void setOutputUserRole(String outputUserRole) {
////		_outputUserRole = outputUserRole;
////	}
//	
////	public String getOutputConfigXml() {
////		return _outputConfigXml;
////	}
////	
////	public void setOutputConfigXml(String outputConfigXml) {
////		_outputConfigXml = outputConfigXml;
////	}
//	
//	public List<String> getOutputUserList() {
//		return _outputUserList;
//	}
//	
//	public void setOutputUserList(List<String> outputUserList) {
//		_outputUserList = outputUserList;
//	}
//	
////	public List<String> getOutputSpecialIdList() {
////		return _outputSpecialIdList;
////	}
////	
////	public void setOutputSpecialIdList(List<String> specialIdList) {
////		_outputSpecialIdList = specialIdList;
////	}
//
//	@Override
//	public String toString() {
//		return "RetrieveConfigAwRequest [_outputSpecialIdList="
//				+ _outputSpecialIdList + ", _outputUserList=" + _outputUserList
//				+ ", _outputUserRole=" + _outputUserRole + "]";
//	}
}
