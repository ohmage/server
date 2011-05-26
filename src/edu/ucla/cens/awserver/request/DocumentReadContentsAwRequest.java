package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Request for reading the contents of a document.
 * 
 * @author John Jenkins
 */
public class DocumentReadContentsAwRequest extends ResultListAwRequest {
	public static final String KEY_DOCUMENT_FILE = "document_read_docuemnt_key_document_file";
	
	/**
	 * Creates a request to read the contents of a document.
	 * 
	 * @param documentId An identifier for the document that is being read.
	 * 
	 * @throws IllegalArgumentException Thrown if the document ID is null or
	 * 									whitespace only.
	 */
	public DocumentReadContentsAwRequest(String documentId) throws IllegalArgumentException {
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document ID must be non-null and not whitespace.");
		}
		
		addToValidate(InputKeys.DOCUMENT_ID, documentId, true);
	}
}
