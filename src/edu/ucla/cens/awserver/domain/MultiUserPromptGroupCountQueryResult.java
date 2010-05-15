package edu.ucla.cens.awserver.domain;

/**
 * @author selsky
 */
public class MultiUserPromptGroupCountQueryResult extends SingleUserPromptGroupCountQueryResult {
	private String _user;

	public String getUser() {
		return _user;
	}

	public void setUser(String user) {
		_user = user;
	}
}
