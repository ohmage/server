package org.ohmage.lib;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator;
import org.ohmage.domain.Document;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.configuration.SurveyResponse;
import org.ohmage.exception.ErrorCodeException;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.RequestBuilder;
import org.ohmage.request.auth.AuthRequest;
import org.ohmage.request.auth.AuthTokenRequest;
import org.ohmage.request.document.DocumentCreationRequest;
import org.ohmage.request.mobility.MobilityReadRequest;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

public class OhmageApi {
	private static final int HTTP_PORT = 80;
	private static final int HTTPS_PORT = 443;
	
	private static final int CHUNK_SIZE = 4096;
	
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_TYPE_HTML = "text/html";
	private static final String CONTENT_TYPE_JSON = "application/json";
		
	private final URL url;
	
	/**
	 * REMOVE BEFORE RELEASE! THIS IS FOR TESTING PURPOSES ONLY.
	 * 
	 * @param args The arguments with which to test.
	 * 
	 * @throws Exception Catch-all for debugging.
	 */
	public static void main(String args[]) throws Exception {
		String client = "library";
		
		OhmageApi api = new OhmageApi("localhost", 8080, false);
		
		String token = api.getAuthenticationToken("sink.thaw", "mill.damper", client);
		String hashedPassword = api.getHashedPassword("sink.thaw", "mill.damper", client);
		
		Collection<String> usernames = new ArrayList<String>(1);
		usernames.add("sink.thaw");
		Collection<String> emptyList = new ArrayList<String>(0);
		/*
		Collection<SurveyResponseInformation> result = 
			api.getSurveyResponsesJsonColumns(token, null, null, client, "urn:campaign:ca:lausd:Addams_HS:CS101:Fall:2011:Sleep", 
				usernames, emptyList, emptyList, null, null, null, null, null, null, null, null, null);
		
		System.out.println(new String(result));*/
	}
	
	/**
	 * Creates a new OhmageAPI object that points to a single server.
	 * 
	 * @param serverAddress The servers address. This includes only the domain
	 * 						section of the URL such as "dev.andwellness.org" or
	 * 						"dev.mobilizingcs.org".
	 * 
	 * @param port The port to use instead of the standard HTTP and HTTPS 
	 * 			   ports. To use the standard ports, make this null.
	 * 
	 * @param secure If set, HTTPS will be used; otherwise, HTTP will be used.
	 * 				 If one is used and the server sends back a HTTP 301 or 302
	 * 				 status code, the call will be redirected to the 
	 * 				 appropriate protocol.
	 * 
	 * @throws IllegalArgumentException Thrown if the server address is null or
	 * 									not a valid address.
	 */
	public OhmageApi(final String serverAddress, final Integer port, 
			final boolean secure) {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(serverAddress)) {
			throw new IllegalArgumentException("The server's address cannot be null.");
		}
		
		// Builds the server URL.
		StringBuilder serverUrlBuilder = new StringBuilder();
		
		// If we are dealing with secure connections,
		if(secure) {
			serverUrlBuilder.append("https://");
		}
		else {
			serverUrlBuilder.append("http://");
		}
		
		// Add the server's domain.
		serverUrlBuilder.append(serverAddress);
		
		// If a port was given, specify the destination.
		if((port != null) && ((port != HTTP_PORT) && (port != HTTPS_PORT))) {
			serverUrlBuilder.append(":").append(port);
		}
		
