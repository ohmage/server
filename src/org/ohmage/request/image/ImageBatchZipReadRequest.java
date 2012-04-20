package org.ohmage.request.image;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.ohmage.domain.campaign.PromptResponse;
import org.ohmage.domain.campaign.RepeatableSetResponse;
import org.ohmage.domain.campaign.Response;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.survey.SurveyResponseRequest;
import org.ohmage.service.ImageServices;

/**
 * <p>Reads all of the images for a campaign that match the given criteria and
 * returns them as a ZIP file.</p>
 * <table>
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
 *     <td>{@value org.ohmage.request.InputKeys#CAMPAIGN_URN}</td>
 *     <td>The campaign URN to use when retrieving responses.</td>
 *     <td>true</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#USER_LIST}</td>
 *     <td>A comma-separated list of usernames to retrieve responses for
 *         or the value {@value URN_SPECIAL_ALL}</td>
 *     <td>true</td>
 *   </tr>  
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_ID_LIST}</td>
 *     <td>A comma-separated list of survey ids to retrieve responses for
 *         or the value {@link #_URN_SPECIAL_ALL}. This key is only
 *         optional if {@value org.ohmage.request.InputKeys#PROMPT_ID_LIST}
 *         is not present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PROMPT_ID_LIST}</td>
 *     <td>A comma-separated list of prompt ids to retrieve responses for
 *         or the value {@link #_URN_SPECIAL_ALL}. This key is only
 *         optional if {@value org.ohmage.request.InputKeys#SURVEY_ID_LIST}
 *         is not present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#START_DATE}</td>
 *     <td>The start date to use for results between dates.
 *         Required if end date is present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#END_DATE}</td>
 *     <td>The end date to use for results between dates.
 *         Required if start date is present.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#PRIVACY_STATE}</td>
 *     <td>Filters the results by their associated privacy state.</td>
 *     <td>false</td>
 *   </tr>
 *   <tr>
 *     <td>{@value org.ohmage.request.InputKeys#SURVEY_RESPONSE_ID_LIST}</td>
 *     <td>Filters the results to only those whose UUID is in the given list.
 *       </td>
 *     <td>false</td>
 *   </tr>
 * </table>
 *
 * @author John Jenkins
 */
public class ImageBatchZipReadRequest extends SurveyResponseRequest {
	private static final Logger LOGGER = 
			Logger.getLogger(ImageBatchZipReadRequest.class);
	
	private final Map<UUID, URL> imageUrls;
	
	/**
	 * Creates a new ImageBatchZipReadRequest.
	 * 
	 * @param httpRequest The HTTP request.
	 */
	public ImageBatchZipReadRequest(final HttpServletRequest httpRequest) {
		super(httpRequest);
		
		// There are no parameters specific to this API that aren't covered in
		// the SurveyResponseRequest.
		if(! isFailed()) {
			LOGGER.info("Creating an image ZIP read request.");
		}
		
		imageUrls = new HashMap<UUID, URL>();
	}
	
