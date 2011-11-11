package org.ohmage.validator;

import java.util.UUID;

import junit.framework.TestCase;

import org.junit.Assert;
import org.junit.Test;
import org.ohmage.exception.ValidationException;
import org.ohmage.test.ParameterSets;

/**
 * Tests the image validators.
 * 
 * @author John Jenkins
 */
public class ImageValidatorsTest extends TestCase {
	/**
	 * The image ID validator.
	 */
	@Test
	public void testValidateId() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ImageValidators.validateId(emptyValue));
			}
			
			try {
				ImageValidators.validateId("Invalid value.");
				fail("The image ID was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			UUID uuid = UUID.randomUUID();
			try {
				ImageValidators.validateId(uuid.toString());
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
	 * Tests the image size validator.
	 */
	@Test
	public void testValidateImageSize() {
		try {
			for(String emptyValue : ParameterSets.getEmptyValues()) {
				Assert.assertNull(ImageValidators.validateImageSize(emptyValue));
			}
			
			try {
				ImageValidators.validateImageSize("Invalid value.");
				fail("The image size was invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
			
			for(ImageValidators.ImageSize imageSize : ImageValidators.ImageSize.values()) {
				Assert.assertEquals(imageSize, ImageValidators.validateImageSize(imageSize.toString()));
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}

	/**
	 * Tests the image contents validator.
	 */
	@Test
	public void testValidateImageContents() {
		try {
			Assert.assertNull(ImageValidators.validateImageContents(null));
			Assert.assertNull(ImageValidators.validateImageContents(new byte[0]));
			
			try {
				ImageValidators.validateImageContents("Invalid value.".getBytes());
				fail("The image contents were invalid.");
			}
			catch(ValidationException e) {
				// Passed.
			}
		}
		catch(ValidationException e) {
			fail("A validation exception was thrown: " + e.getMessage());
		}
	}
}