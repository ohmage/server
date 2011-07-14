package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentPrivacyStateCache;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.request.Request;
import org.ohmage.util.StringUtils;

/**
 * This class contains the functionality for validating document information.
 * 
 * @author John Jenkins
 */
public class DocumentValidators {
	private static final Logger LOGGER = Logger.getLogger(DocumentValidators.class);
	
	/**
	 * Default constructor. Made private to prevent instantiation.
	 */
	private DocumentValidators() {}
	
	/**
	 * Validates a document's unique identifier.
	 * 
	 * @param request The request that is performing this validation.
	 * 
	 * @param documentId The document ID in question.
	 * 
	 * @return Returns the document's ID if it is valid. Returns null if it is
	 * 		   null or whitespace only.
	 * 
	 * @throws ValidationException Thrown if the document's ID is not null, not
	 * 							   whitespace only, and not a valid document 
	 * 							   ID.
	 */
	public static String validateDocumentId(Request request, String documentId) throws ValidationException {
		LOGGER.info("Validating a document's ID.");
		
		if(UuidValidators.validateUuid(documentId)) {
			if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
				return null;
			}
			else {
				return documentId;
			}
		}
		else {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_ID, "The document ID is invalid: " + documentId);
			throw new ValidationException("The document ID is invalid: " + documentId);
		}
	}
	
	/**
	 * Validates that a document's privacy state is a known privacy state.
	 * 
	 * @param request The request that is having this privacy state validated.
	 * 
	 * @param privacyState The privacy state to validate.
	 * 
	 * @return If the privacy state is null or whitespace only, null is 
	 * 		   returned. Otherwise, the privacy state is returned.
	 * 
	 * @throws ValidationException Thrown if the privacy state is not null nor
	 * 							   whitespace only and is not a valid document
	 * 							   privacy state.
	 */
	public static String validatePrivacyState(Request request, String privacyState) throws ValidationException {
		LOGGER.info("Validating a document's privacy state.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
		
		if(DocumentPrivacyStateCache.instance().getKeys().contains(privacyState)) {
			return privacyState;
		}
		else {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_PRIVACY_STATE, "Unknown privacy state: " + privacyState);
			throw new ValidationException("The document's privacy state is unknown: " + privacyState);
		}
	}
	
	/**
	 * Validates that a document role is known.
	 * 
	 * @param request The request that is validating this document role.
	 * 
	 * @param role The document role to validate.
	 * 
	 * @return If the role is null or whitespace only, null is returned. 
	 * 		   Otherwise, the role is returned.
	 * 
	 * @throws ValidationException Thrown if the role is no null nor whitespace
	 * 							   only and is an unknown role.
	 */
	public static String validateRole(Request request, String role) throws ValidationException {
		LOGGER.info("Validating a document role.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			return null;
		}
		
		if(DocumentRoleCache.instance().getKeys().contains(role)) {
			return role;
		}
		else {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_ROLE, "Invalid document role: " + role);
			throw new ValidationException("Invalid document role: " + role);
		}
	}
	
	/**
	 * Validates that a 'personal documents' value is a valid value. It should
	 * be a boolean value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param value The value is be validated.
	 * 
	 * @return If the value is null or whitespace only, null is returned; 
	 * 		   otherwise, the value is returned.
	 * 
	 * @throws ValidationException Thrown if the value is not a valid 'personal
	 * 							   documents' value.
	 */
	public static Boolean validatePersonalDocuments(Request request, String value) throws ValidationException {
		LOGGER.info("Validating a 'personal documents' value.");
		
		if(BooleanValidators.validateBoolean(value)) {
			if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
				return null;
			}
			else {
				if("true".equals(value)) {
					return true;
				}
				else {
					return false;
				}
			}
		}
		else {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, "Invalid personal documents value: " + value);
			throw new ValidationException("Invalid personal documents value: " + value);
		}
	}
}