	/**
	 * Gathers all of the image IDs and their associated URLs.
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing a image ZIP read request.");
		
		// We only need the prompt response.
		Collection<SurveyResponse.ColumnKey> promptResponseColumn =
				new ArrayList<SurveyResponse.ColumnKey>(1);
		promptResponseColumn.add(SurveyResponse.ColumnKey.PROMPT_RESPONSE);
		
		super.service(
				promptResponseColumn, 
				"photo",
				null,
				false, 
				0, 
				Long.MAX_VALUE);
		if(isFailed()) {
			return;
		}
		
		LOGGER.info("Gathering the UUIDs from the survey responses.");
		Collection<UUID> imageIds = new HashSet<UUID>();
		for(SurveyResponse surveyResponse : getSurveyResponses()) {
			imageIds.addAll(getImageIds(surveyResponse.getResponses().values()));
		}
		
		LOGGER.info("Getting the URL for each UUID.");
		try {
			for(UUID imageId : imageIds) {
				URL imageUrl = ImageServices.instance().getImageUrl(imageId);
				if(imageUrl == null) {
					LOGGER.debug(
							"The image doesn't have a URL: " + 
								imageId.toString());
				}
				else {
					imageUrls.put(imageId, imageUrl);
				}
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	@Override
	public void respond(
			HttpServletRequest httpRequest,
			HttpServletResponse httpResponse) {
		
		// Check for failure.
		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
			return;
		}
		
		// We are going to try to write the response, so we will need to set
		// the header to indicate that this will be an attachment.
		httpResponse.setHeader(
				"Content-Disposition", 
				"attachment; filename=images.zip");
		
		// Create the zip stream to the outside world.
		ZipOutputStream zipStream = null;
		try {
			zipStream = 
					new ZipOutputStream(
						getOutputStream(httpRequest, httpResponse));
		}
		catch(IOException e) {
			LOGGER.error("Unable to write response message. Aborting.", e);
			return;
		}
		
		// Stream each image into the input of the ZIP stream.
		int lengthRead;
		byte[] buffer = new byte[4096];
		for(UUID imageId : imageUrls.keySet()) {
			// First, attempt to connect to the image. If this cannot be done,
			// we will simply skip this image and not return it in the ZIP 
			// file.
			InputStream imageStream;
			try {
				URL imageUrl = imageUrls.get(imageId);
				imageStream = imageUrl.openStream();
			}
			catch(IOException e) {
				LOGGER.info(
						"The image does not exist, so it will not be added to the ZIP file: " +
							imageId.toString(),
						e);
				continue;
			}
			
			// Next, we create a new entry in the ZIP file for this image.
			try {
				zipStream.putNextEntry(new ZipEntry(imageId.toString()));
			}
			catch(IOException e) {
				LOGGER.error(
						"Unable to add an entry to the stream: " +
							imageId.toString(),
						e);
				break;
			}
			
			// Attempt to read from the image buffer and shuffle it into the 
			// ZIP file buffer.
			try {
				while((lengthRead = imageStream.read(buffer)) != -1) {
					try {
						zipStream.write(buffer, 0, lengthRead);
					}
					catch(IOException e) {
						LOGGER.error(
								"There was a problem writing the response: " +
									imageId.toString(),
								e);
						break;
					}
				}
			}
			catch(IOException e) {
				LOGGER.error(
						"There was a problem reading the image's contents: " +
							imageId.toString(),
						e);
				break;
			}
			
			// Once we have copied the entire image into the buffer, we can 
			// close it. If it fails to close, we don't really care because we
			// have supposedly read everything we need.
			try {
				imageStream.close();	
			}
			catch(IOException e) {
				LOGGER.error(
						"There was a problem closing the connection to the image: " +
							imageId.toString(),
						e);
				
				// We don't abort here, because everything should have been
				// successfully written to to the stream. Therefore, we should
				// log that it happened, but we can safely continue.
			}
			
			// Finally, close this entry in the ZIP file.
			try {
				zipStream.closeEntry();
			}
			catch(IOException e) {
				LOGGER.error(
						"There was a problem closing this image's entry: " +
							imageId.toString(),
						e);
				break;
			}
		}
		
		// No matter what happens, we still try to flush what we did write to
		// the output stream.
		try {
			zipStream.flush();
		}
		catch(IOException e) {
			LOGGER.error("Couldn't flush the stream.", e);
		}
		
		// Finally, we always want to close the stream.
		try {
			zipStream.close();
		}
		catch(IOException e) {
			LOGGER.error("Couldn't close the stream.", e);
		}
	}
	
	/**
	 * Cycles through a collection of responses and retrieves the photo prompt
	 * response's UUIDs.
	 * 
	 * @param responses The collection of responses.
	 * 
	 * @return The photo UUIDs for those responses.
	 */
	private Collection<UUID> getImageIds(
			final Collection<Response> responses) {
		
		Collection<UUID> result = new LinkedList<UUID>();
		
		for(Response response : responses) {
			if(response instanceof PromptResponse) {
				Object responseObject = response.getResponse();
				
				if(responseObject instanceof UUID) {
					result.add((UUID) response.getResponse());	
				}
			}
			else if(response instanceof RepeatableSetResponse) {
				for(Map<Integer, Response> iteration :
					((RepeatableSetResponse) response).getResponseGroups().values()) {
					
					result.addAll(getImageIds(iteration.values()));
				}
			}
		}
		
		return result;
	}
}