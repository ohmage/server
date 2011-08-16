package org.ohmage.request;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import javax.servlet.http.HttpServletRequest;

import org.ohmage.request.audit.AuditReadRequest;
import org.ohmage.request.auth.AuthRequest;
import org.ohmage.request.auth.AuthTokenRequest;
import org.ohmage.request.campaign.CampaignCreationRequest;
import org.ohmage.request.campaign.CampaignDeletionRequest;
import org.ohmage.request.campaign.CampaignReadRequest;
import org.ohmage.request.campaign.CampaignUpdateRequest;
import org.ohmage.request.clazz.ClassCreationRequest;
import org.ohmage.request.clazz.ClassDeletionRequest;
import org.ohmage.request.clazz.ClassReadRequest;
import org.ohmage.request.clazz.ClassRosterReadRequest;
import org.ohmage.request.clazz.ClassRosterUpdateRequest;
import org.ohmage.request.clazz.ClassUpdateRequest;
import org.ohmage.request.document.DocumentCreationRequest;
import org.ohmage.request.document.DocumentDeletionRequest;
import org.ohmage.request.document.DocumentReadContentsRequest;
import org.ohmage.request.document.DocumentReadRequest;
import org.ohmage.request.document.DocumentUpdateRequest;
import org.ohmage.request.image.ImageReadRequest;
import org.ohmage.request.mobility.MobilityUploadRequest;
import org.ohmage.request.survey.SurveyUploadRequest;
import org.ohmage.request.user.UserChangePasswordRequest;
import org.ohmage.request.user.UserCreationRequest;
import org.ohmage.request.user.UserDeletionRequest;
import org.ohmage.request.user.UserInfoReadRequest;
import org.ohmage.request.user.UserReadRequest;
import org.ohmage.request.user.UserStatsReadRequest;
import org.ohmage.request.user.UserUpdateRequest;
import org.ohmage.util.StringUtils;

/**
 * Request builder from an HTTP request.
 * 
 * @author John Jenkins
 */
public final class RequestBuilder {
	/**
	 * Default constructor. Made private because this class should never be
	 * instantiated. Instead, the static builder method should be called.
	 */
	private RequestBuilder() {}
	
	private static final String KEY_CONTENT_ENCODING = "Content-Encoding";
	private static final String VALUE_GZIP = "gzip";
	
	private static final int CHUNK_SIZE = 4096;
	
	private static final String PARAMETER_SEPARATOR = "&";
	private static final String PARAMETER_VALUE_SEPARATOR = "=";
	
	private static final String API_ROOT = "/app";
	
	// Audit
	public static final String API_AUDIT_READ = API_ROOT + "/audit/read";
	
	// Authentication
	public static final String API_USER_AUTH = API_ROOT + "/user/auth";
	public static final String API_USER_AUTH_TOKEN = API_ROOT + "/user/auth_token";
	
	// Campaign
	public static final String API_CAMPAIGN_CREATE = API_ROOT + "/campaign/create";
	public static final String API_CAMPAIGN_READ = API_ROOT + "/campaign/read";
	public static final String API_CAMPAIGN_UPDATE = API_ROOT + "/campaign/update";
	public static final String API_CAMPAIGN_DELETE = API_ROOT + "/campaign/delete";
	
	// Class
	public static final String API_CLASS_CREATE = API_ROOT + "/class/create";
	public static final String API_CLASS_READ = API_ROOT + "/class/read";
	public static final String API_CLASS_ROSTER_READ = API_ROOT + "/class/roster/read";
	public static final String API_CLASS_UPDATE = API_ROOT + "/class/update";
	public static final String API_CLASS_ROSTER_UPDATE = API_ROOT + "/class/roster/update";
	public static final String API_CLASS_DELETE = API_ROOT + "/class/delete";
	
	// Config
	public static final String API_CONFIG_READ = API_ROOT + "/config/read";
	
	// Document
	public static final String API_DOCUMENT_CREATE = API_ROOT + "/document/create";
	public static final String API_DOCUMENT_READ = API_ROOT + "/document/read";
	public static final String API_DOCUMENT_READ_CONTENTS = API_ROOT + "/document/read/contents";
	public static final String API_DOCUMENT_UPDATE = API_ROOT + "/document/update";
	public static final String API_DOCUMENT_DELETE = API_ROOT + "/document/delete";

