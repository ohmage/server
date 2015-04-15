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
package org.ohmage.request;

import java.io.IOException;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.request.audio.AudioReadRequest;
import org.ohmage.request.audit.AuditReadRequest;
import org.ohmage.request.auth.AuthRequest;
import org.ohmage.request.auth.AuthTokenLogoutRequest;
import org.ohmage.request.auth.AuthTokenRequest;
import org.ohmage.request.auth.AuthTokenWhoAmIRequest;
import org.ohmage.request.campaign.CampaignAssignmentRequest;
import org.ohmage.request.campaign.CampaignCreationRequest;
import org.ohmage.request.campaign.CampaignDeletionRequest;
import org.ohmage.request.campaign.CampaignReadRequest;
import org.ohmage.request.campaign.CampaignSearchRequest;
import org.ohmage.request.campaign.CampaignUpdateRequest;
import org.ohmage.request.clazz.ClassCreationRequest;
import org.ohmage.request.clazz.ClassDeletionRequest;
import org.ohmage.request.clazz.ClassReadRequest;
import org.ohmage.request.clazz.ClassRosterReadRequest;
import org.ohmage.request.clazz.ClassRosterUpdateRequest;
import org.ohmage.request.clazz.ClassSearchRequest;
import org.ohmage.request.clazz.ClassUpdateRequest;
import org.ohmage.request.document.DocumentCreationRequest;
import org.ohmage.request.document.DocumentDeletionRequest;
import org.ohmage.request.document.DocumentReadContentsRequest;
import org.ohmage.request.document.DocumentReadRequest;
import org.ohmage.request.document.DocumentUpdateRequest;
import org.ohmage.request.image.ImageBatchZipReadRequest;
import org.ohmage.request.image.ImageReadRequest;
import org.ohmage.request.media.MediaReadRequest;
import org.ohmage.request.mobility.MobilityAggregateReadRequest;
import org.ohmage.request.mobility.MobilityDatesReadRequest;
import org.ohmage.request.mobility.MobilityReadChunkedRequest;
import org.ohmage.request.mobility.MobilityReadCsvRequest;
import org.ohmage.request.mobility.MobilityReadRequest;
import org.ohmage.request.mobility.MobilityUpdateRequest;
import org.ohmage.request.mobility.MobilityUploadRequest;
import org.ohmage.request.observer.ObserverCreationRequest;
import org.ohmage.request.observer.ObserverReadRequest;
import org.ohmage.request.observer.ObserverUpdateRequest;
import org.ohmage.request.observer.StreamReadInvalidRequest;
import org.ohmage.request.observer.StreamReadRequest;
import org.ohmage.request.observer.StreamUploadRequest;
import org.ohmage.request.omh.OmhAuthenticateRequest;
import org.ohmage.request.omh.OmhCatalogRequest;
import org.ohmage.request.omh.OmhReadRequest;
import org.ohmage.request.omh.OmhRegistryCreateRequest;
import org.ohmage.request.omh.OmhRegistryReadRequest;
import org.ohmage.request.omh.OmhRegistryUpdateRequest;
import org.ohmage.request.omh.OmhWriteRequest;
import org.ohmage.request.registration.RegistrationReadRequest;
import org.ohmage.request.survey.SurveyResponseDeleteRequest;
import org.ohmage.request.survey.SurveyResponseFunctionReadRequest;
import org.ohmage.request.survey.SurveyResponseReadRequest;
import org.ohmage.request.survey.SurveyResponseUpdateRequest;
import org.ohmage.request.survey.SurveyUploadRequest;
import org.ohmage.request.survey.annotation.AnnotationDeleteRequest;
import org.ohmage.request.survey.annotation.AnnotationUpdateRequest;
import org.ohmage.request.survey.annotation.PromptResponseAnnotationCreationRequest;
import org.ohmage.request.survey.annotation.PromptResponseAnnotationReadRequest;
import org.ohmage.request.survey.annotation.SurveyResponseAnnotationCreationRequest;
import org.ohmage.request.survey.annotation.SurveyResponseAnnotationReadRequest;
import org.ohmage.request.user.UserActivationRequest;
import org.ohmage.request.user.UserChangePasswordRequest;
import org.ohmage.request.user.UserCreationRequest;
import org.ohmage.request.user.UserDeletionRequest;
import org.ohmage.request.user.UserInfoReadRequest;
import org.ohmage.request.user.UserPasswordResetRequest;
import org.ohmage.request.user.UserReadRequest;
import org.ohmage.request.user.UserRegistrationRequest;
import org.ohmage.request.user.UserSearchRequest;
import org.ohmage.request.user.UserStatsReadRequest;
import org.ohmage.request.user.UserUpdateRequest;
import org.ohmage.request.video.VideoReadRequest;
import org.ohmage.request.visualization.VizPromptDistributionRequest;
import org.ohmage.request.visualization.VizPromptTimeseriesRequest;
import org.ohmage.request.visualization.VizScatterPlotRequest;
import org.ohmage.request.visualization.VizSurveyResponseCountRequest;
import org.ohmage.request.visualization.VizSurveyResponsePrivacyStateRequest;
import org.ohmage.request.visualization.VizSurveyResponsePrivacyStateTimeseriesRequest;
import org.ohmage.request.visualization.VizTwoDDensityRequest;
import org.ohmage.request.visualization.VizUserTimeseriesRequest;
import org.springframework.web.context.ServletContextAware;

