package org.ohmage.service;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.Calendar;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.ohmage.cache.CacheMissException;
import org.ohmage.cache.PreferenceCache;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.request.VisualizationRequest;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Sends the request to the visualization server and sets the result in the 
 * request to be returned to the original requester.
 * 
 * @author John Jenkins
 */
public class VizScatterPlotService extends AbstractAnnotatingService {
	private static final Logger _logger = Logger.getLogger(VizScatterPlotService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use should the communication with the
	 * 					server fails.
	 */
	public VizScatterPlotService(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Builds and sends the request to the visualization server and then 
	 * places the response in our internal request as as response.
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
		
		// Get the start date.
		String startDate;
		try {
			startDate = (String) awRequest.getToProcessValue(InputKeys.START_DATE);
		}
		catch(IllegalArgumentException e) {
			startDate = null;
		}
		
		// Get the start date.
		String endDate;
		try {
			endDate = (String) awRequest.getToProcessValue(InputKeys.END_DATE);
		}
		catch(IllegalArgumentException e) {
			endDate = null;
		}
		
		// Get the start date.
		String privacyState;
		try {
			privacyState = (String) awRequest.getToProcessValue(InputKeys.PRIVACY_STATE);
		}
		catch(IllegalArgumentException e) {
			privacyState = null;
		}
		
		String promptId;
		try {
			promptId = (String) awRequest.getToProcessValue(InputKeys.PROMPT_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.PROMPT_ID);
			throw new ServiceException(e);
		}
		
		String prompt2Id;
		try {
			prompt2Id = (String) awRequest.getToProcessValue(InputKeys.PROMPT2_ID);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing required parameter: " + InputKeys.PROMPT2_ID);
			throw new ServiceException(e);
		}
		
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
		urlBuilder.append("scatterplot?");
		
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
		
		String httpString = "http";
		try {
			InputStream in = new FileInputStream(PreferenceCache.instance().lookup(PreferenceCache.KEY_PROPERTIES_FILE));
			Properties properties = new Properties();
			properties.load(in);
			in.close();
			
			String sslEnabled = properties.getProperty(PreferenceCache.KEY_SSL_ENABLED);
			if((sslEnabled != null) && (sslEnabled.equals("true"))) {
				httpString = "https";
			}
		}
		catch(CacheMissException e) {
			_logger.error("Unknown value for 'known' key '" + PreferenceCache.KEY_PROPERTIES_FILE + "'. Is the cache database missing a key-value pair?", e);
			throw new ServiceException(e);
		}
		catch(IOException e) {
			_logger.error("Missing the properties file that should have been built with the WAR file.", e);
			throw new ServiceException(e);
		}
		
		// Add the required parameters.
		urlBuilder.append("token='").append(awRequest.getUserToken()).append("'");
		urlBuilder.append("&server='").append(httpString).append("://").append(hostname).append("/app").append("'");
		urlBuilder.append("&campaign_urn='").append(campaignId).append("'");
		urlBuilder.append("&prompt_id='").append(promptId).append("'");
		urlBuilder.append("&prompt2_id='").append(prompt2Id).append("'");
		urlBuilder.append("&!width=").append(width);
		urlBuilder.append("&!height=").append(height);
		
		// Add the optional parameters.
		if(startDate != null) {
			urlBuilder.append("&start_date='").append(startDate).append("'");
		}
		if(endDate != null) {
			urlBuilder.append("&end_date='").append(endDate).append("'");
		}
		if(privacyState != null) {
			urlBuilder.append("&privacy_state='").append(privacyState).append("'");
		}
		
		String urlString = urlBuilder.toString();
	
		try {
			long startTime = Calendar.getInstance().getTimeInMillis();
			
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
			
			long responseTime = Calendar.getInstance().getTimeInMillis();
			
			// Build the response.
			InputStream reader = urlConnection.getInputStream();
			
			ByteArrayOutputStream byteArrayStream = new ByteArrayOutputStream();
			byte[] chunk = new byte[4096];
			int amountRead = 0;
			while((amountRead = reader.read(chunk)) != -1) {
				byteArrayStream.write(chunk, 0, amountRead);
			}
			
			long downloadTime = Calendar.getInstance().getTimeInMillis();
			
			_logger.debug("It took " + (responseTime - startTime) + " milliseconds to hear back from the visualization server.");
			_logger.debug("It took " + (downloadTime - responseTime) + " milliseconds to download the image.");
			_logger.debug("Therefore, the entire request took " + (downloadTime - startTime) + " milliseconds.");
			
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