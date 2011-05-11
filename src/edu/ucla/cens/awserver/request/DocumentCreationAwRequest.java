package edu.ucla.cens.awserver.request;

import org.andwellness.utils.StringUtils;

public class DocumentCreationAwRequest extends ResultListAwRequest {
	public DocumentCreationAwRequest(String urn, String name, String document, String privacyState, String description) {
		super();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(urn)) {
			throw new IllegalArgumentException("The document's URN cannot be null or blank.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(name)) {
			throw new IllegalArgumentException("The document's name cannot be null or blank.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(document)) {
			throw new IllegalArgumentException("The document cannot be null or blank.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			throw new IllegalArgumentException("The document's initial privacy state cannot be null or blank.");
		}
	}
}
