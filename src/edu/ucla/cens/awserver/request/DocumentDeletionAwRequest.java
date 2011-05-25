package edu.ucla.cens.awserver.request;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Request class for deleting documents.
 * 
 * @author John Jenkins
 */
public class DocumentDeletionAwRequest extends ResultListAwRequest {
	/**
	 * Validates that the document ID isn't null or whitespace and then adds it
	 * to the appropriate map.
	 * 
	 * @param documentId The ID for the document to be deleted.
	 */
	public DocumentDeletionAwRequest(String documentId) {
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			throw new IllegalArgumentException("The document's ID cannot be null or whitespace only: " + documentId);
		}
		
		addToValidate(InputKeys.DOCUMENT_ID, documentId, true);
	}
}
