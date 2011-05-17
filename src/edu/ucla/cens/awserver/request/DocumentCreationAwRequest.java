package edu.ucla.cens.awserver.request;

import org.andwellness.utils.StringUtils;

/**
 * Creates a request for document creation.
 * 
 * @author John Jenkins
 */
public class DocumentCreationAwRequest extends ResultListAwRequest {
	/**
	 * Builds the document creation request. The following parameters are
	 * required:
	 * 
	 *   urn
	 *   name
	 *   document
	 *   privacyState
	 * 
	 * @param urn The proposed URN for the document.
	 * 
	 * @param name The name of the document.
	 * 
	 * @param document The document itself.
	 * 
	 * @param privacyState The initial privacy state for this document.
	 * 
	 * @param description An optional description for this document.
	 * 
	 * @throws IllegalArgumentException Thrown if any of the required
	 * 									parameters are null or whitespace
	 * 									only.
	 */
	public DocumentCreationAwRequest(String urn, String name, String document, String privacyState, String description) throws IllegalArgumentException {
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
		
		addToValidate(InputKeys.DOCUMENT_URN, urn, true);
		addToValidate(InputKeys.DOCUMENT_NAME, name, true);
		addToValidate(InputKeys.DOCUMENT, document, true);
		addToValidate(InputKeys.PRIVACY_STATE, privacyState, true);
		addToValidate(InputKeys.DESCRIPTION, description, true);
	}
}