		try {
			url = new URL(serverUrlBuilder.toString());
		}
		catch(MalformedURLException e) {
			throw new IllegalArgumentException("The server's address is invalid.");
		}
	}
	
	/**************************************************************************
	 * Authentication Requests
	 *************************************************************************/

	/**
	 * Authenticates the username and password with the server and returns the
	 * user's hashed password to be used in subsequent calls.
	 * 
	 * @param username The user's username.
	 * 
	 * @param plaintextPassword The user's plaintext password.
	 * 
	 * @param client The client value.
	 * 
	 * @return The user's hashed password.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public String getHashedPassword(final String username, 
			final String plaintextPassword, final String client) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, plaintextPassword);
		parameters.put(InputKeys.CLIENT, client);

		try {
			return processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_USER_AUTH), 
							parameters, 
							false), 
					AuthRequest.KEY_HASHED_PASSWORD);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**
	 * Authenticates the username and password with the server and returns an
	 * authentication token to be used in subsequent requests.
	 * 
	 * @param username The user's username.
	 * 
	 * @param plaintextPassword The user's plaintext password.
	 * 
	 * @param client The client value.
	 * 
	 * @return An authentication token.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public String getAuthenticationToken(final String username, 
			final String plaintextPassword, final String client) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, plaintextPassword);
		parameters.put(InputKeys.CLIENT, client);
		
		try {
			return processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_USER_AUTH_TOKEN), 
							parameters, 
							false), 
					AuthTokenRequest.KEY_AUTH_TOKEN);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}

	/**************************************************************************
	 * Document Requests
	 *************************************************************************/

	/**
	 * Creates a new document.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param name The document's name including an optional extension.
	 * 
	 * @param description An optional description for the document. Set this to
	 * 					  null to omit it.
	 * 
	 * @param privacyState The document's initial privacy state.
	 * 
	 * @param document A File object referencing the document to be uploaded.
	 * 
	 * @param campaignAndDocumentRoleMap A map of campaign IDs to document 
	 * 									 roles to be sent to the server. Either
	 * 									 this or 'classAndDocumentRoleMap' must
	 * 									 be non-null and not empty.
	 * 
	 * @param classAndDocumentRoleMap A map of class IDs to document roles to 
	 * 								  be sent to the server. Either this or
	 * 								  'campaignAndDocumentRoleMap' must be
	 * 								  non-null and not empty.
	 * 
	 * @return Returns the new document's unique identifier.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public String createDocument(final String authenticationToken, 
			final String client, final String name, final String description,
			final Document.PrivacyState privacyState,
			final File document,
			final Map<String, Document.Role> campaignAndDocumentRoleMap,
			final Map<String, Document.Role> classAndDocumentRoleMap)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.DOCUMENT_NAME, name);
		parameters.put(InputKeys.PRIVACY_STATE, privacyState);
		parameters.put(InputKeys.DOCUMENT, document);
		
		if(campaignAndDocumentRoleMap != null) {
			parameters.put(
					InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST, 
					StringUtils.mapToStringList(
							campaignAndDocumentRoleMap, 
							InputKeys.ENTITY_ROLE_SEPARATOR, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classAndDocumentRoleMap != null) {
			parameters.put(
					InputKeys.DOCUMENT_CLASS_ROLE_LIST, 
					StringUtils.mapToStringList(
							classAndDocumentRoleMap, 
							InputKeys.ENTITY_ROLE_SEPARATOR, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		parameters.put(InputKeys.DESCRIPTION, description);
		
		try {
			return processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_DOCUMENT_CREATE), 
							parameters, 
							true), 
					DocumentCreationRequest.KEY_DOCUMENT_ID);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**
	 * Deletes a document.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param documentId The unique identifier for the document to be deleted.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void deleteDocument(final String authenticationToken,
			final String client, final String documentId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.DOCUMENT_ID, documentId);
		
		try {
			processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_DOCUMENT_DELETE), 
							parameters, 
							false), 
					null);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**************************************************************************
	 * Mobility Requests
	 *************************************************************************/
	
	/**
	 * Uploads a collection of Mobility points.
	 * 
	 * @param username The username of the user who is attempting the upload.
	 * 
	 * @param hashedPassword The user's hashed password.
	 * 
	 * @param client The client value.
	 * 
	 * @param points The collection of points to be uploaded.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void uploadMobilityPoints(final String username, 
			final String hashedPassword, final String client, 
			final Collection<MobilityPoint> points) 
			throws ApiException, RequestErrorException {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.CLIENT, client);
		
		JSONArray dataArray = new JSONArray();
		for(MobilityPoint point : points) {
			if(point == null) {
				continue;
			}
			
			JSONObject pointJson = point.toJson(false, true);
			if(pointJson == null) {
				throw new ApiException("One of the Mobility points could not be converted to JSON.");
			}
			
			dataArray.put(pointJson);
		}
		parameters.put(InputKeys.DATA, dataArray);
		
		try {
			processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_MOBILITY_UPLOAD), 
							parameters, 
							false), 
					null);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**
	 * Reads Mobility points.
	 * 
	 * @param authenticationToken The authentication token for the user making
	 * 							  the request. This may be null if a username
	 * 							  and password are provided.
	 * 
	 * @param username The username of the user that is making the request. 
	 * 				   This may be null if the authentication token is 
	 * 				   provided.
	 * 
	 * @param password The hashed password of the user that is making the 
	 * 				   request. This may be null if the authentication token is
	 * 				   provided.
	 * 
	 * @param client The client value.
	 * 
	 * @param date The date for which the Mobility points will be gathered.
	 * 
	 * @return A, possibly empty but never null, list of MobilityInformation
	 * 		   objects.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public List<MobilityPoint> readMobilityPoints(final String authenticationToken, 
			final String username, final String password, final String client,
			final Date date) throws ApiException, RequestErrorException {

		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if(username != null) {
			parameters.put(InputKeys.USER, username);
		}
		if(password != null) {
			parameters.put(InputKeys.PASSWORD, password);
		}
		if(authenticationToken != null) {
			parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		}
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.DATE, TimeUtils.getIso8601DateString(date));
		
		JSONArray response;
		try {
			response = new JSONArray(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_MOBILITY_READ), 
									parameters, 
									false), 
							MobilityReadRequest.JSON_KEY_DATA
						)
				);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
		catch(JSONException e) {
			throw new ApiException("The response body was not proper JSON.", e);
		}
		
		int numResults = response.length();
		List<MobilityPoint> results = new ArrayList<MobilityPoint>(numResults);
		
		for(int i = 0; i < numResults; i++) {
			JSONObject currResult = response.optJSONObject(i);
			
			// All Mobility points from the server are "mode_only", but that is
			// not included in the response. We add it here for the 
			// constructor's benefit.
			try {
				currResult.put(MobilityPoint.JSON_KEY_SUBTYPE, MobilityPoint.SubType.MODE_ONLY);
			}
			catch(JSONException e) {
				throw new ApiException("Error adding the subtype to the response.", e);
			}
			
			try {
				results.add(new MobilityPoint(currResult, MobilityPoint.PrivacyState.PRIVATE));
			}
			catch(ErrorCodeException e) {
				throw new ApiException("The server returned an malformed MobilityInformation object.", e);
			}
		}
		
		return results;
	}
	
	/**************************************************************************
	 * Survey Response Requests
	 *************************************************************************/
	
	/**
	 * Uploads a collection of survey responses.
	 * 
	 * @param username The username of the user for whom this survey response
	 * 				   belongs.
	 * 
	 * @param hashedPassword The hashsed password of the user that is creating
	 * 						 this point.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The unique identifier for the campaign for whom these
	 * 					 survey responses belong.
	 * 
	 * @param campaignCreationTimestamp The campaign's creation timestamp to
	 * 									ensure we are not uploading out-dated
	 * 									data.
	 * 
	 * @param surveyResponses The collection of survey responses to be 
	 * 						  uploaded.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void uploadSurveyResponses(final String username, 
			final String hashedPassword, final String client,
			final String campaignId, final Date campaignCreationTimestamp,
			final Collection<SurveyResponse> surveyResponses)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.CAMPAIGN_CREATION_TIMESTAMP, TimeUtils.getIso8601DateTimeString(campaignCreationTimestamp));
		
		JSONArray dataArray = new JSONArray();
		for(SurveyResponse response : surveyResponses) {
			if(response == null) {
				continue;
			}
			
			JSONObject responseJson = response.toJson(false, false, false, 
					false, true, true, true, true, true, true, false, false, 
					true, true, true, false);
			if(responseJson == null) {
				throw new ApiException("One of the survey responses could not be converted to JSON.");
			}
			
			dataArray.put(responseJson);
		}
		parameters.put(InputKeys.SURVEYS, dataArray);
		
		try {
			processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_SURVEY_UPLOAD), 
							parameters, 
							true), 
					null);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}	
	
	/**
	 * Makes a request to the server for the survey responses based on the 
	 * given parameters and returns the CSV file as a byte array.
	 * 
	 * @param authenticationToken The user's current authentication token. This
	 * 							  may be null if a username and password are 
	 * 							  given.
	 * 
	 * @param username The user's username. This may be null if an 
	 * 				   authentication token is given.
	 * 
	 * @param hashedPassword The user's hashed password. This may be null if an 
	 * 						 authentication token was given.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param usernames A collection of usernames for which the results will 
	 * 					only apply to those users.
	 * 
	 * @param columnList A collection of column IDs which will limit the 
	 * 					 results to only returning that specific type of 
	 * 					 information.
	 * 
	 * @param surveyIdList A collection of campaign-unique survey IDs which 
	 * 					   will limit the results to only those responses from
	 * 					   these surveys.
	 * 
	 * @param promptIdList A collection of campaign-unique prompt IDs which 
	 * 					   will limit the results to only those responses from
	 * 					   these prompts.
	 * 
	 * @param collapse Whether or not to collapse the results which will result
	 * 				   in duplicate rows being rolled into a single resulting
	 * 				   row.
	 * 
	 * @param startDate A date and time limiting the results to only those that
	 * 					occurred on or after this date.
	 * 
	 * @param endDate A date and time limiting the results to only those that
	 * 				  occurred on or before this date.
	 * 
	 * @param privacyState A survey response privacy state limiting the results
	 * 					   to only those with the given privacy state.
	 * 
	 * @param returnId Whether or not to return unique identifiers for the 
	 * 				   survey responses.
	 * 
	 * @return A byte array representing the CSV file.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public byte[] getSurveyResponsesCsv(
			final String authenticationToken, final String username, 
			final String hashedPassword, final String client,
			final String campaignId, final Collection<String> usernames,
			final Collection<String> columnList,
			final Collection<String> surveyIdList, 
			final Collection<String> promptIdList,
			final Boolean collapse,
			final Date startDate, final Date endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Boolean returnId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if(username != null) {
			parameters.put(InputKeys.USER, username);
		}
		if(hashedPassword != null) {
			parameters.put(InputKeys.PASSWORD, hashedPassword);
		}
		if(authenticationToken != null) {
			parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		}
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.OUTPUT_FORMAT, SurveyResponseReadRequest.OUTPUT_FORMAT_CSV);
		parameters.put(InputKeys.USER_LIST, StringUtils.collectionToStringList(usernames, InputKeys.LIST_ITEM_SEPARATOR));
		
		if(columnList != null) {
			if(columnList.size() == 0) {
				parameters.put(InputKeys.COLUMN_LIST, SurveyResponseReadRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.COLUMN_LIST, StringUtils.collectionToStringList(columnList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(surveyIdList != null) {
			if(surveyIdList.size() == 0) {
				parameters.put(InputKeys.SURVEY_ID_LIST, SurveyResponseReadRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.SURVEY_ID_LIST, StringUtils.collectionToStringList(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		if(promptIdList != null) {
			if(promptIdList.size() == 0) {
				parameters.put(InputKeys.PROMPT_ID_LIST, SurveyResponseReadRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.PROMPT_ID_LIST, StringUtils.collectionToStringList(promptIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		parameters.put(InputKeys.COLLAPSE, collapse);
		parameters.put(InputKeys.SUPPRESS_METADATA, true);
		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateTimeString(startDate));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateTimeString(endDate));
		parameters.put(InputKeys.RETURN_ID, returnId);
		
		if(privacyState != null) {
			parameters.put(InputKeys.PRIVACY_STATE, privacyState.toString());
		}
		
		byte[] response;
		try {
			response = makeRequest(
					new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_READ), 
					parameters, 
					false
				);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
		
		return response;
	}
	
	public Collection<SurveyResponse> getSurveyResponsesJsonColumns(
			final String authenticationToken, final String username, 
			final String hashedPassword, final String client,
			final String campaignId, final Collection<String> usernames,
			final Collection<String> columnList,
			final Collection<String> surveyIdList, 
			final Collection<String> promptIdList,
			final Boolean collapse,
			final Date startDate, final Date endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Boolean returnId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		
		if(username != null) {
			parameters.put(InputKeys.USER, username);
		}
		if(hashedPassword != null) {
			parameters.put(InputKeys.PASSWORD, hashedPassword);
		}
		if(authenticationToken != null) {
			parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		}
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.OUTPUT_FORMAT, "json-columns");
		parameters.put(InputKeys.USER_LIST, StringUtils.collectionToStringList(usernames, InputKeys.LIST_ITEM_SEPARATOR));
		
		if(columnList != null) {
			if(columnList.size() == 0) {
				parameters.put(InputKeys.COLUMN_LIST, SurveyResponseReadRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.COLUMN_LIST, StringUtils.collectionToStringList(columnList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(surveyIdList != null) {
			if(surveyIdList.size() == 0) {
				parameters.put(InputKeys.SURVEY_ID_LIST, SurveyResponseReadRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.SURVEY_ID_LIST, StringUtils.collectionToStringList(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		if(promptIdList != null) {
			if(promptIdList.size() == 0) {
				parameters.put(InputKeys.PROMPT_ID_LIST, SurveyResponseReadRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.PROMPT_ID_LIST, StringUtils.collectionToStringList(promptIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		parameters.put(InputKeys.COLLAPSE, collapse);
		parameters.put(InputKeys.SUPPRESS_METADATA, true);
		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateTimeString(startDate));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateTimeString(endDate));
		parameters.put(InputKeys.RETURN_ID, returnId);
		
		if(privacyState != null) {
			parameters.put(InputKeys.PRIVACY_STATE, privacyState.toString());
		}
		
		JSONObject response;
		try {
			response = new JSONObject(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_READ), 
									parameters, 
									false
								),
							SurveyResponseReadRequest.JSON_KEY_RESULT
						)
				);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
		catch(JSONException e) {
			throw new ApiException("The response was proper JSON but the data was not.", e);
		}
		
		
		return null;
		//return response;
	}
	
	/**************************************************************************
	 * User Requests
	 *************************************************************************/
	
	/**
	 * Creates a new user. The requesting user must be an admin.
	 * 
	 * @param authenticationToken The requesting user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param newUsername The username for the new user.
	 * 
	 * @param newPassword The plaintext password for the new user.
	 * 
	 * @param admin Whether or not the new user should be an admin.
	 * 
	 * @param enabled Whether or not the new user's account should be enabled.
	 * 
	 * @param newAccount Whether or not the new user must change their password
	 * 					 before they can login. This is optional and defaults 
	 * 					 to true. To omit this value, pass null.
	 * 
	 * @param canCreateCampaigns Whether or not the new user is allowed to 
	 * 							 create campaigns. This is optional and 
	 * 							 defaults to however the server is configured.
	 * 							 To omit this value, pass null.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void createUser(final String authenticationToken, 
			final String client,
			final String newUsername, final String newPassword,
			final boolean admin, final boolean enabled,
			final Boolean newAccount, final Boolean canCreateCampaigns) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.USERNAME, newUsername);
		parameters.put(InputKeys.PASSWORD, newPassword);
		parameters.put(InputKeys.USER_ADMIN, admin);
		parameters.put(InputKeys.USER_ENABLED, enabled);
		
		if(newAccount != null) {
			parameters.put(InputKeys.NEW_ACCOUNT, newAccount);
		}
		if(canCreateCampaigns != null) {
			parameters.put(InputKeys.CAMPAIGN_CREATION_PRIVILEGE, canCreateCampaigns);
		}
		
		try {
			processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_USER_CREATE),
							parameters,
							false
					),
					null
			);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**
	 * Deletes a user. The requesting user must be an admin.
	 * 
	 * @param authenticationToken The requesting user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param usernames A collection of usernames of the users to be deleted.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void deleteUser(final String authenticationToken,
			final String client, 
			final Collection<String> usernames) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(
				InputKeys.USER_LIST, 
				StringUtils.collectionToStringList(
						usernames, 
						InputKeys.LIST_ITEM_SEPARATOR
					)
			);
		
		try {
			processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_USER_DELETE),
							parameters,
							false
					),
					null
			);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**************************************************************************
	 * Private Methods
	 *************************************************************************/
	
	/**
	 * Makes a call to the URL. The call will be a GET if 'postParameters' is
	 * null and a POST if 'postParameters' is non-null, even if it is empty. If
	 * it is a POST, 'isForm' will set it to be a "multipart/form-data" 
	 * request, but if it is set to false it will default to a 
	 * "application/x-www-form-urlencoded" request.<br />
	 * <br />
	 * If the response has a Content-Type that suggests that it is JSON, it 
	 * will check if the ohmage result is success or failure and throw an 
	 * exception if it is failure.
	 * 
	 * @param url The URL which dictates the location to which the request
	 * 			  should be made. This includes the scheme, which must be an
	 * 			  HTTP scheme ("http" or "https"), a domain, an optional port,
	 * 			  a path, and an optional query string. 
	 * 
	 * @param postParameters A map of keys to values for a POST call. The 
	 * 						 values must be a byte array to facilitate sending
	 * 						 things other than text such as images. If this is
	 * 						 null, the request will be a GET request; if this
	 * 						 is non-null, even if it is empty, it will force it
	 * 						 to be a POST call.
	 * 
	 * @param isForm This flag is used if the 'postParameters' is non-null and
	 * 				 indicates whether this POST should be a 
	 * 				 "multipart/form-data" request or not.
	 * 
	 * @return Returns the result from the server as a byte array. 
	 * 
	 * @throws ApiException Thrown if the URL is not an HTTP URL or if there
	 * 						   was an error communicating with the server.
	 */
	private byte[] makeRequest(final URL url, 
			final Map<String, Object> postParameters, final boolean isForm) 
		throws ApiException, RequestErrorException {
		
		// Create a default client to use to connect with.
		HttpClient httpClient = new DefaultHttpClient();
		
		// Build the request based on the parameters.
		HttpRequestBase request;
		if(postParameters == null) {
			// This is a GET request and the parameters are encoded in the URL.
			try {
				request = new HttpGet(url.toURI());
			}
			catch(URISyntaxException e) {
				throw new ApiException("There was an error building the request.", e);
			}
		}
		else {
			// This is a POST request and the parameter list must be built.
			try {
				HttpPost postRequest = new HttpPost(url.toURI());
				request = postRequest;
				
				// Build the parameter map which is called a "HttpEntity".
				HttpEntity entity;
				// It is a "multipart/form-data" request and the parameters
				// must be assigned to the 'entity'.
				if(isForm) {
					MultipartEntity multipartEntity = new MultipartEntity();
					entity = multipartEntity;
					
					for(String key : postParameters.keySet()) {
						Object value = postParameters.get(key);
						
						// If it is a File, add it to the entity list as an 
						// attachment.
						if(value instanceof File) {
							multipartEntity.addPart(key, new FileBody((File) value));
						}
						// Otherwise, get its string value and add it as such.
						else if(value != null) {
							try {
								multipartEntity.addPart(key, new StringBody(String.valueOf(value)));
							}
							catch(UnsupportedEncodingException e) {
								throw new ApiException("The value for key '" + key + "' could not be encoded.", e);
							}
						}
					}
				}
				// It is a "application/x-www-form-urlencoded" request and the
				// parameters can each be added as a string-string pair.
				else {
					List<BasicNameValuePair> items = new ArrayList<BasicNameValuePair>(postParameters.size());
					
					for(String key : postParameters.keySet()) {
						Object value = postParameters.get(key);
						
						if(value != null) {
							items.add(new BasicNameValuePair(key, String.valueOf(value)));
						}
					}
					
					try {
						entity = new UrlEncodedFormEntity(items);
					}
					catch(UnsupportedEncodingException e) {
						throw new ApiException("The parameter list could not be properly encoded.", e);
					}
				}
				postRequest.setEntity(entity);
			}
			catch(URISyntaxException e) {
				throw new ApiException("There was an error building the request.", e);
			}
		}
		
		// Make the request and get the response.
		HttpResponse httpResponse;
		try {
			httpResponse = httpClient.execute(request);
		}
		catch(ClientProtocolException e) {
			throw new ApiException("An HTTP protocol error occurred.", e);
		}
		catch(IOException e) {
			throw new ApiException("The connection was aborted.", e);
		}
		
		// Check the status code.
		int statusCode = httpResponse.getStatusLine().getStatusCode();
		// If it is a redirect, get the new location and remake the request.
		if((statusCode == 301) || (statusCode == 302)) {
			String newLocation = httpResponse.getFirstHeader("Location").getValue();
			
			try {
				return makeRequest(new URL(newLocation), postParameters, isForm);
			}
			catch(MalformedURLException e) {
				throw new ApiException("The server returned a bad redirect address: " + newLocation, e);
			}
		}
		// Otherwise, if it is is a non-success code, fail the request.
		else if(statusCode != 200) {
			throw new ApiException("There was an error connecting to the server: " + statusCode);
		}
		
		// Retrieve the server's response as an InputStream.
		InputStream content;
		try {
			content = httpResponse.getEntity().getContent();
		}
		catch(IOException e) {
			throw new ApiException("There was an error connecting to the response from the server.", e);
		}
		
		// Read the results as a byte array. This is used instead of a string 
		// to allow the function to me more open to different types of return 
		// values such as text, images, etc.
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		byte[] chunk = new byte[CHUNK_SIZE];
		int amountRead;
		try {
			while((amountRead = content.read(chunk)) != -1) {
				baos.write(chunk, 0, amountRead);
			}
		}
		catch(IOException e) {
			throw new ApiException("There was an error reading from the server.", e);
		}
		byte[] result = baos.toByteArray();
		
		// Finally, check the Content-Type to see if it suggests that this is
		// an ohmage JSON result. If so, check if it failed and, if so, throw
		// an exception.
		Header[] headers = httpResponse.getHeaders(CONTENT_TYPE_HEADER);
		String contentType = headers[0].getValue();
		if(CONTENT_TYPE_HTML.equals(contentType) || 
				CONTENT_TYPE_JSON.equals(contentType)) {
			checkFailure(result);
		}
		
		// Return the byte array.
		return result;
	}

	/**
	 * Throws an exception if the response is valid ohmage JSON and indicates
	 * failure.
	 * 
	 * @param response The byte array response returned by the server.
	 * 
	 * @throws IllegalArgumentException Thrown if the response is valid ohmage
	 * 									JSON and indicates failure, but the 
	 * 									error code and error text are missing.
	 * 
	 * @throws RequestErrorException Thrown if the response is valid ohmage 
	 * 								 JSON and indicates failure.
	 */
	private void checkFailure(final byte[] response) 
			throws RequestErrorException {
		
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(new String(response));
		}
		catch(JSONException e) {
			// If it isn't JSON, we were mistaken and can abort.
			return;
		}
		
		try {
			// If it is flagged as failed, we need to throw an exception.
			if(Request.RESULT_FAILURE.equals(
					jsonResponse.getString(Request.JSON_KEY_RESULT))) {
				
				// Generate an ohmage-specific error.
				try {
					JSONObject error = jsonResponse.getJSONArray(
							Request.JSON_KEY_ERRORS).getJSONObject(0);
					
					String errorCode = error.getString(Annotator.JSON_KEY_CODE);
					String errorText = error.getString(Annotator.JSON_KEY_TEXT);
					
					throw new RequestErrorException(errorCode, errorText);
				}
				catch(JSONException e) {
					throw new IllegalArgumentException(
							"The failed JSON response doesn't contain a proper error object.",
							e);
				}
			}
		}
		catch(JSONException e) {
			// The result is JSON, but it isn't ohmage JSON, so we can safely
			// abort.
		}
	}
	
	/**
	 * Parses the byte array returned from the 
	 * {@link #makeRequest(URL, Map, boolean)} call, verifies that it is JSON,
	 * ensures that it was a successful call, and returns the string value
	 * associated with the given key.
	 * 
	 * @param response The byte array response as returned by 
	 * 				   {@link #makeRequest(URL, Map, boolean)}.
	 * 
	 * @param jsonKey The key string to use to get the response value from the
	 * 				  successful JSON response. If null, the response will be
	 * 				  checked for success or error and, if error, will throw an
	 * 				  exception.
	 * 
	 * @return Returns the value associated with the 'jsonKey'.
	 * 
	 * @throws IllegalArgumentException Thrown if the response cannot be 
	 * 									decoded into JSON or if there is no
	 * 									such key with the responded JSON.
	 * 
	 * @throws RequestErrorException Thrown if the server returned a valid JSON
	 * 								 response, but the request failed.
	 * 
	 * @see #makeRequest(URL, Map, boolean)
	 */
	private String processJsonResponse(final byte[] response, 
			final String jsonKey) throws RequestErrorException {
		
		JSONObject jsonResponse;
		try {
			jsonResponse = new JSONObject(new String(response));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException(
					"The response is not valid JSON.", 
					e);
		}
		
		boolean success;
		try {
			success = Request.RESULT_SUCCESS.equals(
					jsonResponse.getString(Request.JSON_KEY_RESULT));
		}
		catch(JSONException e) {
			throw new IllegalArgumentException(
					"There is no '" + 
						Request.RESULT_SUCCESS + 
						"' key in the response, but it is valid JSON. " +
						"This indicates a deeper problem with the server.", 
					e);
		}
		
		if((success) && (jsonKey != null)) {
			try {
				return jsonResponse.getString(jsonKey);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException(
						"The key '" + jsonKey + "' does not exist in the successful JSON response.", e);
			}
		}
		else if(success) {
			return null;
		}
		else {
			// Generate an ohmage-specific error.
			try {
				JSONObject error = jsonResponse.getJSONArray(
						Request.JSON_KEY_ERRORS).getJSONObject(0);
				
				String errorCode = error.getString(Annotator.JSON_KEY_CODE);
				String errorText = error.getString(Annotator.JSON_KEY_TEXT);
				
				throw new RequestErrorException(errorCode, errorText);
			}
			catch(JSONException e) {
				throw new IllegalArgumentException(
						"The failed JSON response doesn't contain a proper error object.",
						e);
			}
		}
	}
}