	// Image
	public static final String API_IMAGE_READ = API_ROOT + "/image/read";
	
	// Mobility
	public static final String API_MOBILITY_UPLOAD = API_ROOT + "/mobility/upload";

	// Survey
	private static final String API_SURVEY_UPLOAD = API_ROOT + "/survey/upload";

	// User
	public static final String API_USER_CREATE = API_ROOT + "/user/create";
	public static final String API_USER_READ = API_ROOT + "/user/read";
	public static final String API_USER_INFO_READ = API_ROOT + "/user_info/read";
	public static final String API_USER_STATS_READ = API_ROOT + "/user_stats/read";
	public static final String API_USER_UPDATE = API_ROOT + "/user/update";
	public static final String API_USER_CHANGE_PASSWORD = API_ROOT + "/user/change_password";
	public static final String API_USER_DELETE = API_ROOT + "/user/delete";
	
	/**
	 * Builds a new request based on the request's URI. This will always return
	 * a request and will never return null. If the URI is unknown it will 
	 * return a FailedRequest().
	 * 
	 * @param httpRequest The incoming HTTP request.
	 * 
	 * @return A new Request object based on the HTTP request's URI.
	 */
	public static Request buildRequest(HttpServletRequest httpRequest) {
		String requestUri = httpRequest.getRequestURI();

		// Retrieve the parameters from the request. This will unzip them if
		// necessary.
		Map<String, String[]> parameters;
		try {
			parameters = getParameters(httpRequest);
		}
		catch(IllegalArgumentException e) {
			return new FailedRequest();
		}
		catch(IllegalStateException e) {
			return new FailedRequest();
		}
		
		// Config
		if(API_CONFIG_READ.equals(requestUri)) {
			return new ConfigReadRequest();
		}
		// Authentication
		else if(API_USER_AUTH.equals(requestUri)) {
			return new AuthRequest(httpRequest);
		}
		else if(API_USER_AUTH_TOKEN.equals(requestUri)) {
			return new AuthTokenRequest(httpRequest);
		}
		// Audit
		else if(API_AUDIT_READ.equals(requestUri)) {
			return new AuditReadRequest(httpRequest);
		}
		// Campaign
		else if(API_CAMPAIGN_CREATE.equals(requestUri)) {
			return new CampaignCreationRequest(httpRequest);
		}
		else if(API_CAMPAIGN_READ.equals(requestUri)) {
			return new CampaignReadRequest(httpRequest);
		}
		else if(API_CAMPAIGN_UPDATE.equals(requestUri)) {
			return new CampaignUpdateRequest(httpRequest);
		}
		else if(API_CAMPAIGN_DELETE.equals(requestUri)) {
			return new CampaignDeletionRequest(httpRequest);
		}
		// Class
		else if(API_CLASS_CREATE.equals(requestUri)) {
			return new ClassCreationRequest(httpRequest);
		}
		else if(API_CLASS_READ.equals(requestUri)) {
			return new ClassReadRequest(httpRequest);
		}
		else if(API_CLASS_ROSTER_READ.equals(requestUri)) {
			return new ClassRosterReadRequest(httpRequest);
		}
		else if(API_CLASS_UPDATE.equals(requestUri)) {
			return new ClassUpdateRequest(httpRequest);
		}
		else if(API_CLASS_ROSTER_UPDATE.equals(requestUri)) {
			return new ClassRosterUpdateRequest(httpRequest);
		}
		else if(API_CLASS_DELETE.equals(requestUri)) {
			return new ClassDeletionRequest(httpRequest);
		}
		// Document
		else if(API_DOCUMENT_CREATE.equals(requestUri)) {
			return new DocumentCreationRequest(httpRequest);
		}
		else if(API_DOCUMENT_READ.equals(requestUri)) {
			return new DocumentReadRequest(httpRequest);
		}
		else if(API_DOCUMENT_READ_CONTENTS.equals(requestUri)) {
			return new DocumentReadContentsRequest(httpRequest);
		}
		else if(API_DOCUMENT_UPDATE.equals(requestUri)) {
			return new DocumentUpdateRequest(httpRequest);
		}
		else if(API_DOCUMENT_DELETE.equals(requestUri)) {
			return new DocumentDeletionRequest(httpRequest);
		}
		// Image
		else if(API_IMAGE_READ.equals(requestUri)) {
			return new ImageReadRequest(httpRequest);
		}
		else if(API_MOBILITY_UPLOAD.equals(requestUri)) {
			return new MobilityUploadRequest(parameters);
		}
		//Survey
		else if(API_SURVEY_UPLOAD.equals(requestUri)) {
			return new SurveyUploadRequest(httpRequest);
		}
		// User
		else if(API_USER_CREATE.equals(requestUri)) {
			return new UserCreationRequest(httpRequest);
		}
		else if(API_USER_READ.equals(requestUri)) {
			return new UserReadRequest(httpRequest);
		}
		else if(API_USER_INFO_READ.equals(requestUri)) {
			return new UserInfoReadRequest(httpRequest);
		}
		else if(API_USER_STATS_READ.equals(requestUri)) {
			return new UserStatsReadRequest(httpRequest);
		}
		else if(API_USER_UPDATE.equals(requestUri)) {
			return new UserUpdateRequest(httpRequest);
		}
		else if(API_USER_CHANGE_PASSWORD.equals(requestUri)) {
			return new UserChangePasswordRequest(httpRequest);
		}
		else if(API_USER_DELETE.equals(requestUri)) {
			return new UserDeletionRequest(httpRequest);
		}
		
		// The URI is unknown.
		return new FailedRequest();
	}
	
