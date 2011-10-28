package org.ohmage.validator;

import org.apache.log4j.Logger;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Document;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class contains the functionality for validating document information.
 * 
 * @author John Jenkins
 */
public class DocumentValidators {
	private static final Logger LOGGER = Logger.getLogger(DocumentValidators.class);
	
	/**
	 * The maximum allowed length for a document's name.
	 */
	public static final int MAX_NAME_LENGTH = 255;
	
	/**
	 * Default constructor. Made private to prevent instantiation.
	 */
	private DocumentValidators() {}
	
	/**
	 * Validates a document's unique identifier.
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
	public static String validateDocumentId(final String documentId) 
			throws ValidationException {
		
		LOGGER.info("Validating a document's ID.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(documentId)) {
			return null;
		}
		
		if(StringUtils.isValidUuid(documentId.trim())) {
			return documentId.trim();
		}
		else {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_ID, 
					"The document ID is invalid: " + documentId);
		}
	}
	
	/**
	 * Validates that a document's privacy state is a known privacy state.
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
	public static Document.PrivacyState validatePrivacyState(
			final String privacyState) throws ValidationException {
		
		LOGGER.info("Validating a document's privacy state.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(privacyState)) {
			return null;
		}
			
		try {
			return Document.PrivacyState.getValue(privacyState);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_PRIVACY_STATE, 
					"The document's privacy state is unknown: " + privacyState,
					e);
		}
	}
	
	/**
	 * Validates that a document role is known.
	 * 
	 * @param role The document role to validate.
	 * 
	 * @return If the role is null or whitespace only, null is returned. 
	 * 		   Otherwise, the role is returned.
	 * 
	 * @throws ValidationException Thrown if the role is no null nor whitespace
	 * 							   only and is an unknown role.
	 */
	public static Document.Role validateRole(final String role) 
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(role)) {
			return null;
		}
		
		try {
			return Document.Role.getValue(role);
		}
		catch(IllegalArgumentException e) {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_ROLE, 
					"Invalid document role: " + role,
					e);
		}
	}
	
	/**
	 * Validates that a 'personal documents' value is a valid value. It should
	 * be a boolean value.
	 * 
	 * @param value The value is be validated.
	 * 
	 * @return If the value is null or whitespace only, null is returned; 
	 * 		   otherwise, the value is returned.
	 * 
	 * @throws ValidationException Thrown if the value is not a valid 'personal
	 * 							   documents' value.
	 */
	public static Boolean validatePersonalDocuments(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating a 'personal documents' value.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isValidBoolean(value.trim())) {
			return StringUtils.decodeBoolean(value.trim());
		}
		else {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_PERSONAL_DOCUMENTS_VALUE, 
					"Invalid personal documents value: " + value);
		}
	}
	
	/**
	 * Validates that the name of the document is not profane and not longer 
	 * than a set length.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return Returns null if the name is null or whitespace only; otherwise,
	 * 		   it returns the name.
	 * 
	 * @throws ValidationException Thrown if the name contains profanity or is
	 * 							   too long.
	 */
	public static String validateName(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating a document's name.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_NAME, 
					"The name of this document contains profanity: " + value);
		}
		else if(! StringUtils.lengthWithinLimits(value.trim(), 0, MAX_NAME_LENGTH)) {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_NAME, 
					"The name of this document is too long. The limit is " + 
						MAX_NAME_LENGTH + 
						" characters.");
		}
		else {
			return value.trim();
		}
	}
	
	/**
	 * Validates that the description of a document does not contain profanity.
	 * 
	 * @param value The value to be validated.
	 * 
	 * @return Returns null if the description is null or whitespace only;
	 * 		   otherwise, it returns the description.
	 * 
	 * @throws ValidationException Thrown if the description contains 
	 * 							   profanity.
	 */
	public static String validateDescription(final String value) 
			throws ValidationException {
		
		LOGGER.info("Validating a document's description.");
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		if(StringUtils.isProfane(value.trim())) {
			throw new ValidationException(
					ErrorCode.DOCUMENT_INVALID_DESCRIPTION, 
					"The document's description contains profanity.");
		}
		else {
			return value.trim();
		}
	}
}