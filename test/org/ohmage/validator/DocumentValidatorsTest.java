package org.ohmage.validator;

import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.domain.Document;
import org.ohmage.exception.ValidationException;

/**
 * Tests the document validators.
 * 
 * @author John Jenkins
 */
public class DocumentValidatorsTest extends TestCase {
	/**
	 * Tests the document ID validator.
	 */
	@Test
	public void testValidateDocumentId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DocumentValidators.validateDocumentId(emptyValue));
			}
			
			try {
				DocumentValidators.validateDocumentId("Invalid value.");
				fail("The document ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			UUID uuid = UUID.randomUUID();
			try {
				DocumentValidators.validateDocumentId(uuid.toString());
			}
			catch(ValidationException e) {
				fail("A randomly-generated, yet valid, UUID '" + uuid.toString() + "' was rejected: " + e.getMessage());
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the document privacy state validator.
	 */
	@Test
	public void testValidatePrivacyState() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DocumentValidators.validatePrivacyState(emptyValue));
			}
			
			try {
				DocumentValidators.validatePrivacyState("Invalid value.");
				fail("The document privacy state was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(Document.PrivacyState privacyState : Document.PrivacyState.values()) {
				Assert.assertEquals(privacyState, DocumentValidators.validatePrivacyState(privacyState.toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the document role validator.
	 */
	@Test
	public void testValidateRole() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DocumentValidators.validateRole(emptyValue));
			}
			
			try {
				DocumentValidators.validateRole("Invalid value.");
				fail("The campaign ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(Document.Role role : Document.Role.values()) {
				Assert.assertEquals(role, DocumentValidators.validateRole(role.toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the personal documents validator.
	 */
	@Test
	public void testValidatePersonalDocuments() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DocumentValidators.validatePersonalDocuments(emptyValue));
			}
			
			try {
				DocumentValidators.validatePersonalDocuments("Invalid value.");
				fail("The campaign ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			Assert.assertEquals(true, DocumentValidators.validatePersonalDocuments("true"));
			Assert.assertEquals(false, DocumentValidators.validatePersonalDocuments("false"));
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the document name validator.
	 */
	@Test
	public void testValidateName() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DocumentValidators.validateName(emptyValue));
			}
			
			StringBuilder nameBuilder = new StringBuilder();
			for(int i = 0; i < DocumentValidators.MAX_NAME_LENGTH; i++) {
				nameBuilder.append('a');
			}
			nameBuilder.append('a');
			
			try {
				DocumentValidators.validateName(nameBuilder.toString());
				fail("A name whose length is too long passed validation: " + nameBuilder.toString());
			}
			catch(ValidationException e) {
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the document description validator.
	 */
	@Test
	public void testValidateDescription() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(DocumentValidators.validateDescription(emptyValue));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}