	/**
	 * Returns whether or not some URI is known.
	 * 
	 * @param uri The URI to check.
	 * 
	 * @return Returns true if the URI is known; false, otherwise.
	 */
	public static boolean knownUri(String uri) {
		if(
				// Config
				API_CONFIG_READ.equals(uri) ||
				// Authentication
				API_USER_AUTH.equals(uri) ||
				API_USER_AUTH_TOKEN.equals(uri) ||
				// Audit
				API_AUDIT_READ.equals(uri) ||
				// Campaign
				API_CAMPAIGN_CREATE.equals(uri) ||
				API_CAMPAIGN_READ.equals(uri) ||
				API_CAMPAIGN_UPDATE.equals(uri) ||
				API_CAMPAIGN_DELETE.equals(uri) ||
				// Class
				API_CLASS_CREATE.equals(uri) ||
				API_CLASS_READ.equals(uri) ||
				API_CLASS_ROSTER_READ.equals(uri) ||
				API_CLASS_UPDATE.equals(uri) ||
				API_CLASS_ROSTER_UPDATE.equals(uri) ||
				API_CLASS_DELETE.equals(uri) ||
				// Document
				API_DOCUMENT_CREATE.equals(uri) ||
				API_DOCUMENT_READ.equals(uri) ||
				API_DOCUMENT_READ_CONTENTS.equals(uri) ||
				API_DOCUMENT_UPDATE.equals(uri) ||
				API_DOCUMENT_DELETE.equals(uri) ||
				// Image
				API_IMAGE_READ.equals(uri) ||
				API_MOBILITY_UPLOAD.equals(uri) ||
				// User
				API_USER_CREATE.equals(uri) ||
				API_USER_READ.equals(uri) ||
				API_USER_INFO_READ.equals(uri) ||
				API_USER_STATS_READ.equals(uri) ||
				API_USER_UPDATE.equals(uri) ||
				API_USER_CHANGE_PASSWORD.equals(uri) ||
				API_USER_DELETE.equals(uri)) {
			return true;
		}
		
		// The URI is unknown.
		return false;
	}
	
	/**
	 * Retrieves the parameter map from the request and returns it.
	 * 
	 * @param httpRequest A HttpServletRequest that contains the desired 
	 * 					  parameter map.
	 * 
	 * @return Returns a map of keys to an array of values for all of the
	 * 		   parameters contained in the request.
	 * 
	 * @throws IllegalArgumentException Thrown if the parameters cannot be 
	 * 									parsed.
	 * 
	 * @throws IllegalStateException Thrown if there is a problem connecting to
	 * 								 or reading from the request.
	 */
	private static Map<String, String[]> getParameters(HttpServletRequest httpRequest) {
		Enumeration<String> contentEncodingHeaders = httpRequest.getHeaders(KEY_CONTENT_ENCODING);
		
		while(contentEncodingHeaders.hasMoreElements()) {
			if(VALUE_GZIP.equals(contentEncodingHeaders.nextElement())) {
				return gunzipRequest(httpRequest);
			}
		}
		
		return httpRequest.getParameterMap();
	}
	
