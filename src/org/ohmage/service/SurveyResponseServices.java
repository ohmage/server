package org.ohmage.service;

import java.util.List;

import org.ohmage.dao.ImageDaos;
import org.ohmage.dao.SurveyResponseDaos;
import org.ohmage.dao.SurveyResponseImageDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class is responsible for creating, reading, updating, and deleting 
 * survey responses only. While it may read information from other classes, the
 * only values the functions in this class should take and return should 
 * pertain to survey responses. Likewise, it should only change the state of
 * survey responses.
 * 
 * @author John Jenkins
 */
public final class SurveyResponseServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private SurveyResponseServices() {}
	
	/**
	 * Deletes all of the images associated with a survey response then deletes
	 * the survey response itself.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param surveyResponseId The survey response's unique identifier.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static void deleteSurveyResponse(Request request, Long surveyResponseId) throws ServiceException {
		try {
			List<String> imageIds = SurveyResponseImageDaos.getImageIdsFromSurveyResponse(surveyResponseId);

			// Here we are deleting the images then deleting the survey 
			// response. If this fails, the entire service is aborted, but the
			// individual services that deleted the images are not rolled back.
			// This leads to an appropriate division of labor in the code; 
			// otherwise, the deleteSurveyResponse() function would need to 
			// modify the url_based_resource table and file system. If an image
			// deletion fails, the survey response will exist, but the images 
			// from the survey response that were deleted will be deleted. Not 
			// all of them will be delete, however; the one that caused the
			// exception will remain. While this may seem like an error, I felt
			// that it would be worse to have the user stuck in a situation 
			// where no images were being deleted than one where only part of a
			// survey response was deleted. If the user is trying to delete an
			// image, this will give them the chance to delete it even if 
			// another image and/or the survey response are causing a problem.
			for(String imageId : imageIds) {
				ImageDaos.deleteImage(imageId);
			}
			
			SurveyResponseDaos.deleteSurveyResponse(surveyResponseId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
}
