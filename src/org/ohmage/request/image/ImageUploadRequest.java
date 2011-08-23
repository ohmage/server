package org.ohmage.request.image;

import java.awt.image.BufferedImage;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.annotator.ErrorCodes;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.ImageServices;
import org.ohmage.service.UserImageServices;
import org.ohmage.validator.ImageValidators;

/**
 * <p>Stores an image based on the image ID as long as the corresponding prompt
 * response has already been uploaded via survey/upload</p>
 * <table border="1">
 *   <tr>
 *     <td>Parameter Name</td>
 *     <td>Description</td>
 *     <td>Required</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#CLIENT}</td>
 *     <td>A string describing the client that is making this request.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#IMAGE_ID}</td>
 *     <td>The image's unique identifier.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#DATA}</td>
 *     <td>The contents of the image.</td>
 *     <td>false</td>
 *   </tr>
 * </table>
 * 
 * @author John Jenkins
 */
public class ImageUploadRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ImageUploadRequest.class);
	
	private final String imageId;
	private final BufferedImage imageContents;
	
	/**
	 * Creates a new image upload request.
	 * 
	 * @param httpRequest The HttpServletRequest with the parameters for this
	 * 					  request.
	 */
	public ImageUploadRequest(HttpServletRequest httpRequest) {
		super(httpRequest, false);
		
		LOGGER.info("Creating an image upload request.");
		
		String tImageId = null;
		BufferedImage tImageContents = null;
		
		if(! isFailed()) {
			try {
				tImageId = ImageValidators.validateId(this, httpRequest.getParameter(InputKeys.IMAGE_ID));
				if(tImageId == null) {
					setFailed(ErrorCodes.IMAGE_INVALID_ID, "The image ID is missing: " + InputKeys.IMAGE_ID);
					throw new ValidationException("The image ID is missing: " + InputKeys.IMAGE_ID);
				}
				else if(httpRequest.getParameterValues(InputKeys.IMAGE_ID).length > 1) {
					setFailed(ErrorCodes.IMAGE_INVALID_ID, "Multiple owner values were given: " + InputKeys.IMAGE_ID);
					throw new ValidationException("Multiple owner values were given: " + InputKeys.IMAGE_ID);
				}

				tImageContents = ImageValidators.validateImageContents(this, getMultipartValue(httpRequest, InputKeys.DATA));
				if(tImageContents == null) {
					setFailed(ErrorCodes.IMAGE_INVALID_DATA, "The image's contents are missing: " + InputKeys.DATA);
					throw new ValidationException("The image's contents were missing.");
				}
			}
			catch(ValidationException e) {
				LOGGER.info(e.toString());
			}
		}
		
		imageId = tImageId;
		imageContents = tImageContents;
	}

	/**
	 * Services the request.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the image upload request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// This was originally done in two steps, first check that the 
			// prompt exists then check that it belongs to the user. The 
			// problem with this approach is that an attacker could 
			// continuously give random prompt IDs always receiving a "no such
			// prompt" response until they correctly hit one. At this point,
			// they would know that there exists a photo prompt with the given 
			// prompt ID but not necessarily who it belongs to. Doing it in one
			// step will prevent an attacker from gaining this piece of
			// information.
			LOGGER.info("Verifing that some photo prompt response exists for this user whose response is this image's ID.");
			UserImageServices.verifyPhotoPromptResponseExistsForUserAndImage(this, getUser().getUsername(), imageId);
			
			// We want to prevent someone from updating an image.
			LOGGER.info("Verifying that an image has not already been uploaded for this photo prompt.");
			ImageServices.verifyImageExistance(this, imageId, false);
			
			LOGGER.info("Saving the image.");
			ImageServices.createImage(this, getUser().getUsername(), getClient(), imageId, imageContents);
		}
		catch(ServiceException e) {
			e.logException(LOGGER);
		}
	}

	/**
	 * Responds to the image upload request with success or a failure message
	 * that contains a failure code and failure text.
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		LOGGER.info("Responding to the image upload request.");
		
		super.respond(httpRequest, httpResponse, null);
	}
}