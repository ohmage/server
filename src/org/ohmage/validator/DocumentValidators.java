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
	
	private static final int MAX_NAME_LENGTH = 255;
	
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
		
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			return null;
		}
		
		if(StringUtils.isValidUuid(documentId)) {
			return documentId;
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
	 * Validates that a 'personal documents' Value is a valid Value. It should
	 * be a boolean Value.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The Value is be validated.
	 * 
	 * @return If the Value is null or whitespace only, null is returned; 
	 * 		   otherwise, the Value is returned.
	 * 
	 * @throws ValidationException Thrown if the Value is not a valid 'personal
	 * 							   documents' Value.
	 */
	public static Boolean validatePersonalDocuments(Request request, String value) throws ValidationException {
		LOGGER.info("Validating a 'personal documents' Value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value)) {
			return StringUtils.decodeBoolean(value);
		}
		else {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, "Invalid personal documents Value: " + value);
			throw new ValidationException("Invalid personal documents Value: " + value);
		}
	}
	
	/**
	 * Validates that the name of the document is not profane and not longer 
	 * than a set length.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The Value to be validated.
	 * 
	 * @return Returns null if the name is null or whitespace only; otherwise,
	 * 		   it returns the name.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or is
	 * 							   too long.
	 */
	public static String validateName(Request request, String value) throws ValidationException {
		LOGGER.info("Validating a document's name.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value)) {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_NAME, "The name of this document contains profanity: " + value);
			throw new ValidationException("The name of this document contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value, 0, MAX_NAME_LENGTH)) {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_NAME, "The name of this document is too long. The limit is " + MAX_NAME_LENGTH + " characters.");
			throw new ValidationException("The name of this document is too long. The limit is " + MAX_NAME_LENGTH + " characters.");
		}
		else {
			return value;
		}
	}
	
	/**
	 * Validates that the description of a document does not contain profanity.
	 * 
	 * @param request The Request that is performing this validation.
	 * 
	 * @param Value The Value to be validated.
	 * 
	 * @return Returns null if the description is null or whitespace only;
	 * 		   otherwise, it returns the description.
	 * 
	 * @throws ValidationException Thrown if the description contains 
	 * 							   profanity.
	 */
	public static String validateDescription(Request request, String value) throws ValidationException {
		LOGGER.info("Validating a document's description.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value)) {
			request.setFailed(ErrorCodes.DOCUMENT_INVALID_DESCRIPTION, "The document's description contains profanity.");
			throw new ValidationException("The document's description contains profanity.");
		}
		else {
			return value;
		}
	}
}