	/**
	 * Retrieves the parameter map from a request that has had its contents
	 * GZIP'd. 
	 * 
	 * @param httpRequest A HttpServletRequest whose contents are GZIP'd as
	 * 					  indicated by a "Content-Encoding" header.
	 * 
	 * @return Returns a map of keys to a list of values for all of the 
	 * 		   parameters passed to the server.
	 * 
	 * @throws IllegalArgumentException Thrown if the parameters cannot be 
	 * 									parsed.
	 * 
	 * @throws IllegalStateException Thrown if there is a problem connecting to
	 * 								 or reading from the request.
	 */
	private static Map<String, String[]> gunzipRequest(HttpServletRequest httpRequest) {
		// Retrieve the InputStream for the GZIP'd content of the request.
		InputStream inputStream;
		try {
			inputStream = new BufferedInputStream(new GZIPInputStream(httpRequest.getInputStream()));
		}
		catch(IllegalStateException e) {
			throw new IllegalStateException("The request's input stream can no longer be connected.", e);
		}
		catch(IOException e) {
			throw new IllegalStateException("Could not connect to the request's input stream.", e);
		}
		
		// Retrieve the parameter list as a string.
		String parameterString;
		try {
			// This will build the parameter string.
			StringBuilder builder = new StringBuilder();
			
			// These will store the information for the current chunk.
			byte[] chunk = new byte[CHUNK_SIZE];
			int readLen = 0;
			
			while((readLen = inputStream.read(chunk)) != -1) {
				builder.append(new String(chunk, 0, readLen));
			}
			
			parameterString = builder.toString();
		}
		catch(IOException e) {
			throw new IllegalStateException("There was an error while reading from the request's input stream.", e);
		}
		finally {
			try {
				inputStream.close();
			}
			catch(IOException e) {
				throw new IllegalStateException("And error occurred while closing the input stream.", e);
			}
		}
		
		// Create the resulting object so that, unless we fail, we will never
		// return null.
		Map<String, String[]> parameterMap = new HashMap<String, String[]>();
		
		// If the parameters string is not empty, parse it for the parameters.
		if(! StringUtils.isEmptyOrWhitespaceOnly(parameterString)) {
			Map<String, List<String>> parameters = new HashMap<String, List<String>>();
			
			// First, split all of the parameters apart.
			String[] keyValuePairs = parameterString.split(PARAMETER_SEPARATOR);
			
			// For each of the pairs, split their key and value and store them.
			for(String keyValuePair : keyValuePairs) {
				// If the pair is empty or null, ignore it.
				if(StringUtils.isEmptyOrWhitespaceOnly(keyValuePair.trim())) {
					continue;
				}
				
				// Split the key from the value.
				String[] splitPair = keyValuePair.split(PARAMETER_VALUE_SEPARATOR);
				
				// If there isn't exactly one key to one value, then there is a
				// problem, and we need to abort.
				if(splitPair.length <= 1) {
					throw new IllegalArgumentException("One of the parameter's 'pairs' did not contain a '" + PARAMETER_VALUE_SEPARATOR + "': " + keyValuePair);
				}
				else if(splitPair.length > 2) {
					throw new IllegalArgumentException("One of the parameter's 'pairs' contained multiple '" + PARAMETER_VALUE_SEPARATOR + "'s: " + keyValuePair);
				}
				
				// The key is the first part of the pair.
				String key = splitPair[0];
				
				// The first or next value for the key is the second part of 
				// the pair.
				List<String> values = parameters.get(key);
				if(values == null) {
					values = new LinkedList<String>();
					parameters.put(key, values);
				}
				values.add(StringUtils.urlDecode(splitPair[1]));
			}
			
			// Now that we have all of the pairs, convert it into the 
			// appropriate map.
			for(String key : parameters.keySet()) {
				parameterMap.put(key, parameters.get(key).toArray(new String[0]));
			}
		}
		
		return parameterMap;
	}
}