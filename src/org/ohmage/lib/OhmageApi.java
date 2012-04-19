/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.domain.Document;
import org.ohmage.domain.MobilityPoint;
import org.ohmage.domain.MobilityPoint.MobilityColumnKey;
import org.ohmage.domain.ServerConfig;
import org.ohmage.domain.UserInformation.UserPersonal;
import org.ohmage.domain.UserSummary;
import org.ohmage.domain.campaign.Campaign;
import org.ohmage.domain.campaign.SurveyResponse;
import org.ohmage.exception.DomainException;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.Request;
import org.ohmage.request.RequestBuilder;
import org.ohmage.request.auth.AuthRequest;
import org.ohmage.request.auth.AuthTokenRequest;
import org.ohmage.request.clazz.ClassRosterUpdateRequest;
import org.ohmage.request.document.DocumentCreationRequest;
import org.ohmage.request.mobility.MobilityReadRequest;
import org.ohmage.request.survey.SurveyResponseRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.util.TimeUtils;

/**
 * This is the main interface class for the server. 
 * 
 * @author John Jenkins
 */
public class OhmageApi {
	private static final int HTTP_PORT = 80;
	private static final int HTTPS_PORT = 443;
	
	private static final int CHUNK_SIZE = 4096;
	
	private static final String CONTENT_TYPE_HEADER = "Content-Type";
	private static final String CONTENT_TYPE_HTML = "text/html";
		
	private final URL url;
	
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
	 * Configuration Requests
	 *************************************************************************/
	
