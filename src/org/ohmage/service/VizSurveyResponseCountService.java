package org.ohmage.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VisualizationRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Calls the R visualization server with the parameters that we received in the
 * request. Specifically, it calls the R survey response count API.
 * 
 * @author John Jenkins
 */
public class VizSurveyResponseCountService extends AbstractAnnotatingService {
	private static final Logger _logger = Logger.getLogger(VizSurveyResponseCountService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use should the communication with the
	 * 					server fails.
	 */
	public VizSurveyResponseCountService(AwRequestAnnotator annotator) {
		super(annotator);
	}

	/**
	 * Gets the parameters from the request and forwards the call to the remote
	 * R server. It then writes the result back in the 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Sending the request to the visualization server.");
		
		// Get the requested image's width.
		String width;
		try {
			width = (String) awRequest.getToProcessValue(InputKeys.VISUALIZATION_WIDTH);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.VISUALIZATION_WIDTH);
			throw new ServiceException(e);
		}
		
		// Get the requested image's height.
		String height;
		try {
			height = (String) awRequest.getToProcessValue(InputKeys.VISUALIZATION_HEIGHT);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.VISUALIZATION_HEIGHT);
			throw new ServiceException(e);
		}
		
		// Get the campaign ID.
		String campaignId = awRequest.getCampaignUrn();
		
		// Build the request.
		StringBuilder urlBuilder = new StringBuilder();
		try {
			String serverUrl = PreferenceCache.instance().lookup(PreferenceCache.KEY_VISUALIZATION_SERVER);
			urlBuilder.append(serverUrl);
			
			if(! serverUrl.endsWith("/")) {
				urlBuilder.append("/");
			}
		}
		catch(CacheMissException e) {
			_logger.error("Cache doesn't know about 'known' key: " + PreferenceCache.KEY_VISUALIZATION_SERVER);
			throw new ServiceException(e);
		}
		urlBuilder.append("responseplot?");
		
		// Get this machine's hostname.
		String hostname;
		try {
			hostname = InetAddress.getLocalHost().getHostName();
		}
		catch(UnknownHostException e) {
			getAnnotator().annotate(awRequest, "The sky is falling! Oh, and our hostname is unknown.");
			awRequest.setFailedRequest(true);
			return;
		}
		
		// Add the required parameters.
		urlBuilder.append("token='").append(awRequest.getUserToken()).append("'");
		urlBuilder.append("&server='").append("http://").append(hostname).append("/app").append("'");
		urlBuilder.append("&campaign_urn='").append(campaignId).append("'");
		urlBuilder.append("&!width=").append(width);
		urlBuilder.append("&!height=").append(height);
		String urlString = urlBuilder.toString();
	
		try {
			// Connect to the visualization server.
			URL url = new URL(urlString);
			URLConnection urlConnection = url.openConnection();
			
			// Check that the response code was 200.
			if(urlConnection instanceof HttpURLConnection) {
				HttpURLConnection httpUrlConnection = (HttpURLConnection) urlConnection;
				if(httpUrlConnection.getResponseCode() != 200) {
					getAnnotator().annotate(awRequest, "The server returned a non-200 response.");
				}
			}
			
			// Build the response.
			InputStream reader = urlConnection.getInputStream();
			
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int amountRead = 0;
			while((amountRead = reader.read(chunk)) != -1) {
				byteArrayStream.write(chunk, 0, amountRead);
			}
			
			// Set the response in the request.
			awRequest.addToReturn(VisualizationRequest.VISUALIZATION_REQUEST_RESULT, byteArrayStream.toByteArray(), true);
		}
		catch(MalformedURLException e) {
			_logger.error("Built a malformed URL: " + urlString);
			throw new ServiceException(e);
		}
		catch(IOException e) {
			getAnnotator().annotate(awRequest, "Error communicating with the visualization server: " + e.toString());
			awRequest.setFailedRequest(true);
			return;
		}
	}
}