/**
 * Request builder from an HTTP request.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class RequestBuilder implements ServletContextAware {
	private static final Logger LOGGER = 
		Logger.getLogger(RequestBuilder.class);
	
	// Root
	private String apiRoot;
	
	// Annotation
	private String apiAnnotationPromptResponseCreate;
	private String apiAnnotationPromptResponseRead;
	private String apiAnnotationSurveyResponseCreate;
	private String apiAnnotationSurveyResponseRead;
	private String apiAnnotationUpdate;
	private String apiAnnotationDelete;
	
	// Audio
	private String apiAudioRead;
	
	// Audit
	private String apiAuditRead;
	
	// Authentication
	private String apiUserAuth;
	private String apiUserAuthToken;
	private String apiUserLogout;
	private String apiUserWhoAmI;
	
	// Campaign
	private String apiCampaignAssignment;
	private String apiCampaignCreate;
	private String apiCampaignRead;
	private String apiCampaignSearch;
	private String apiCampaignUpdate;
	private String apiCampaignDelete;
	
	// Class
	private String apiClassCreate;
	private String apiClassRead;
	private String apiClassRosterRead;
	private String apiClassSearch;
	private String apiClassUpdate;
	private String apiClassRosterUpdate;
	private String apiClassDelete;
	
	// Config
	private String apiConfigRead;
	
	// Document
	private String apiDocumentCreate;
	private String apiDocumentRead;
	private String apiDocumentReadContents;
	private String apiDocumentUpdate;
	private String apiDocumentDelete;

	// Image
	private String apiImageRead;
	private String apiImageBatchZipRead;
	
	//HT: Media? 
	private String apiMediaRead;
	private String apiMediaDocumentRead; 
	private String apiMediaAudioRead;
	private String apiMediaVideoRead;
	
	// Mobility
	private String apiMobilityUpload;
	private String apiMobilityRead;
	private String apiMobilityReadChunked;
	private String apiMobilityAggregateRead;
	private String apiMobilityDatesRead;
	private String apiMobilityReadCsv;
	private String apiMobilityUpdate;
	
	// Observer
	private String apiObserverCreate;
	private String apiObserverRead;
	private String apiObserverReadXml;
	private String apiObserverUpdate;
	private String apiStreamUpload;
	private String apiStreamRead;
	private String apiStreamInvalidRead;
	
	// OMH
	private String apiOmhAuth;
	private String apiOmhRegistryCreate;
	private String apiOmhRegistryRead;
	private String apiOmhRegistryUpdate;
	private String apiOmhCatalog;
	private String apiOmhRead;
	private String apiOmhWrite;
	
	// Survey
	private String apiSurveyUpload;
	private String apiSurveyResponseDelete;
	private String apiSurveyResponseRead;
	private String apiSurveyResponseUpdate;
	private String apiSurveyResponseFunctionRead;
	
	// User
	private String apiUserCreate;
	private String apiUserRegister;
	private String apiUserActivate;
	private String apiUserPasswordReset;
	private String apiUserRead;
	private String apiUserInfoRead;
	private String apiUserStatsRead;
	private String apiUserSearch;
	private String apiUserUpdate;
	private String apiUserChangePassword;
	private String apiUserDelete;
	
	// Registration
	private String apiRegistrationRead;
	
	// Video
	private String apiVideoRead;
	
	// Visualization
	private String apiVisualization;
	private String apiVisualizationSurveyResponseCount;
	private String apiVisualizationPromptDistribution;
	private String apiVisualizationPromptTimeseries;
	private String apiVisualizationUserTimeseries;
	private String apiVisualizationScatterPlot;
	private String apiVisualization2dDensity;
	private String apiVisualizationSurveyResponsePrivacy;
	private String apiVisualizationSurveyResponsePrivacyTimeseries;
	
	private static RequestBuilder singleton;

	/**
	 * Default constructor. Made private because Spring uses reflection to 
	 * instantiate classes. The {@link #getInstance()} should be used to get a
	 * reference to this object.
	 */
	public RequestBuilder() {}
	
	/**
	 * Returns the single reference to this class.
	 * 
	 * @return The single reference to this class.
	 */
	public static RequestBuilder getInstance() {
		return singleton;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
	 */
	@Override
	public void setServletContext(final ServletContext servletContext) {
		singleton = this;
		
		apiRoot = servletContext.getContextPath();
		
		// Annotation
		apiAnnotationPromptResponseCreate = apiRoot + "/annotation/prompt_response/create";
		apiAnnotationPromptResponseRead = apiRoot + "/annotation/prompt_response/read";
		apiAnnotationSurveyResponseCreate = apiRoot + "/annotation/survey_response/create";
		apiAnnotationSurveyResponseRead = apiRoot + "/annotation/survey_response/read";
		apiAnnotationUpdate = apiRoot + "/annotation/update";
		apiAnnotationDelete = apiRoot + "/annotation/delete";
		
		// Audio
		apiAudioRead = apiRoot + "/audio/read";
		
		// Audit
		apiAuditRead = apiRoot + "/audit/read";
		
		// Authentication
		apiUserAuth = apiRoot + "/user/auth";
		apiUserAuthToken = apiRoot + "/user/auth_token";
		apiUserLogout = apiRoot + "/user/logout";
		apiUserWhoAmI = apiRoot + "/user/whoami";
		
		// Campaign
		apiCampaignAssignment = apiRoot + "/campaign/assign";
		apiCampaignCreate = apiRoot + "/campaign/create";
		apiCampaignRead = apiRoot + "/campaign/read";
		apiCampaignSearch = apiRoot + "/campaign/search";
		apiCampaignUpdate = apiRoot + "/campaign/update";
		apiCampaignDelete = apiRoot + "/campaign/delete";
		
		// Class
		apiClassCreate = apiRoot + "/class/create";
		apiClassRead = apiRoot + "/class/read";
		apiClassRosterRead = apiRoot + "/class/roster/read";
		apiClassSearch = apiRoot + "/class/search";
		apiClassUpdate = apiRoot + "/class/update";
		apiClassRosterUpdate = apiRoot + "/class/roster/update";
		apiClassDelete = apiRoot + "/class/delete";
		
		// Config
		apiConfigRead = apiRoot + "/config/read";
		
		// Document
		apiDocumentCreate = apiRoot + "/document/create";
		apiDocumentRead = apiRoot + "/document/read";
		apiDocumentReadContents = apiRoot + "/document/read/contents";
		apiDocumentUpdate = apiRoot + "/document/update";
		apiDocumentDelete = apiRoot + "/document/delete";

		// Image
		apiImageRead = apiRoot + "/image/read";
		apiImageBatchZipRead = apiRoot + "/image/batch/zip/read";
		
		// HT: Media??
		apiMediaRead = apiRoot + "/media/read";
		apiMediaDocumentRead = apiRoot + "/media/document/read";
		apiMediaAudioRead = apiRoot + "/media/audio/read";
		apiMediaVideoRead = apiRoot + "/media/video/read";
		
		// Mobility
		apiMobilityUpload = apiRoot + "/mobility/upload";
		apiMobilityRead = apiRoot + "/mobility/read";
		apiMobilityReadChunked = apiRoot + "/mobility/read/chunked";
		apiMobilityAggregateRead = apiRoot + "/mobility/aggregate/read";
		apiMobilityDatesRead = apiRoot + "/mobility/dates/read";
		apiMobilityReadCsv = apiRoot + "/mobility/read/csv";
		apiMobilityUpdate = apiRoot + "/mobility/update";
		
		// Observer
		apiObserverCreate = apiRoot + "/observer/create";
		apiObserverRead = apiRoot + "/observer/read";
		apiObserverReadXml = apiRoot + "/observer/read/xml";
		apiObserverUpdate = apiRoot + "/observer/update";
		apiStreamUpload = apiRoot + "/stream/upload";
		apiStreamRead = apiRoot + "/stream/read";
		apiStreamInvalidRead = apiRoot + "/stream/invalid/read";
		
		// OMH
		apiOmhAuth = apiRoot + "/omh/v1.0/authenticate";
		apiOmhRegistryCreate = apiRoot + "/omh/v1.0/registry/create";
		apiOmhRegistryRead = apiRoot + "/omh/v1.0/registry/read";
		apiOmhRegistryUpdate = apiRoot + "/omh/v1.0/registry/update";
		apiOmhCatalog = apiRoot + "/omh/v1.0/catalog";
		apiOmhRead = apiRoot + "/omh/v1.0/read";
		apiOmhWrite = apiRoot + "/omh/v1.0/write";
		
		// Survey
		apiSurveyUpload = apiRoot + "/survey/upload";
		apiSurveyResponseDelete = apiRoot + "/survey_response/delete";
		apiSurveyResponseRead = apiRoot + "/survey_response/read";
		apiSurveyResponseUpdate = apiRoot + "/survey_response/update";
		apiSurveyResponseFunctionRead = apiRoot + "/survey_response/function/read";
		
		// User
		apiUserCreate = apiRoot + "/user/create";
		apiUserRegister = apiRoot + "/user/register";
		apiUserActivate = apiRoot + "/user/activate";
		apiUserPasswordReset = apiRoot + "/user/reset_password";
		apiUserRead = apiRoot + "/user/read";
		apiUserInfoRead = apiRoot + "/user_info/read";
		apiUserStatsRead = apiRoot + "/user_stats/read";
		apiUserSearch = apiRoot + "/user/search";
		apiUserUpdate = apiRoot + "/user/update";
		apiUserChangePassword = apiRoot + "/user/change_password";
		apiUserDelete = apiRoot + "/user/delete";

		// Registration
		apiRegistrationRead = apiRoot + "/registration/read";

		// Video
		apiVideoRead = apiRoot + "/video/read";

		// Visualization
		apiVisualization = apiRoot + "/viz";
		apiVisualizationSurveyResponseCount = apiVisualization + "/survey_response_count/read";
		apiVisualizationPromptDistribution = apiVisualization + "/prompt_distribution/read";
		apiVisualizationPromptTimeseries = apiVisualization + "/prompt_timeseries/read";
		apiVisualizationUserTimeseries = apiVisualization + "/user_timeseries/read";
		apiVisualizationScatterPlot = apiVisualization + "/scatter_plot/read";
		apiVisualization2dDensity = apiVisualization + "/2d_density/read";
		apiVisualizationSurveyResponsePrivacy = apiVisualization + "/survey_responses_privacy_state/read";
		apiVisualizationSurveyResponsePrivacyTimeseries = apiVisualization + "/survey_responses_privacy_state_time/read";
	}
	
	/**
	 * Builds a new request based on the request's URI. This will always return
	 * a request and will never return null. If the URI is unknown it will 
	 * return a {@link org.ohmage.request.FailedRequest}.
	 * 
	 * @param httpRequest The incoming HTTP request.
	 * 
	 * @return A new Request object based on the HTTP request's URI.
	 * 
	 * @throws InvalidRequestException Thrown if the parameters cannot be 
	 * 								   parsed.
	 * 
	 * @throws IOException There was an error reading from the request.
	 */
	public Request buildRequest(
			final HttpServletRequest httpRequest) 
			throws IOException, InvalidRequestException {
		
		String requestUri = httpRequest.getRequestURI();
		
		LOGGER.debug(requestUri);
		
		// Config
		if(apiConfigRead.equals(requestUri)) {
			return new ConfigReadRequest(httpRequest);
		}
		// Authentication
		else if(apiUserAuth.equals(requestUri)) {
			return new AuthRequest(httpRequest);
		}
		else if(apiUserAuthToken.equals(requestUri)) {
			return new AuthTokenRequest(httpRequest);
		}
		else if(apiUserLogout.equals(requestUri)) {
			return new AuthTokenLogoutRequest(httpRequest);
		}
		else if(apiUserWhoAmI.equals(requestUri)) {
			return new AuthTokenWhoAmIRequest(httpRequest);
		}
		// Annotation
		else if(apiAnnotationPromptResponseCreate.equals(requestUri)) {
			return new PromptResponseAnnotationCreationRequest(httpRequest);
		}
		else if(apiAnnotationPromptResponseRead.equals(requestUri)) {
			return new PromptResponseAnnotationReadRequest(httpRequest);
		}
		else if(apiAnnotationSurveyResponseCreate.equals(requestUri)) {
			return new SurveyResponseAnnotationCreationRequest(httpRequest);
		}
		else if(apiAnnotationSurveyResponseRead.equals(requestUri)) {
			return new SurveyResponseAnnotationReadRequest(httpRequest);
		}
		else if(apiAnnotationUpdate.equals(requestUri)) {
			return new AnnotationUpdateRequest(httpRequest);
		}
		else if(apiAnnotationDelete.equals(requestUri)) {
			return new AnnotationDeleteRequest(httpRequest);
		}
		// Audio
		else if(apiAudioRead.equals(requestUri)) {
			return new AudioReadRequest(httpRequest);
		}
		// Audit
		else if(apiAuditRead.equals(requestUri)) {
			return new AuditReadRequest(httpRequest);
		}
		// Campaign
		else if(apiCampaignAssignment.equals(requestUri)) {
			return new CampaignAssignmentRequest(httpRequest);
		}
		else if(apiCampaignCreate.equals(requestUri)) {
			return new CampaignCreationRequest(httpRequest);
		}
		else if(apiCampaignRead.equals(requestUri)) {
			return new CampaignReadRequest(httpRequest);
		}
		else if(apiCampaignSearch.equals(requestUri)) {
			return new CampaignSearchRequest(httpRequest);
		}
		else if(apiCampaignUpdate.equals(requestUri)) {
			return new CampaignUpdateRequest(httpRequest);
		}
		else if(apiCampaignDelete.equals(requestUri)) {
			return new CampaignDeletionRequest(httpRequest);
		}
		// Class
		else if(apiClassCreate.equals(requestUri)) {
			return new ClassCreationRequest(httpRequest);
		}
		else if(apiClassRead.equals(requestUri)) {
			return new ClassReadRequest(httpRequest);
		}
		else if(apiClassRosterRead.equals(requestUri)) {
			return new ClassRosterReadRequest(httpRequest);
		}
		else if(apiClassSearch.equals(requestUri)) {
			return new ClassSearchRequest(httpRequest);
		}
		else if(apiClassUpdate.equals(requestUri)) {
			return new ClassUpdateRequest(httpRequest);
		}
		else if(apiClassRosterUpdate.equals(requestUri)) {
			return new ClassRosterUpdateRequest(httpRequest);
		}
		else if(apiClassDelete.equals(requestUri)) {
			return new ClassDeletionRequest(httpRequest);
		}
		// Document
		else if(apiDocumentCreate.equals(requestUri)) {
			return new DocumentCreationRequest(httpRequest);
		}
		else if(apiDocumentRead.equals(requestUri)) {
			return new DocumentReadRequest(httpRequest);
		}
		else if(apiDocumentReadContents.equals(requestUri)) {
			return new DocumentReadContentsRequest(httpRequest);
		}
		else if(apiDocumentUpdate.equals(requestUri)) {
			return new DocumentUpdateRequest(httpRequest);
		}
		else if(apiDocumentDelete.equals(requestUri)) {
			return new DocumentDeletionRequest(httpRequest);
		}
		// Image
		else if(apiImageRead.equals(requestUri)) {
			return new ImageReadRequest(httpRequest);
		}
		else if(apiImageBatchZipRead.equals(requestUri)) {
			return new ImageBatchZipReadRequest(httpRequest);
		}
		// apiMediaRead
		else if(apiMediaRead.equals(requestUri)) {
			return new MediaReadRequest(httpRequest);
		}
		// Mobility
		else if(apiMobilityUpload.equals(requestUri)) {
			return new MobilityUploadRequest(httpRequest);
		}
		else if(apiMobilityRead.equals(requestUri)) {
			return new MobilityReadRequest(httpRequest);
		}
		else if(apiMobilityReadChunked.equals(requestUri)) {
			return new MobilityReadChunkedRequest(httpRequest);
		}
		else if(apiMobilityAggregateRead.equals(requestUri)) {
			return new MobilityAggregateReadRequest(httpRequest);
		}
		else if(apiMobilityDatesRead.equals(requestUri)) {
			return new MobilityDatesReadRequest(httpRequest);
		}
		else if(apiMobilityReadCsv.equals(requestUri)) {
			return new MobilityReadCsvRequest(httpRequest);
		}
		else if(apiMobilityUpdate.equals(requestUri)) {
			return new MobilityUpdateRequest(httpRequest);
		}
		// Observer
		else if(apiObserverCreate.equals(requestUri)) {
			return new ObserverCreationRequest(httpRequest);
		}
		else if(apiObserverRead.equals(requestUri)) {
			return new ObserverReadRequest(httpRequest, false);
		}
		else if(apiObserverReadXml.equals(requestUri)) {
			return new ObserverReadRequest(httpRequest, true);
		}
		else if(apiObserverUpdate.equals(requestUri)) {
			return new ObserverUpdateRequest(httpRequest);
		}
		else if(apiStreamUpload.equals(requestUri)) {
			return new StreamUploadRequest(httpRequest);
		}
		else if(apiStreamRead.equals(requestUri)) {
			return new StreamReadRequest(httpRequest);
		}
		else if(apiStreamInvalidRead.equals(requestUri)) {
			return new StreamReadInvalidRequest(httpRequest);
		}
		// OMH
		else if(apiOmhAuth.equals(requestUri)) {
			return new OmhAuthenticateRequest(httpRequest);
		}
		else if(apiOmhRegistryCreate.equals(requestUri)) {
			return new OmhRegistryCreateRequest(httpRequest);
		}
		else if(apiOmhRegistryRead.equals(requestUri)) {
			return new OmhRegistryReadRequest(httpRequest);
		}
		else if(apiOmhRegistryUpdate.equals(requestUri)) {
			return new OmhRegistryUpdateRequest(httpRequest);
		}
		else if(apiOmhCatalog.equals(requestUri)) {
			return new OmhCatalogRequest(httpRequest);
		}
		else if(apiOmhRead.equals(requestUri)) {
			return new OmhReadRequest(httpRequest);
		}
		else if(apiOmhWrite.equals(requestUri)) {
			return new OmhWriteRequest(httpRequest);
		}
		// Survey
		else if(apiSurveyUpload.equals(requestUri)) {
			return new SurveyUploadRequest(httpRequest);
		}
		else if(apiSurveyResponseRead.equals(requestUri)) {
			return new SurveyResponseReadRequest(httpRequest);
		}
		else if(apiSurveyResponseUpdate.equals(requestUri)) {
			return new SurveyResponseUpdateRequest(httpRequest);
		}
		else if(apiSurveyResponseDelete.equals(requestUri)) {
			return new SurveyResponseDeleteRequest(httpRequest);
		}
		else if(apiSurveyResponseFunctionRead.equals(requestUri)) {
			return new SurveyResponseFunctionReadRequest(httpRequest);
		}
		// User
		else if(apiUserCreate.equals(requestUri)) {
			return new UserCreationRequest(httpRequest);
		}
		else if(apiUserRegister.equals(requestUri)) {
			return new UserRegistrationRequest(httpRequest);
		}
		else if(apiUserActivate.equals(requestUri)) {
			return new UserActivationRequest(httpRequest);
		}
		else if(apiUserPasswordReset.equals(requestUri)) {
			return new UserPasswordResetRequest(httpRequest);
		}
		else if(apiUserRead.equals(requestUri)) {
			return new UserReadRequest(httpRequest);
		}
		else if(apiUserInfoRead.equals(requestUri)) {
			return new UserInfoReadRequest(httpRequest);
		}
		else if(apiUserStatsRead.equals(requestUri)) {
			return new UserStatsReadRequest(httpRequest);
		}
		else if(apiUserSearch.equals(requestUri)) {
			return new UserSearchRequest(httpRequest);
		}
		else if(apiUserUpdate.equals(requestUri)) {
			return new UserUpdateRequest(httpRequest);
		}
		else if(apiUserChangePassword.equals(requestUri)) {
			return new UserChangePasswordRequest(httpRequest);
		}
		else if(apiUserDelete.equals(requestUri)) {
			return new UserDeletionRequest(httpRequest);
		}
		// Registration
		else if(apiRegistrationRead.equals(requestUri)) {
			return new RegistrationReadRequest(httpRequest);
		}
		else if(apiVideoRead.equals(requestUri)) {
			return new VideoReadRequest(httpRequest);
		}
		// Visualization
		else if(apiVisualizationSurveyResponseCount.equals(requestUri)) {
			return new VizSurveyResponseCountRequest(httpRequest);
		}
		else if(apiVisualizationPromptDistribution.equals(requestUri)) {
			return new VizPromptDistributionRequest(httpRequest);
		}
		else if(apiVisualizationPromptTimeseries.equals(requestUri)) {
			return new VizPromptTimeseriesRequest(httpRequest);
		}
		else if(apiVisualizationUserTimeseries.equals(requestUri)) {
			return new VizUserTimeseriesRequest(httpRequest);
		}
		else if(apiVisualizationScatterPlot.equals(requestUri)) {
			return new VizScatterPlotRequest(httpRequest);
		}
		else if(apiVisualization2dDensity.equals(requestUri)) {
			return new VizTwoDDensityRequest(httpRequest);
		}
		else if(apiVisualizationSurveyResponsePrivacy.equals(requestUri)) {
			return new VizSurveyResponsePrivacyStateRequest(httpRequest);
		}
		else if(apiVisualizationSurveyResponsePrivacyTimeseries.equals(requestUri)) {
			return new VizSurveyResponsePrivacyStateTimeseriesRequest(httpRequest);
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
	public boolean knownUri(String uri) {
		if(
				// Config
				apiConfigRead.equals(uri) ||
				// Annotation
				apiAnnotationPromptResponseCreate.equals(uri) ||
				apiAnnotationPromptResponseRead.equals(uri) ||
				apiAnnotationSurveyResponseCreate.equals(uri) ||
				apiAnnotationSurveyResponseRead.equals(uri) ||
				// Authentication
				apiUserAuth.equals(uri) ||
				apiUserAuthToken.equals(uri) ||
				apiUserLogout.equals(uri) ||
				apiUserWhoAmI.equals(uri) ||
				// Audio
				apiAudioRead.equals(uri) ||
				// Audit
				apiAuditRead.equals(uri) ||
				// Campaign
				apiCampaignAssignment.equals(uri) ||
				apiCampaignCreate.equals(uri) ||
				apiCampaignRead.equals(uri) ||
				apiCampaignSearch.equals(uri) ||
				apiCampaignUpdate.equals(uri) ||
				apiCampaignDelete.equals(uri) ||
				// Class
				apiClassCreate.equals(uri) ||
				apiClassRead.equals(uri) ||
				apiClassRosterRead.equals(uri) ||
				apiClassSearch.equals(uri) ||
				apiClassUpdate.equals(uri) ||
				apiClassRosterUpdate.equals(uri) ||
				apiClassDelete.equals(uri) ||
				// Document
				apiDocumentCreate.equals(uri) ||
				apiDocumentRead.equals(uri) ||
				apiDocumentReadContents.equals(uri) ||
				apiDocumentUpdate.equals(uri) ||
				apiDocumentDelete.equals(uri) ||
				// Image
				apiImageRead.equals(uri) ||
				apiImageBatchZipRead.equals(uri) ||
				// Mobility
				apiMobilityUpload.equals(uri) ||
				apiMobilityRead.equals(uri) ||
				apiMobilityReadChunked.equals(uri) ||
				apiMobilityAggregateRead.equals(uri) ||
				apiMobilityDatesRead.equals(uri) ||
				apiMobilityReadCsv.equals(uri) ||
				apiMobilityUpdate.equals(uri) ||
				// Observer
				apiObserverCreate.equals(uri) ||
				apiObserverRead.equals(uri) ||
				apiObserverReadXml.equals(uri) ||
				apiObserverUpdate.equals(uri) ||
				apiStreamUpload.equals(uri) ||
				apiStreamRead.equals(uri) ||
				apiStreamInvalidRead.equals(uri) ||
				// OMH
				apiOmhAuth.equals(uri) ||
				apiOmhRegistryCreate.equals(uri) ||
				apiOmhRegistryRead.equals(uri) ||
				apiOmhRegistryUpdate.equals(uri) ||
				apiOmhCatalog.equals(uri) ||
				apiOmhRead.equals(uri) ||
				apiOmhWrite.equals(uri) ||
				// Survey
				apiSurveyUpload.equals(uri) ||
				apiSurveyResponseRead.equals(uri) ||
				apiSurveyResponseUpdate.equals(uri) ||
				apiSurveyResponseDelete.equals(uri) ||
				apiSurveyResponseFunctionRead.equals(uri) ||
				// User
				apiUserCreate.equals(uri) ||
				apiUserRegister.equals(uri) ||
				apiUserActivate.equals(uri) ||
				apiUserPasswordReset.equals(uri) ||
				apiUserRead.equals(uri) ||
				apiUserInfoRead.equals(uri) ||
				apiUserStatsRead.equals(uri) ||
				apiUserSearch.equals(uri) ||
				apiUserUpdate.equals(uri) ||
				apiUserChangePassword.equals(uri) ||
				apiUserDelete.equals(uri) ||
				// Registration
				apiRegistrationRead.equals(uri) ||
				// Video
				apiVideoRead.equals(uri) ||
				// Visualization
				apiVisualizationSurveyResponseCount.equals(uri) ||
				apiVisualizationPromptDistribution.equals(uri) ||
				apiVisualizationPromptTimeseries.equals(uri) ||
				apiVisualizationUserTimeseries.equals(uri) ||
				apiVisualizationScatterPlot.equals(uri) ||
				apiVisualization2dDensity.equals(uri) ||
				apiVisualizationSurveyResponsePrivacy.equals(uri) ||
				apiVisualizationSurveyResponsePrivacyTimeseries.equals(uri)) {
			return true;
		}
		
		// The URI is unknown.
		return false;
	}

	/**
	 * Returns the root of this web application.
	 * 
	 * @return The root of this web application.
	 */
	public String getRoot() {
		return apiRoot;
	}

	/**
	 * Returns apiAnnotationPromptResponseCreate.
	 *
	 * @return The apiAnnotationPromptResponseCreate.
	 */
	public String getApiAnnotationPromptResponseCreate() {
		return apiAnnotationPromptResponseCreate;
	}

	/**
	 * Returns apiAnnotationPromptResponseRead.
	 *
	 * @return The apiAnnotationPromptResponseRead.
	 */
	public String getApiAnnotationPromptResponseRead() {
		return apiAnnotationPromptResponseRead;
	}

	/**
	 * Returns apiAnnotationSurveyResponseCreate.
	 *
	 * @return The apiAnnotationSurveyResponseCreate.
	 */
	public String getApiAnnotationSurveyResponseCreate() {
		return apiAnnotationSurveyResponseCreate;
	}

	/**
	 * Returns apiAnnotationSurveyResponseRead.
	 *
	 * @return The apiAnnotationSurveyResponseRead.
	 */
	public String getApiAnnotationSurveyResponseRead() {
		return apiAnnotationSurveyResponseRead;
	}

	/**
	 * Returns apiAnnotationUpdate.
	 *
	 * @return The apiAnnotationUpdate.
	 */
	public String getApiAnnotationUpdate() {
		return apiAnnotationUpdate;
	}

	/**
	 * Returns apiAnnotationDelete.
	 *
	 * @return The apiAnnotationDelete.
	 */
	public String getApiAnnotationDelete() {
		return apiAnnotationDelete;
	}
	
	/**
	 * Returns apiAudioRead.
	 * 
	 * @return The apiAudioRead.
	 */
	public String getApiAudioRead() {
		return apiAudioRead;
	}

	/**
	 * Returns apiAuditRead.
	 *
	 * @return The apiAuditRead.
	 */
	public String getApiAuditRead() {
		return apiAuditRead;
	}

	/**
	 * Returns apiUserAuth.
	 *
	 * @return The apiUserAuth.
	 */
	public String getApiUserAuth() {
		return apiUserAuth;
	}

	/**
	 * Returns apiUserAuthToken.
	 *
	 * @return The apiUserAuthToken.
	 */
	public String getApiUserAuthToken() {
		return apiUserAuthToken;
	}

	/**
	 * Returns apiUserLogout.
	 *
	 * @return The apiUserLogout.
	 */
	public String getApiUserLogout() {
		return apiUserLogout;
	}

	/**
	 * Returns apiOmhAuth.
	 *
	 * @return The apiOmhAuth.
	 */
	public String getApiOmhAuth() {
		return apiOmhAuth;
	}
	
	/**
	 * Returns the URI for OMH's catalog API.
	 * 
	 * @return The URI for OMH's catalog API.
	 */
	public String getApiOmhCatalog() {
		return apiOmhCatalog;
	}
	
	/**
	 * Returns the URI for OMH's read API.
	 * 
	 * @return The URI for OMH's read API.
	 */
	public String getApiOmhRead() {
		return apiOmhRead;
	}

	/**
	 * Returns apiCampaignCreate.
	 *
	 * @return The apiCampaignCreate.
	 */
	public String getApiCampaignCreate() {
		return apiCampaignCreate;
	}

	/**
	 * Returns apiCampaignRead.
	 *
	 * @return The apiCampaignRead.
	 */
	public String getApiCampaignRead() {
		return apiCampaignRead;
	}

	/**
	 * Returns apiCampaignSearch.
	 *
	 * @return The apiCampaignSearch.
	 */
	public String getApiCampaignSearch() {
		return apiCampaignSearch;
	}

	/**
	 * Returns apiCampaignUpdate.
	 *
	 * @return The apiCampaignUpdate.
	 */
	public String getApiCampaignUpdate() {
		return apiCampaignUpdate;
	}

	/**
	 * Returns apiCampaignDelete.
	 *
	 * @return The apiCampaignDelete.
	 */
	public String getApiCampaignDelete() {
		return apiCampaignDelete;
	}

	/**
	 * Returns apiClassCreate.
	 *
	 * @return The apiClassCreate.
	 */
	public String getApiClassCreate() {
		return apiClassCreate;
	}

	/**
	 * Returns apiClassRead.
	 *
	 * @return The apiClassRead.
	 */
	public String getApiClassRead() {
		return apiClassRead;
	}

	/**
	 * Returns apiClassRosterRead.
	 *
	 * @return The apiClassRosterRead.
	 */
	public String getApiClassRosterRead() {
		return apiClassRosterRead;
	}

	/**
	 * Returns apiClassSearch.
	 *
	 * @return The apiClassSearch.
	 */
	public String getApiClassSearch() {
		return apiClassSearch;
	}

	/**
	 * Returns apiClassUpdate.
	 *
	 * @return The apiClassUpdate.
	 */
	public String getApiClassUpdate() {
		return apiClassUpdate;
	}

	/**
	 * Returns apiClassRosterUpdate.
	 *
	 * @return The apiClassRosterUpdate.
	 */
	public String getApiClassRosterUpdate() {
		return apiClassRosterUpdate;
	}

	/**
	 * Returns apiClassDelete.
	 *
	 * @return The apiClassDelete.
	 */
	public String getApiClassDelete() {
		return apiClassDelete;
	}

	/**
	 * Returns apiConfigRead.
	 *
	 * @return The apiConfigRead.
	 */
	public String getApiConfigRead() {
		return apiConfigRead;
	}

	/**
	 * Returns apiDocumentCreate.
	 *
	 * @return The apiDocumentCreate.
	 */
	public String getApiDocumentCreate() {
		return apiDocumentCreate;
	}

	/**
	 * Returns apiDocumentRead.
	 *
	 * @return The apiDocumentRead.
	 */
	public String getApiDocumentRead() {
		return apiDocumentRead;
	}

	/**
	 * Returns apiDocumentReadContents.
	 *
	 * @return The apiDocumentReadContents.
	 */
	public String getApiDocumentReadContents() {
		return apiDocumentReadContents;
	}

	/**
	 * Returns apiDocumentUpdate.
	 *
	 * @return The apiDocumentUpdate.
	 */
	public String getApiDocumentUpdate() {
		return apiDocumentUpdate;
	}

	/**
	 * Returns apiDocumentDelete.
	 *
	 * @return The apiDocumentDelete.
	 */
	public String getApiDocumentDelete() {
		return apiDocumentDelete;
	}

	/**
	 * Returns apiImageRead.
	 *
	 * @return The apiImageRead.
	 */
	public String getApiImageRead() {
		return apiImageRead;
	}

	/**
	 * Returns apiImageBatchZipRead.
	 *
	 * @return The apiImageBatchZipRead.
	 */
	public String getApiImageBatchZipRead() {
		return apiImageBatchZipRead;
	}

	/**
	 * Returns apiMobilityUpload.
	 *
	 * @return The apiMobilityUpload.
	 */
	public String getApiMobilityUpload() {
		return apiMobilityUpload;
	}

	/**
	 * Returns apiMobilityRead.
	 *
	 * @return The apiMobilityRead.
	 */
	public String getApiMobilityRead() {
		return apiMobilityRead;
	}

	/**
	 * Returns apiMobilityReadChunked.
	 *
	 * @return The apiMobilityReadChunked.
	 */
	public String getApiMobilityReadChunked() {
		return apiMobilityReadChunked;
	}

	/**
	 * Returns apiMobilityAggregateRead.
	 *
	 * @return The apiMobilityAggregateRead.
	 */
	public String getApiMobilityAggregateRead() {
		return apiMobilityAggregateRead;
	}

	/**
	 * Returns apiMobilityDatesRead.
	 *
	 * @return The apiMobilityDatesRead.
	 */
	public String getApiMobilityDatesRead() {
		return apiMobilityDatesRead;
	}

	/**
	 * Returns apiMobilityReadCsv.
	 *
	 * @return The apiMobilityReadCsv.
	 */
	public String getApiMobilityReadCsv() {
		return apiMobilityReadCsv;
	}

	/**
	 * Returns apiMobilityUpdate.
	 *
	 * @return The apiMobilityUpdate.
	 */
	public String getApiMobilityUpdate() {
		return apiMobilityUpdate;
	}

	/**
	 * Returns apiObserverCreate.
	 *
	 * @return The apiObserverCreate.
	 */
	public String getApiObserverCreate() {
		return apiObserverCreate;
	}

	/**
	 * Returns apiObserverUpdate.
	 *
	 * @return The apiObserverUpdate.
	 */
	public String getApiObserverUpdate() {
		return apiObserverUpdate;
	}

	/**
	 * Returns apiStreamUpload.
	 *
	 * @return The apiStreamUpload.
	 */
	public String getApiStreamUpload() {
		return apiStreamUpload;
	}

	/**
	 * Returns apiStreamRead.
	 *
	 * @return The apiStreamRead.
	 */
	public String getApiStreamRead() {
		return apiStreamRead;
	}

	/**
	 * Returns apiStreamInvalidRead.
	 *
	 * @return The apiStreamInvalidRead.
	 */
	public String getApiStreamInvalidRead() {
		return apiStreamInvalidRead;
	}

	/**
	 * Returns apiSurveyUpload.
	 *
	 * @return The apiSurveyUpload.
	 */
	public String getApiSurveyUpload() {
		return apiSurveyUpload;
	}

	/**
	 * Returns apiSurveyResponseDelete.
	 *
	 * @return The apiSurveyResponseDelete.
	 */
	public String getApiSurveyResponseDelete() {
		return apiSurveyResponseDelete;
	}

	/**
	 * Returns apiSurveyResponseRead.
	 *
	 * @return The apiSurveyResponseRead.
	 */
	public String getApiSurveyResponseRead() {
		return apiSurveyResponseRead;
	}

	/**
	 * Returns apiSurveyResponseUpdate.
	 *
	 * @return The apiSurveyResponseUpdate.
	 */
	public String getApiSurveyResponseUpdate() {
		return apiSurveyResponseUpdate;
	}

	/**
	 * Returns apiSurveyResponseFunctionRead.
	 *
	 * @return The apiSurveyResponseFunctionRead.
	 */
	public String getApiSurveyResponseFunctionRead() {
		return apiSurveyResponseFunctionRead;
	}

	/**
	 * Returns apiUserCreate.
	 *
	 * @return The apiUserCreate.
	 */
	public String getApiUserCreate() {
		return apiUserCreate;
	}

	/**
	 * Returns apiUserRegister.
	 *
	 * @return The apiUserRegister.
	 */
	public String getApiUserRegister() {
		return apiUserRegister;
	}

	/**
	 * Returns apiUserActivate.
	 *
	 * @return The apiUserActivate.
	 */
	public String getApiUserActivate() {
		return apiUserActivate;
	}

	/**
	 * Returns apiUserPasswordReset.
	 *
	 * @return The apiUserPasswordReset.
	 */
	public String getApiUserPasswordReset() {
		return apiUserPasswordReset;
	}

	/**
	 * Returns apiUserRead.
	 *
	 * @return The apiUserRead.
	 */
	public String getApiUserRead() {
		return apiUserRead;
	}

	/**
	 * Returns apiUserInfoRead.
	 *
	 * @return The apiUserInfoRead.
	 */
	public String getApiUserInfoRead() {
		return apiUserInfoRead;
	}

	/**
	 * Returns apiUserStatsRead.
	 *
	 * @return The apiUserStatsRead.
	 */
	public String getApiUserStatsRead() {
		return apiUserStatsRead;
	}

	/**
	 * Returns apiUserSearch.
	 *
	 * @return The apiUserSearch.
	 */
	public String getApiUserSearch() {
		return apiUserSearch;
	}

	/**
	 * Returns apiUserUpdate.
	 *
	 * @return The apiUserUpdate.
	 */
	public String getApiUserUpdate() {
		return apiUserUpdate;
	}

	/**
	 * Returns apiUserChangePassword.
	 *
	 * @return The apiUserChangePassword.
	 */
	public String getApiUserChangePassword() {
		return apiUserChangePassword;
	}

	/**
	 * Returns apiUserDelete.
	 *
	 * @return The apiUserDelete.
	 */
	public String getApiUserDelete() {
		return apiUserDelete;
	}

	/**
	 * Returns apiRegistrationRead.
	 *
	 * @return The apiRegistrationRead.
	 */
	public String getApiRegistrationRead() {
		return apiRegistrationRead;
	}

	/**
	 * Returns apiVideoRead.
	 *
	 * @return The apiVideoRead.
	 */
	public String getApiVideoRead() {
		return apiVideoRead;
	}

	/**
	 * Returns apiVisualization.
	 *
	 * @return The apiVisualization.
	 */
	public String getApiVisualization() {
		return apiVisualization;
	}

	/**
	 * Returns apiVisualizationSurveyResponseCount.
	 *
	 * @return The apiVisualizationSurveyResponseCount.
	 */
	public String getApiVisualizationSurveyResponseCount() {
		return apiVisualizationSurveyResponseCount;
	}

	/**
	 * Returns apiVisualizationPromptDistribution.
	 *
	 * @return The apiVisualizationPromptDistribution.
	 */
	public String getApiVisualizationPromptDistribution() {
		return apiVisualizationPromptDistribution;
	}

	/**
	 * Returns apiVisualizationPromptTimeseries.
	 *
	 * @return The apiVisualizationPromptTimeseries.
	 */
	public String getApiVisualizationPromptTimeseries() {
		return apiVisualizationPromptTimeseries;
	}

	/**
	 * Returns apiVisualizationUserTimeseries.
	 *
	 * @return The apiVisualizationUserTimeseries.
	 */
	public String getApiVisualizationUserTimeseries() {
		return apiVisualizationUserTimeseries;
	}

	/**
	 * Returns apiVisualizationScatterPlot.
	 *
	 * @return The apiVisualizationScatterPlot.
	 */
	public String getApiVisualizationScatterPlot() {
		return apiVisualizationScatterPlot;
	}

	/**
	 * Returns apiVisualization2dDensity.
	 *
	 * @return The apiVisualization2dDensity.
	 */
	public String getApiVisualization2dDensity() {
		return apiVisualization2dDensity;
	}

	/**
	 * Returns apiVisualizationSurveyResponsePrivacy.
	 *
	 * @return The apiVisualizationSurveyResponsePrivacy.
	 */
	public String getApiVisualizationSurveyResponsePrivacy() {
		return apiVisualizationSurveyResponsePrivacy;
	}

	/**
	 * Returns apiVisualizationSurveyResponsePrivacyTimeseries.
	 *
	 * @return The apiVisualizationSurveyResponsePrivacyTimeseries.
	 */
	public String getApiVisualizationSurveyResponsePrivacyTimeseries() {
		return apiVisualizationSurveyResponsePrivacyTimeseries;
	}
	
	public String getApiUserWhoAmI() {
		return apiUserWhoAmI;
	}
}