	/**
	 * Reads the server's configuration.
	 * 
	 * @return The server's configuration.
	 * 
	 * @throws ApiException Thrown if the server returns an error or if the
	 * 						server's configuration is invalid.
	 */
	public ServerConfig getServerConfiguration() throws ApiException {
		String serverResponse;
		try {
			serverResponse = 
				processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_CONFIG_READ), 
							new HashMap<String, Object>(0), 
							false), 
					InputKeys.DATA);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
		
		try {
			return new ServerConfig(new JSONObject(serverResponse));
		}
		catch(JSONException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
		catch(DomainException e) {
			throw new ApiException(
					"The response is missing a required key.", e);
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
	 * Campaign Requests
	 *************************************************************************/
	
	/**
	 * Creates a campaign.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param runningState The initial running state for this campaign.
	 * 
	 * @param privacyState The initial privacy state for this campaign.
	 *  
	 * @param classIds The IDs for the classes to associate with this campaign.
	 * 
	 * @param xml The campaign's XML.
	 * 
	 * @param description The campaign's description. Optional.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void createCampaign(final String authenticationToken, 
			final String client, final Campaign.RunningState runningState, 
			final Campaign.PrivacyState privacyState,
			final Collection<String> classIds, final String xml,
			final String description) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.RUNNING_STATE, runningState);
		parameters.put(InputKeys.PRIVACY_STATE, privacyState);
		
		if(classIds != null) {
			parameters.put(InputKeys.CLASS_URN_LIST, StringUtils.collectionToStringList(classIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		parameters.put(InputKeys.XML, xml);
		parameters.put(InputKeys.DESCRIPTION, description);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_CAMPAIGN_CREATE), 
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
	}
	
	/**
	 * Retrieves the XML for a campaign.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @return The campaign's XML as a byte array.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public byte[] getCampaignXml(final String authenticationToken,
			final String client, final String campaignId) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN_LIST, campaignId);
		parameters.put(InputKeys.OUTPUT_FORMAT, Campaign.OutputFormat.XML);
		
		try {
			return makeRequest(
					new URL(url.toString() + RequestBuilder.API_CAMPAIGN_READ), 
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
	}
	
	/**
	 * Retrieves information about all of the campaigns that satisfy the given
	 * parameters. The amount of information is determined by 'outputFormat'.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param outputFormat The 
	 * 					   {@link org.ohmage.domain.campaign.Campaign.OutputFormat}
	 * 					   that determines the amount of information returned.
	 * 
	 * @param campaignIds A collection of campaign identifiers with which to 
	 * 					  begin; however, information about these campaigns
	 * 					  might not be returned if they don't meet the other
	 * 					  criteria. If this is omitted, then all campaigns to
	 * 					  which the user is associated will be the initial 
	 * 					  list.
	 * 
	 * @param userRole A campaign role to limit the campaigns to only those
	 * 				   in which the user has the given role.
	 * 
	 * @param startDate A date to limit the campaigns to only those that were
	 * 					created on or after this date.
	 * 
	 * @param endDate A date to limit the campaigns to only those that were
	 * 				  created on or before this date.
	 * 
	 * @param privacyState A campaign privacy state to limit the results to 
	 * 					   only those that have the given privacy state.
	 * 
	 * @param runningState A campaign running state to limit the results to
	 * 					   only those that have the given running state.
	 * 
	 * @param classIds A collection of class identifiers to limit the campaigns
	 * 				   to only those that are associated with any of the 
	 * 				   classes in the list.
	 * 
	 * @return A map of campaign IDs to Campaign objects.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public Map<String, Campaign> getCampaigns(final String authenticationToken,
			final String client, final Campaign.OutputFormat outputFormat,
			final Collection<String> campaignIds, final Campaign.Role userRole,
			final DateTime startDate, final DateTime endDate,
			final Campaign.PrivacyState privacyState, 
			final Campaign.RunningState runningState,
			final Collection<String> classIds)
			throws ApiException, RequestErrorException {
		
		if(Campaign.OutputFormat.XML.equals(outputFormat)) {
			throw new IllegalArgumentException("The XML output format is not allowed for this call.");
		}
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.OUTPUT_FORMAT, outputFormat);
		parameters.put(InputKeys.USER_ROLE, userRole);
		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateString(startDate, true));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateString(endDate, true));
		parameters.put(InputKeys.PRIVACY_STATE, privacyState);
		parameters.put(InputKeys.RUNNING_STATE, runningState);
		if(campaignIds != null) {
			parameters.put(InputKeys.CAMPAIGN_URN_LIST, StringUtils.collectionToStringList(campaignIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classIds != null) {
			parameters.put(InputKeys.CLASS_URN_LIST, StringUtils.collectionToStringList(classIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		JSONObject resultJson;
		try {
			resultJson = new JSONObject(
					processJsonResponse(
						makeRequest(
								new URL(url.toString() + RequestBuilder.API_CAMPAIGN_READ), 
								parameters, 
								false), 
						Request.JSON_KEY_DATA
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
			throw new ApiException("The data was not proper JSON.", e);
		}
		
		Map<String, Campaign> result = new HashMap<String, Campaign>();
		
		Iterator<?> keys = resultJson.keys();
		while(keys.hasNext()) {
			String campaignId = (String) keys.next();
			
			try {
				result.put(
						campaignId, 
						new Campaign(
								campaignId, 
								resultJson.getJSONObject(campaignId)));
			} 
			catch(JSONException e) {
				throw new ApiException("The campaign information was not valid JSON.", e);
			}
			catch(DomainException e) {
				throw new ApiException("The campaign was malformed.", e);
			}
		}
		
		return result;
	}
	
	/**
	 * Updates a campaign.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param runningState The campaign's new running state. Optional.
	 * 
	 * @param privacyState The campaign's new privacy state. Optional.
	 * 
	 * @param xml The campaign's new XML. Optional.
	 * 
	 * @param description The campaign's new description. Optional.
	 * 
	 * @param classesToAdd A collection of classes to associate with the 
	 * 					   campaign. Optional.
	 * 
	 * @param classesToRemove A collection of classes to disassociate with the
	 * 						  campaign. Optional.
	 * 
	 * @param usersToAdd A map of usernames to a set of campaign roles to grant
	 * 					 a user a set of roles in a campaign. Optional.
	 * 
	 * @param usersToRemove A map of usernames to a set of campaign roles to
	 * 						revoke from a set of users. Optional.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void updateCampaign(final String authenticationToken,
			final String client, final String campaignId,
			final Campaign.RunningState runningState,
			final Campaign.PrivacyState privacyState,
			final String xml, final String description,
			final Collection<String> classesToAdd,
			final Collection<String> classesToRemove,
			final Map<String, Set<Campaign.Role>> usersToAdd, 
			final Map<String, Set<Campaign.Role>> usersToRemove)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.RUNNING_STATE, runningState);
		parameters.put(InputKeys.PRIVACY_STATE, privacyState);
		parameters.put(InputKeys.XML, xml);
		parameters.put(InputKeys.DESCRIPTION, description);
		
		if(classesToAdd != null) {
			parameters.put(InputKeys.CLASS_LIST_ADD, StringUtils.collectionToStringList(classesToAdd, InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classesToRemove != null) {
			parameters.put(InputKeys.CLASS_LIST_REMOVE, StringUtils.collectionToStringList(classesToRemove, InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(usersToAdd != null) {
			StringBuilder resultBuilder = new StringBuilder();
			for(String username : usersToAdd.keySet()) {
				for(Campaign.Role role : usersToAdd.get(username)) {
					resultBuilder.append(username).append(InputKeys.ENTITY_ROLE_SEPARATOR).append(role).append(InputKeys.LIST_ITEM_SEPARATOR);
				}
			}
			
			String result = resultBuilder.toString();
			while(result.endsWith(InputKeys.LIST_ITEM_SEPARATOR)) {
				result = result.substring(0, result.length() - 1);
			}
			
			parameters.put(InputKeys.USER_LIST_ADD, result);
		}
		if(usersToRemove != null) {
			StringBuilder resultBuilder = new StringBuilder();
			for(String username : usersToRemove.keySet()) {
				for(Campaign.Role role : usersToRemove.get(username)) {
					resultBuilder.append(username).append(InputKeys.ENTITY_ROLE_SEPARATOR).append(role).append(InputKeys.LIST_ITEM_SEPARATOR);
				}
			}
			
			String result = resultBuilder.toString();
			while(result.endsWith(InputKeys.LIST_ITEM_SEPARATOR)) {
				result = result.substring(0, result.length() - 1);
			}
			parameters.put(InputKeys.USER_LIST_REMOVE, result);
		}
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_CAMPAIGN_UPDATE), 
					parameters, 
					true
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
	 * Deletes a campaign.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The unique identifier for the campaign to be deleted.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void deleteCampaign(final String authenticationToken,
			final String client, final String campaignId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_CAMPAIGN_DELETE), 
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
	}

	/**************************************************************************
	 * Class Requests
	 *************************************************************************/
	
	/**
	 * Creates a new class.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param classId The new class' unique identifier.
	 * 
	 * @param className The new class' name.
	 * 
	 * @param description The new class' description.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void createClass(final String authenticationToken,
			final String client, final String classId, final String className,
			final String description) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CLASS_URN, classId);
		parameters.put(InputKeys.CLASS_NAME, className);
		parameters.put(InputKeys.DESCRIPTION, description);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_CLASS_CREATE), 
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
	}
	
	/**
	 * Retrieves the information about the requested classes.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param classIds The requested classes' unique identifiers.
	 * 
	 * @return A map of class IDs to their Clazz object.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public Map<String, Clazz> getClasses(final String authenticationToken,
			final String client, final Collection<String> classIds)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		
		if(classIds != null) {
			parameters.put(InputKeys.CLASS_URN_LIST, StringUtils.collectionToStringList(classIds, InputKeys.LIST_ITEM_SEPARATOR));
		}

		JSONObject resultJson;
		try {
			resultJson = new JSONObject(
					processJsonResponse(
						makeRequest(
								new URL(url.toString() + RequestBuilder.API_CLASS_READ), 
								parameters, 
								false), 
						Request.JSON_KEY_DATA
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
			throw new ApiException("The data was not proper JSON.", e);
		}
		
		Map<String, Clazz> result = new HashMap<String, Clazz>();
		
		Iterator<?> keys = resultJson.keys();
		while(keys.hasNext()) {
			String classId = (String) keys.next();
			
			try {
				result.put(classId, new Clazz(classId, resultJson.getJSONObject(classId)));
			} 
			catch(JSONException e) {
				throw new ApiException("The class information is not valid JSON.", e);
			} 
			catch (DomainException e) {
				throw new ApiException("The class information is not valid.", e);
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves a roster for all of the classes in question.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param classIds The collection of classes.
	 * 
	 * @return A CSV file as a byte array that is the class roster.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public byte[] getClassRoster(final String authenticationToken,
			final String client, final Collection<String> classIds)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		
		if(classIds != null) {
			parameters.put(InputKeys.CLASS_URN_LIST, StringUtils.collectionToStringList(classIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		try {
			return makeRequest(
					new URL(url.toString() + RequestBuilder.API_CLASS_ROSTER_READ), 
					parameters, 
					false);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**
	 * Updates a class with the given information.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param classId The unique identifier for the class to update.
	 * 
	 * @param newName The class' new name. Optional.
	 * 
	 * @param newDescription The class' new description. Optional.
	 * 
	 * @param usersToAdd A map of usernames to class role to add to the class.
	 * 					 Optional.
	 * 
	 * @param usersToRemove A list of classes to remove. Optional.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void updateClass(final String authenticationToken, 
			final String client, final String classId, final String newName,
			final String newDescription, 
			final Map<String, Clazz.Role> usersToAdd, 
			final Collection<String> usersToRemove)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CLASS_URN, classId);
		parameters.put(InputKeys.CLASS_NAME, newName);
		parameters.put(InputKeys.DESCRIPTION, newDescription);
		
		if(usersToAdd != null) {
			parameters.put(InputKeys.USER_ROLE_LIST_ADD, StringUtils.mapToStringList(usersToAdd, InputKeys.ENTITY_ROLE_SEPARATOR, InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(usersToRemove != null) {
			parameters.put(InputKeys.USER_LIST_REMOVE, StringUtils.collectionToStringList(usersToRemove, InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_CLASS_UPDATE), 
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
	}
	
	/**
	 * Uploads a class roster to update some classes and returns warning 
	 * messages if something happened that wasn't fatal but the user probably
	 * needs to know.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param roster The roster.
	 * 
	 * @return The warning messages from the server.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public List<String> uploadRoster(final String authenticationToken,
			final String client, final String roster)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.ROSTER, roster);
		
		JSONArray warningMessages;
		try {
			warningMessages = new JSONArray(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_CLASS_ROSTER_UPDATE), 
									parameters, 
									true
								),
							ClassRosterUpdateRequest.KEY_WARNING_MESSAGES
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
			throw new ApiException("The warning messages were not valid JSON.", e);
		}
		
		int numWarningMessages = warningMessages.length();
		List<String> result = new ArrayList<String>(numWarningMessages);
		for(int i = 0; i < numWarningMessages; i++) {
			try {
				result.add(warningMessages.getString(i));
			} 
			catch(JSONException e) {
				throw new ApiException("An error message was not a String.", e);
			}
		}
		
		return result;
	}
	
	/**
	 * Deletes a class.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client THe client value.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void deleteClass(final String authenticationToken, 
			final String client, final String classId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CLASS_URN, classId);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_CLASS_DELETE), 
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
		parameters.put(InputKeys.DESCRIPTION, description);
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
	 * Retrieves the list of documents associated with the user based on the
	 * parameters.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param includePersonalDocuments Whether or not to include documents that
	 * 								   are only directly associated with the
	 * 								   user.
	 *  
	 * @param campaignIds The campaign IDs for which to get the documents'
	 * 					  information.
	 * 
	 * @param classIds The class IDs for which to get the documents' 
	 * 				   information.
	 * 
	 * @return A, possibly empty but never null, map of document unique 
	 * 		   identifiers to Document objects that contain all of the data
	 * 		   pertaining to the document.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public Map<String, Document> getDocuments(final String authenticationToken,
			final String client, final boolean includePersonalDocuments,
			final Collection<String> campaignIds, 
			final Collection<String> classIds) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.DOCUMENT_PERSONAL_DOCUMENTS, includePersonalDocuments);
		
		if(campaignIds != null) {
			parameters.put(InputKeys.CAMPAIGN_URN_LIST, StringUtils.collectionToStringList(campaignIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classIds != null) {
			parameters.put(InputKeys.CLASS_URN_LIST, StringUtils.collectionToStringList(classIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		JSONObject documents;
		try {
			documents = new JSONObject(
					processJsonResponse(
						makeRequest(
								new URL(url.toString() + RequestBuilder.API_DOCUMENT_READ), 
								parameters, 
								false), 
						Request.JSON_KEY_DATA
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
			throw new ApiException("The data was not proper JSON.", e);
		}
		
		Map<String, Document> result = new HashMap<String, Document>(documents.length());
		
		Iterator<?> keys = documents.keys();
		while(keys.hasNext()) {
			String documentId = (String) keys.next();
			
			try {
				result.put(documentId, new Document(documentId, documents.getJSONObject(documentId)));
			}
			catch(JSONException e) {
				throw new ApiException("The document was not proper JSON: " + documentId, e);
			}
			catch(DomainException e) {
				throw new ApiException("The response is missing some information.", e);
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves the contents of the document.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return The contents of the document.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public byte[] getDocumentContents(final String authenticationToken,
			final String client, final String documentId) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.DOCUMENT_ID, documentId);
		
		try {
			return makeRequest(
					new URL(url.toString() + RequestBuilder.API_DOCUMENT_READ_CONTENTS), 
					parameters, 
					false);
		}
		catch(MalformedURLException e) {
			throw new ApiException("The URL was incorrectly created.", e);
		}
		catch(IllegalArgumentException e) {
			throw new ApiException("The response was not proper JSON.", e);
		}
	}
	
	/**
	 * Updates the information and/or contents of a document. The 
	 * authentication and document ID are required, but any of the other values
	 * may be null meaning that their values won't be updated.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @param newName The document's new name.
	 * 
	 * @param newDescription The document's new description.
	 * 
	 * @param newPrivacyState The document's new privacy state.
	 * 
	 * @param newContents The document's new contents.
	 * 
	 * @param campaignAndRolesToAdd A map of campaign IDs to document roles to
	 * 								associate with the document.
	 * 
	 * @param campaignsToRemove A list of the campaigns whose association with
	 * 							the document should be removed.
	 * 
	 * @param classAndRolesToAdd A map of class IDs to document roles to
	 * 							 associate with the document.
	 * 
	 * @param classesToRemove A list of the classes whose association with the
	 * 						  document should be removed.
	 * 
	 * @param userAndRolesToAdd A map of usernames to document roles to 
	 * 							associate with the document.
	 * 
	 * @param usersToRemove A list of usernames of users whose association with
	 * 						the document should be removed.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void updateDocument(final String authenticationToken,
			final String client, final String documentId,
			final String newName, final String newDescription,
			final Document.PrivacyState newPrivacyState, 
			final byte[] newContents,
			final Map<String, Document.Role> campaignAndRolesToAdd,
			final Collection<String> campaignsToRemove,
			final Map<String, Document.Role> classAndRolesToAdd,
			final Collection<String> classesToRemove,
			final Map<String, Document.Role> userAndRolesToAdd,
			final Collection<String> usersToRemove) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.DOCUMENT_ID, documentId);
		parameters.put(InputKeys.DOCUMENT_NAME, newName);
		parameters.put(InputKeys.DESCRIPTION, newDescription);
		parameters.put(InputKeys.PRIVACY_STATE, newPrivacyState);
		parameters.put(InputKeys.DOCUMENT, newContents);
		
		if(campaignAndRolesToAdd != null) {
			parameters.put(
					InputKeys.DOCUMENT_CAMPAIGN_ROLE_LIST, 
					StringUtils.mapToStringList(
							campaignAndRolesToAdd, 
							InputKeys.ENTITY_ROLE_SEPARATOR, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(campaignsToRemove != null) {
			parameters.put(
					InputKeys.CAMPAIGN_LIST_REMOVE, 
					StringUtils.collectionToStringList(
							campaignsToRemove, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classAndRolesToAdd != null) {
			parameters.put(
					InputKeys.DOCUMENT_CLASS_ROLE_LIST, 
					StringUtils.mapToStringList(
							classAndRolesToAdd, 
							InputKeys.ENTITY_ROLE_SEPARATOR, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classesToRemove != null) {
			parameters.put(
					InputKeys.CLASS_LIST_REMOVE, 
					StringUtils.collectionToStringList(
							classesToRemove, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(userAndRolesToAdd != null) {
			parameters.put(
					InputKeys.DOCUMENT_USER_ROLE_LIST, 
					StringUtils.mapToStringList(
							userAndRolesToAdd, 
							InputKeys.ENTITY_ROLE_SEPARATOR, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(usersToRemove != null) {
			parameters.put(
					InputKeys.USER_LIST_REMOVE, 
					StringUtils.collectionToStringList(
							usersToRemove, 
							InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		try {
			processJsonResponse(
					makeRequest(
							new URL(url.toString() + RequestBuilder.API_DOCUMENT_UPDATE), 
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
			
			JSONObject pointJson;
			try {
				pointJson = point.toJson(false, MobilityColumnKey.ALL_COLUMNS);
			}
			catch(JSONException e) {
				throw new ApiException("One of the Mobility points could not be converted to JSON.", e);
			}
			catch(DomainException e) {
				throw new ApiException("One of the Mobility points could not be converted to JSON.", e);
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
			final DateTime date) throws ApiException, RequestErrorException {

		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, password);
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		
		if(date != null) {
			parameters.put(InputKeys.DATE, TimeUtils.getIso8601DateString(date, false));
		}
		
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
				currResult.put(MobilityColumnKey.SUB_TYPE.toString(false), MobilityPoint.SubType.MODE_ONLY);
			}
			catch(JSONException e) {
				throw new ApiException("Error adding the subtype to the response.", e);
			}
			
			try {
				results.add(new MobilityPoint(currResult, MobilityPoint.PrivacyState.PRIVATE));
			}
			catch(DomainException e) {
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
			final String campaignId, final DateTime campaignCreationTimestamp,
			final Collection<SurveyResponse> surveyResponses)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.CAMPAIGN_CREATION_TIMESTAMP, TimeUtils.getIso8601DateString(campaignCreationTimestamp, true));
		
		JSONArray dataArray = new JSONArray();
		for(SurveyResponse response : surveyResponses) {
			if(response == null) {
				continue;
			}
			
			JSONObject responseJson;
			try {
				responseJson = response.toJson(false, false, false, 
						false, true, true, true, true, true, false, false, 
						true, true, true, true, false, false);
				
			}
			catch(JSONException e) {
				throw new ApiException("There was a problem building the JSON.", e);
			}
			catch(DomainException e) {
				throw new ApiException("There was a problem building the JSON.", e);
			}
			
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
	 * @param startDate A date and time limiting the results to only those that
	 * 					occurred on or after this date.
	 * 
	 * @param endDate A date and time limiting the results to only those that
	 * 				  occurred on or before this date.
	 * 
	 * @param privacyState A survey response privacy state limiting the results
	 * 					   to only those with the given privacy state.
	 * 
	 * @param collapse Whether or not to collapse the results which will result
	 * 				   in duplicate rows being rolled into a single resulting
	 * 				   row.
	 * 
	 * @param suppressMetadata Whether or not to suppress the metadata comments
	 * 						   in the header.
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
			final Collection<SurveyResponse.ColumnKey> columnList,
			final Collection<String> surveyIdList, 
			final Collection<String> promptIdList,
			final DateTime startDate, final DateTime endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Boolean collapse, final Boolean suppressMetadata,
			final Boolean returnId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.OUTPUT_FORMAT, SurveyResponse.OutputFormat.CSV);
		
		if(usernames != null) {
			if(usernames.size() == 0) {
				parameters.put(InputKeys.USER_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.USER_LIST, StringUtils.collectionToStringList(usernames, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(columnList != null) {
			if(columnList.size() == 0) {
				parameters.put(InputKeys.COLUMN_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.COLUMN_LIST, StringUtils.collectionToStringList(columnList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(surveyIdList != null) {
			if(surveyIdList.size() == 0) {
				parameters.put(InputKeys.SURVEY_ID_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.SURVEY_ID_LIST, StringUtils.collectionToStringList(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		if(promptIdList != null) {
			if(promptIdList.size() == 0) {
				parameters.put(InputKeys.PROMPT_ID_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.PROMPT_ID_LIST, StringUtils.collectionToStringList(promptIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		parameters.put(InputKeys.COLLAPSE, collapse);
		parameters.put(InputKeys.SUPPRESS_METADATA, suppressMetadata);
		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateString(startDate, true));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateString(endDate, true));
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
	
	/**
	 * Retrieves the survey response information as JSON.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param username The user's username.
	 * 
	 * @param hashedPassword The user's hashed password.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The unique identifier for the campaign to which the
	 * 					 survey responses belong.
	 * 
	 * @param usernames A collection of usernames for the users whose data is
	 * 					desired. An empty collection indicates that all users 
	 * 					are desired.
	 * 
	 * @param columnList A collection of the desired columns. See
	 * 					 {@link org.ohmage.domain.campaign.SurveyResponse.ColumnKey ColumnKey}.
	 * 					 An empty collection indicates that all columns are
	 * 					 desired.
	 * 
	 * @param surveyIdList The collection of survey IDs for which to gather 
	 * 					   survey responses.
	 * 
	 * @param promptIdList The collection of prompt IDs for which to gather
	 * 					   survey responses.
	 * 
	 * @param startDate A date indicating that only survey responses on or 
	 * 					after this date should be returned. Optional.
	 * 
	 * @param endDate A date indicating that only survey responses on or before
	 * 				  this date should be returned. Optional.
	 * 
	 * @param privacyState A survey response privacy state indicating that only
	 * 					   survey responses with this privacy state should be
	 * 					   returned. Optional.
	 * 
	 * @param collapse Whether or not to combine identical results. Optional.
	 * 
	 * @param suppressMetadata Whether or not to include the metadata. 
	 * 						   Optional.
	 * 
	 * @param returnId Whether or not to return the unique identifiers for all
	 * 				   of the survey responses.
	 * 
	 * @return A JSONObject representing the desired results. Note that this 
	 * 		   will almost certainly change in the near future to return a more
	 * 		   Java-friendly response.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public JSONObject getSurveyResponsesJsonColumns(
			final String authenticationToken, final String username, 
			final String hashedPassword, final String client,
			final String campaignId, final Collection<String> usernames,
			final Collection<SurveyResponse.ColumnKey> columnList,
			final Collection<String> surveyIdList, 
			final Collection<String> promptIdList,
			final DateTime startDate, final DateTime endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Boolean collapse, final Boolean suppressMetadata,
			final Boolean returnId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.OUTPUT_FORMAT, SurveyResponse.OutputFormat.JSON_COLUMNS);
		
		if(usernames != null) {
			if(usernames.size() == 0) {
				parameters.put(InputKeys.USER_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.USER_LIST, StringUtils.collectionToStringList(usernames, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(columnList != null) {
			if(columnList.size() == 0) {
				parameters.put(InputKeys.COLUMN_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.COLUMN_LIST, StringUtils.collectionToStringList(columnList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(surveyIdList != null) {
			if(surveyIdList.size() == 0) {
				parameters.put(InputKeys.SURVEY_ID_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.SURVEY_ID_LIST, StringUtils.collectionToStringList(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		if(promptIdList != null) {
			if(promptIdList.size() == 0) {
				parameters.put(InputKeys.PROMPT_ID_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.PROMPT_ID_LIST, StringUtils.collectionToStringList(promptIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}

		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateString(startDate, true));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateString(endDate, true));
		parameters.put(InputKeys.PRIVACY_STATE, privacyState);
		parameters.put(InputKeys.COLLAPSE, collapse);
		parameters.put(InputKeys.SUPPRESS_METADATA, suppressMetadata);
		parameters.put(InputKeys.RETURN_ID, returnId);
		
		JSONObject response;
		try {
			response = new JSONObject(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_READ), 
									parameters, 
									false
								),
							InputKeys.DATA
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
		
		return response;
		// TODO: We need to convert this from a JSONObject into some Java 
		// object.
	}
	
	/**
	 * Retrieves the survey response information as JSON.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param username The user's username.
	 * 
	 * @param hashedPassword The user's hashed password.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The unique identifier for the campaign to which the
	 * 					 survey responses belong.
	 * 
	 * @param usernames A collection of usernames for the users whose data is
	 * 					desired. An empty collection indicates that all users 
	 * 					are desired.
	 * 
	 * @param columnList A collection of the desired columns. See
	 * 					 {@link org.ohmage.domain.campaign.SurveyResponse.ColumnKey ColumnKey}.
	 * 					 An empty collection indicates that all columns are
	 * 					 desired.
	 * 
	 * @param surveyIdList The collection of survey IDs for which to gather 
	 * 					   survey responses.
	 * 
	 * @param promptIdList The collection of prompt IDs for which to gather
	 * 					   survey responses.
	 * 
	 * @param startDate A date indicating that only survey responses on or 
	 * 					after this date should be returned. Optional.
	 * 
	 * @param endDate A date indicating that only survey responses on or before
	 * 				  this date should be returned. Optional.
	 * 
	 * @param privacyState A survey response privacy state indicating that only
	 * 					   survey responses with this privacy state should be
	 * 					   returned. Optional.
	 * 
	 * @param collapse Whether or not to combine identical results. Optional.
	 * 
	 * @param suppressMetadata Whether or not to include the metadata. 
	 * 						   Optional.
	 * 
	 * @param returnId Whether or not to return the unique identifiers for all
	 * 				   of the survey responses.
	 * 
	 * @return A JSONArray representing the desired results. Note that this 
	 * 		   will almost certainly change in the near future to return a more
	 * 		   Java-friendly response.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public JSONArray getSurveyResponsesJsonRows(
			final String authenticationToken, final String username, 
			final String hashedPassword, final String client,
			final String campaignId, final Collection<String> usernames,
			final Collection<SurveyResponse.ColumnKey> columnList,
			final Collection<String> surveyIdList, 
			final Collection<String> promptIdList,
			final DateTime startDate, final DateTime endDate, 
			final SurveyResponse.PrivacyState privacyState,
			final Boolean collapse, final Boolean suppressMetadata,
			final Boolean returnId)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.OUTPUT_FORMAT, SurveyResponse.OutputFormat.JSON_ROWS);
		
		if(usernames != null) {
			if(usernames.size() == 0) {
				parameters.put(InputKeys.USER_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.USER_LIST, StringUtils.collectionToStringList(usernames, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(columnList != null) {
			if(columnList.size() == 0) {
				parameters.put(InputKeys.COLUMN_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.COLUMN_LIST, StringUtils.collectionToStringList(columnList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		
		if(surveyIdList != null) {
			if(surveyIdList.size() == 0) {
				parameters.put(InputKeys.SURVEY_ID_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.SURVEY_ID_LIST, StringUtils.collectionToStringList(surveyIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}
		if(promptIdList != null) {
			if(promptIdList.size() == 0) {
				parameters.put(InputKeys.PROMPT_ID_LIST, SurveyResponseRequest.URN_SPECIAL_ALL);
			}
			else {
				parameters.put(InputKeys.PROMPT_ID_LIST, StringUtils.collectionToStringList(promptIdList, InputKeys.LIST_ITEM_SEPARATOR));
			}
		}

		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateString(startDate, true));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateString(endDate, true));
		parameters.put(InputKeys.PRIVACY_STATE, privacyState);
		parameters.put(InputKeys.COLLAPSE, collapse);
		parameters.put(InputKeys.SUPPRESS_METADATA, suppressMetadata);
		parameters.put(InputKeys.RETURN_ID, returnId);
		
		JSONArray response;
		try {
			response = new JSONArray(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_READ), 
									parameters, 
									false
								),
							InputKeys.DATA
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
		
		return response;
		// TODO: We need to convert this from a JSONArray into some Java 
		// object.
	}
	
	/**
	 * Retrieves the privacy states for all of the survey responses and the
	 * count of each of those privacy states.
	 * 
	 * @param username The username of the user who is asking.
	 * 
	 * @param hashedPassword The hashed password of the user who is asking.
	 * 
	 * @param authenticationToken The authentication token of the user who is
	 * 							  asking.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param startDate A date indicating that all survey responses examined
	 * 					must have been made on or after this date.
	 *  
	 * @param endDate A date indicating that all survey responses examined must
	 * 				  have been made on or before this date.
	 * 
	 * @param ownerUsername Limits the privacy states to only those of the
	 * 						survey responses of the given user. Optional.
	 * 
	 * @return A map of survey response privacy states to their counts.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 * 
	 * FIXME: This has been changed.
	public Map<SurveyResponse.PrivacyState, Integer> getSurveyResponsePrivacyStateCounts(
			final String username, final String hashedPassword, 
			final String authenticationToken, final String client,
			final String campaignId, 
			final Date startDate, final Date endDate,
			final String ownerUsername)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, hashedPassword);
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.SURVEY_RESPONSE_OWNER, ownerUsername);
		parameters.put(InputKeys.START_DATE, TimeUtils.getIso8601DateTimeString(startDate));
		parameters.put(InputKeys.END_DATE, TimeUtils.getIso8601DateTimeString(endDate));
		parameters.put(InputKeys.SURVEY_FUNCTION_ID, SurveyResponse.Function.STATS);
		
		JSONObject response;
		try {
			response = new JSONObject(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_FUNCTION_READ), 
									parameters, 
									false
								),
							InputKeys.DATA
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
		
		Map<SurveyResponse.PrivacyState, Integer> result = 
			new HashMap<SurveyResponse.PrivacyState, Integer>(response.length());
		
		Iterator<?> keys = response.keys();
		while(keys.hasNext()) {
			String privacyStateString = (String) keys.next();
			
			try {
				SurveyResponse.PrivacyState currPrivacyState =
					SurveyResponse.PrivacyState.getValue(privacyStateString);
				
				result.put(currPrivacyState, response.getInt(privacyStateString));
			}
			catch(IllegalArgumentException e) {
				throw new ApiException("The server returned an unknown survey response privacy state.", e);
			}
			catch(JSONException e) {
				throw new ApiException("The server returned not a number for a survey response privacy state's count.", e);
			}
		}
		
		return result;
	}*/
	
	/**
	 * Updates a survey response.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param surveyId The survey response's unique identifier.
	 * 
	 * @param newPrivacyState The survey response's new privacy state.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void updateSurveyResponse(final String authenticationToken,
			final String client, final String campaignId, 
			final String surveyId, 
			final SurveyResponse.PrivacyState newPrivacyState)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.SURVEY_ID, surveyId);
		parameters.put(InputKeys.PRIVACY_STATE, newPrivacyState);

		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_UPDATE),
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
	}
	
	/**
	 * Deletes a survey response.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param campaignId The campaign's unique identifier.
	 * 
	 * @param surveyId The survey response's unique identifier.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void deleteSurveyResponse(final String authenticationToken,
			final String client, final String campaignId, 
			final String surveyId) throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.CAMPAIGN_URN, campaignId);
		parameters.put(InputKeys.SURVEY_ID, surveyId);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_SURVEY_RESPONSE_DELETE),
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
	 * 					 to true. Optional.
	 * 
	 * @param canCreateCampaigns Whether or not the new user is allowed to 
	 * 							 create campaigns. This is optional and 
	 * 							 defaults to however the server is configured.
	 * 							 Optional.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void createUser(final String authenticationToken, 
			final String client,
			final String newUsername, final String newPassword,
			final Boolean admin, final Boolean enabled,
			final Boolean newAccount, final Boolean canCreateCampaigns) 
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.USERNAME, newUsername);
		parameters.put(InputKeys.PASSWORD, newPassword);
		parameters.put(InputKeys.USER_ADMIN, admin);
		parameters.put(InputKeys.USER_ENABLED, enabled);
		parameters.put(InputKeys.NEW_ACCOUNT, newAccount);
		parameters.put(InputKeys.CAMPAIGN_CREATION_PRIVILEGE, canCreateCampaigns);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_USER_CREATE),
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
	}
	
	/**
	 * Retrieves the personal information about each of the users in the list
	 * and all of the given classes and campaigns.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @param usernames The collection of usernames.
	 * 
	 * @param campaignIds A collection of campaign IDs whose users' personal
	 * 					  information is desired.
	 * 
	 * @param classIds A collection of class DIs whose users' personal 
	 * 				   information is desired.
	 * 
	 * @return A, possibly empty but never null, map of usernames to 
	 * 		   UserPersonal information for the user or null if that user 
	 * 		   doesn't have personal information.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public Map<String, UserPersonal> getUsersPersonalInformation(
			final String authenticationToken, final String client,
			final Collection<String> usernames,
			final Collection<String> campaignIds, 
			final Collection<String> classIds)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		
		if(campaignIds != null) {
			parameters.put(InputKeys.CAMPAIGN_URN_LIST, StringUtils.collectionToStringList(campaignIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		if(classIds != null) {
			parameters.put(InputKeys.CLASS_URN_LIST, StringUtils.collectionToStringList(classIds, InputKeys.LIST_ITEM_SEPARATOR));
		}
		
		JSONObject response;
		try {
			response = new JSONObject(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_USER_READ), 
									parameters, 
									false
								),
							InputKeys.DATA
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
		
		Map<String, UserPersonal> result = new HashMap<String, UserPersonal>(response.length());
		
		Iterator<?> keys = response.keys();
		while(keys.hasNext()) {
			String username = (String) keys.next();
			
			try {
				JSONObject information = response.getJSONObject(username);
			
				if(information.length() == 0) {
					result.put(username, null);
				}
				else {
					try {
						result.put(username, new UserPersonal(information));
					}
					catch(DomainException e) {
						throw new ApiException("The response is missing some information.", e);
					}
				}
			} 
			catch(JSONException e) {
				throw new ApiException("The user personal information was not well-formed JSON.");
			}
		}
		
		return result;
	}
	
	/**
	 * Retrieves the user information about the currently logged in user.
	 * 
	 * @param authenticationToken The user's authentication token.
	 * 
	 * @param client The client value.
	 * 
	 * @return A UserSummary object about this user.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public UserSummary getUserInformation(final String authenticationToken,
			final String client) throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		
		JSONObject response;
		try {
			response = new JSONObject(
					processJsonResponse(
							makeRequest(
									new URL(url.toString() + RequestBuilder.API_USER_INFO_READ), 
									parameters, 
									false
								),
							InputKeys.DATA
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

		if(response.length() == 0) {
			return null;
		}
		else if(response.length() > 1) {
			throw new ApiException("Multiple user's information was returned.");
		}

		try {
			return new UserSummary(response.getJSONObject((String) response.keys().next()));
		} 
		catch(JSONException e) {
			throw new ApiException("The user's information was not well-formed JSON.", e);
		}
		catch(DomainException e) {
			throw new ApiException("The user's information is missing some information.", e);
		}
	}
	
	/**
	 * Updates a user's information.
	 * 
	 * @param authenticationToken The user's authentication token. Required.
	 * 
	 * @param client The client value. Required.
	 * 
	 * @param username The username of the user to be updated. Required.
	 * 
	 * @param admin Whether or not the user should be an admin. Optional.
	 * 
	 * @param enabled Whether or not the user's account should be enabled.
	 * 				  Optional.
	 * 
	 * @param newAccount Whether or not the user should be forced to change 
	 * 					 their password the next time they login. Optional.
	 * 
	 * @param campaignCreationPrivilege Whether or not the usr is allowed to
	 * 									create campaigns. Optional.
	 * 
	 * @param firstName The user's new first name. Optional.
	 * 
	 * @param lastName The user's new last name. Optional.
	 * 
	 * @param organization The user's new organization. Optional.
	 * 
	 * @param personalId The user's new personal identifier. Optional.
	 * 
	 * @param emailAddress The user's email address. Optional.
	 * 
	 * @param jsonData The user's new JSON data. It is advised to get the old
	 * 				   JSON data and update accordingly. Optional.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void updateUser(final String authenticationToken, 
			final String client, final String username, 
			final Boolean admin, final Boolean enabled, 
			final Boolean newAccount, final Boolean campaignCreationPrivilege,
			final String firstName, final String lastName,
			final String organization, final String personalId,
			final String emailAddress)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.AUTH_TOKEN, authenticationToken);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.USERNAME, username);
		parameters.put(InputKeys.USER_ADMIN, admin);
		parameters.put(InputKeys.USER_ENABLED, enabled);
		parameters.put(InputKeys.NEW_ACCOUNT, newAccount);
		parameters.put(InputKeys.CAMPAIGN_CREATION_PRIVILEGE, campaignCreationPrivilege);
		parameters.put(InputKeys.FIRST_NAME, firstName);
		parameters.put(InputKeys.LAST_NAME, lastName);
		parameters.put(InputKeys.ORGANIZATION, organization);
		parameters.put(InputKeys.PERSONAL_ID, personalId);
		parameters.put(InputKeys.EMAIL_ADDRESS, emailAddress);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_USER_UPDATE), 
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
	}
	
	/**
	 * Changes the user's password.
	 * 
	 * @param username The user's username.
	 * 
	 * @param password The user's current, plaintext password.
	 * 
	 * @param client The client value.
	 * 
	 * @param newPassword The user's new password.
	 * 
	 * @throws ApiException Thrown if there is a library error.
	 * 
	 * @throws RequestErrorException Thrown if the server returns an error.
	 */
	public void changePassword(final String username, final String password,
			final String client, final String newPassword)
			throws ApiException, RequestErrorException {
		
		Map<String, Object> parameters = new HashMap<String, Object>();
		parameters.put(InputKeys.USER, username);
		parameters.put(InputKeys.PASSWORD, password);
		parameters.put(InputKeys.CLIENT, client);
		parameters.put(InputKeys.NEW_PASSWORD, newPassword);
		
		try {
			makeRequest(
					new URL(url.toString() + RequestBuilder.API_USER_CHANGE_PASSWORD), 
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
		if(usernames != null) {
			parameters.put(
					InputKeys.USER_LIST, 
					StringUtils.collectionToStringList(
							usernames, 
							InputKeys.LIST_ITEM_SEPARATOR
						)
				);
		}
		
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
		if(CONTENT_TYPE_HTML.equals(contentType)) {
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
	 * @throws ApiException Thrown if the error code is unknown.
	 * 
	 * @throws RequestErrorException Thrown if the response is valid ohmage 
	 * 								 JSON and indicates failure.
	 */
	private void checkFailure(final byte[] response) 
			throws ApiException, RequestErrorException {
		
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
					
					String errorCodeString = 
						error.getString(Annotator.JSON_KEY_CODE);
					ErrorCode errorCode;
					try {
						errorCode = ErrorCode.getValue(errorCodeString);
					}
					catch(IllegalArgumentException e) {
						throw new ApiException("The error code was unknown.", e);
					}
					
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
	 * @throws ApiException Thrown if the error code is unknown.
	 * 
	 * @throws RequestErrorException Thrown if the server returned a valid JSON
	 * 								 response, but the request failed.
	 * 
	 * @see #makeRequest(URL, Map, boolean)
	 */
	private String processJsonResponse(final byte[] response, 
			final String jsonKey) throws ApiException, RequestErrorException {
		
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
				
				String errorCodeString = 
					error.getString(Annotator.JSON_KEY_CODE);
				ErrorCode errorCode;
				try {
					errorCode = ErrorCode.getValue(errorCodeString);
				}
				catch(IllegalArgumentException e) {
					throw new ApiException("The error code was unknown.", e);
				}
				
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
