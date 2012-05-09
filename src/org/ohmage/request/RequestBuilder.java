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

import java.lang.reflect.InvocationTargetException;

import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;
import org.ohmage.request.audit.AuditReadRequest;
import org.ohmage.request.auth.AuthRequest;
import org.ohmage.request.auth.AuthTokenLogoutRequest;
import org.ohmage.request.auth.AuthTokenRequest;
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
import org.ohmage.request.mobility.MobilityAggregateReadRequest;
import org.ohmage.request.mobility.MobilityDatesReadRequest;
import org.ohmage.request.mobility.MobilityReadChunkedRequest;
import org.ohmage.request.mobility.MobilityReadCsvRequest;
import org.ohmage.request.mobility.MobilityReadRequest;
import org.ohmage.request.mobility.MobilityUpdateRequest;
import org.ohmage.request.mobility.MobilityUploadRequest;
import org.ohmage.request.observer.ObserverCreationRequest;
import org.ohmage.request.observer.StreamUploadRequest;
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
import org.ohmage.request.visualization.VizPromptDistributionRequest;
import org.ohmage.request.visualization.VizPromptTimeseriesRequest;
import org.ohmage.request.visualization.VizScatterPlotRequest;
import org.ohmage.request.visualization.VizSurveyResponseCountRequest;
import org.ohmage.request.visualization.VizSurveyResponsePrivacyStateRequest;
import org.ohmage.request.visualization.VizSurveyResponsePrivacyStateTimeseriesRequest;
import org.ohmage.request.visualization.VizTwoDDensityRequest;
import org.ohmage.request.visualization.VizUserTimeseriesRequest;

/**
 * Request builder from an HTTP request.
 * 
 * @author John Jenkins
 * @author Joshua Selsky
 */
public final class RequestBuilder {
	private static final Logger LOGGER = Logger.getLogger(RequestBuilder.class);

	/**
	 * Default constructor. Made private because this class should never be
	 * instantiated. Instead, the static builder method should be called.
	 */
	private RequestBuilder() {}
	
	/**
	 * This enum was initially intended to be a central location to hold the 
	 * information required to build a new Request. An enum was used instead of
	 * a class because I was hoping to use the built-in functionality for 
	 * looking up enums to minimize the code for building Requests. However, 
	 * this has been more difficult than originally perceived and is being put 
	 * on the back burner for the time being. It also drastically limits what 
	 * the constructor for each Request can consist of, forcing all requests to 
	 * having the same constructor format.
	 * 
	 * @author John Jenkins
	 */
	public static enum RequestEnum {
		AUTHENTICATE_REQUEST (AuthRequest.class),
		AUTHENTICATION_TOKEN_REQUEST (AuthTokenRequest.class);
		
		private final Class<? extends Request> requestClass;
		
		/**
		 * Correlates a Request subclass with this RequestEnum.
		 * 
		 * @param requestClass The Request subclass that should be created when
		 * 					   this class is run.
		 */
		private RequestEnum(final Class<? extends Request> requestClass) {
			if(requestClass == null) {
				throw new IllegalStateException("The request's class cannot be null.");
			}
			
			this.requestClass = requestClass;
		}
		
		/**
		 * Creates a new Request based on how this enum object.
		 * 
		 * @param httpRequest The HttpServletRequest with the matching URI.
		 * 
		 * @return A Request object based on this enum.
		 */
		public Request getRequest(final HttpServletRequest httpRequest) {
			try {
				return requestClass.getConstructor(HttpServletRequest.class).newInstance(httpRequest);
			}
			catch(NoSuchMethodException e) {
				LOGGER.error("The HttpServletRequest constructor is missing for the Request: " + requestClass.getCanonicalName());
				throw new IllegalStateException("The HttpServletRequest constructor is missing for the Request: " + requestClass.getCanonicalName(), e);
			}
			catch(SecurityException e) {
				LOGGER.error("The SecurityManager is preventing us from accessing the constructor via reflection: " + requestClass.getCanonicalName());
				throw new IllegalStateException("The SecurityManager is preventing us from accessing the constructor via reflection: " + requestClass.getCanonicalName(), e);
			}
			catch(InvocationTargetException e) {
				LOGGER.error("The constructor threw an exception: " + requestClass.getCanonicalName());
				throw new IllegalStateException("The constructor threw an exception: " + requestClass.getCanonicalName(), e);
			}
			catch(IllegalAccessException e) {
				LOGGER.error("The constructor enforces Java language access control and is preventing instantiation: " + requestClass.getCanonicalName());
				throw new IllegalStateException("The constructor enforces Java language access control and is preventing instantiation: " + requestClass.getCanonicalName(), e);
			}
			catch(InstantiationException e) {
				LOGGER.error("Attempting to instantiate an abstract class: " + requestClass.getCanonicalName());
				throw new IllegalStateException("Attempting to instantiate an abstract class: " + requestClass.getCanonicalName(), e);
			}
			catch(ExceptionInInitializerError e) {
				LOGGER.error("The initialization failed: " + requestClass.getCanonicalName());
				throw new IllegalStateException("The initialization failed: " + requestClass.getCanonicalName(), e);
			}
			catch(IllegalArgumentException e) {
				LOGGER.error("The parameter's type doesn't match the desired type: " + requestClass.getCanonicalName());
				throw new IllegalStateException("The parameter's type doesn't match the desired type: " + requestClass.getCanonicalName(), e);
			}
		}
	}
	
	public static final String API_ROOT = "/app";
	
	// Annotation
	public static final String API_ANNOTATION_PROMPT_RESPONSE_CREATE = API_ROOT + "/annotation/prompt_response/create";
	public static final String API_ANNOTATION_PROMPT_RESPONSE_READ = API_ROOT + "/annotation/prompt_response/read";
	public static final String API_ANNOTATION_SURVEY_RESPONSE_CREATE = API_ROOT + "/annotation/survey_response/create";
	public static final String API_ANNOTATION_SURVEY_RESPONSE_READ = API_ROOT + "/annotation/survey_response/read";
	public static final String API_ANNOTATION_UDPATE = API_ROOT + "/annotation/update";
	public static final String API_ANNOTATION_DELETE = API_ROOT + "/annotation/delete";
	
	// Audit
	public static final String API_AUDIT_READ = API_ROOT + "/audit/read";
	
	// Authentication
	public static final String API_USER_AUTH = API_ROOT + "/user/auth";
	public static final String API_USER_AUTH_TOKEN = API_ROOT + "/user/auth_token";
	public static final String API_USER_LOGOUT = API_ROOT + "/user/logout";
	
	// Campaign
	public static final String API_CAMPAIGN_CREATE = API_ROOT + "/campaign/create";
	public static final String API_CAMPAIGN_READ = API_ROOT + "/campaign/read";
	public static final String API_CAMPAIGN_SEARCH = API_ROOT + "/campaign/search";
	public static final String API_CAMPAIGN_UPDATE = API_ROOT + "/campaign/update";
	public static final String API_CAMPAIGN_DELETE = API_ROOT + "/campaign/delete";
	
	// Class
	public static final String API_CLASS_CREATE = API_ROOT + "/class/create";
	public static final String API_CLASS_READ = API_ROOT + "/class/read";
	public static final String API_CLASS_ROSTER_READ = API_ROOT + "/class/roster/read";
	public static final String API_CLASS_SEARCH = API_ROOT + "/class/search";
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
	public static final String API_IMAGE_BATCH_ZIP_READ = API_ROOT + "/image/batch/zip/read";
	
	// Mobility
	public static final String API_MOBILITY_UPLOAD = API_ROOT + "/mobility/upload";
	public static final String API_MOBILITY_READ = API_ROOT + "/mobility/read";
	public static final String API_MOBILITY_READ_CHUNKED = API_ROOT + "/mobility/read/chunked";
	public static final String API_MOBILITY_AGGREGATE_READ = API_ROOT + "/mobility/aggregate/read";
	public static final String API_MOBILITY_DATES_READ = API_ROOT + "/mobility/dates/read";
	public static final String API_MOBILITY_READ_CSV = API_ROOT + "/mobility/read/csv";
	public static final String API_MOBILITY_UPDATE = API_ROOT + "/mobility/update";
	
	// Observer
	public static final String API_OBSERVER_CREATE = API_ROOT + "/observer/create";
	public static final String API_OBSERVER_UPLOAD = API_ROOT + "/observer/";
	
	// Survey
	public static final String API_SURVEY_UPLOAD = API_ROOT + "/survey/upload";
	public static final String API_SURVEY_RESPONSE_DELETE = API_ROOT + "/survey_response/delete";
	public static final String API_SURVEY_RESPONSE_READ = API_ROOT + "/survey_response/read";
	public static final String API_SURVEY_RESPONSE_UPDATE = API_ROOT + "/survey_response/update";
	public static final String API_SURVEY_RESPONSE_FUNCTION_READ = API_ROOT + "/survey_response/function/read";
	
	// User
	public static final String API_USER_CREATE = API_ROOT + "/user/create";
	public static final String API_USER_REGISTER = API_ROOT + "/user/register";
	public static final String API_USER_ACTIVATE = API_ROOT + "/user/activate";
	public static final String API_USER_PASSWORD_RESET = API_ROOT + "/user/reset_password";
	public static final String API_USER_READ = API_ROOT + "/user/read";
	public static final String API_USER_INFO_READ = API_ROOT + "/user_info/read";
	public static final String API_USER_STATS_READ = API_ROOT + "/user_stats/read";
	public static final String API_USER_SEARCH = API_ROOT + "/user/search";
	public static final String API_USER_UPDATE = API_ROOT + "/user/update";
	public static final String API_USER_CHANGE_PASSWORD = API_ROOT + "/user/change_password";
	public static final String API_USER_DELETE = API_ROOT + "/user/delete";
	
	// Registration
	public static final String API_REGISTRATION_READ = API_ROOT + "/registration/read";
	
	// Visualization
	public static final String API_VISUALIZATION = API_ROOT + "/viz";
	public static final String API_VISUALIZATION_SURVEY_RESPONSE_COUNT = API_VISUALIZATION + "/survey_response_count/read";
	public static final String API_VISUALIZATION_PROMPT_DISTRIBUTION = API_VISUALIZATION + "/prompt_distribution/read";
	public static final String API_VISUALIZATION_PROMPT_TIMESERIES = API_VISUALIZATION + "/prompt_timeseries/read";
	public static final String API_VISUALIZATION_USER_TIMESERIES = API_VISUALIZATION + "/user_timeseries/read";
	public static final String API_VISUALIZATION_SCATTER_PLOT = API_VISUALIZATION + "/scatter_plot/read";
	public static final String API_VISUALIZATION_2D_DENSITY = API_VISUALIZATION + "/2d_density/read";
	public static final String API_VISUALIZATION_SURVEY_RESPONSE_PRIVACY = API_VISUALIZATION + "/survey_responses_privacy_state/read";
	public static final String API_VISUALIZATION_SURVEY_RESPONSE_PRIVACY_TIMESERIES = API_VISUALIZATION + "/survey_responses_privacy_state_time/read";
	
	/**
	 * Builds a new request based on the request's URI. This will always return
	 * a request and will never return null. If the URI is unknown it will 
	 * return a {@link org.ohmage.request.FailedRequest}.
	 * 
	 * @param httpRequest The incoming HTTP request.
	 * 
	 * @return A new Request object based on the HTTP request's URI.
	 */
	public static Request buildRequest(HttpServletRequest httpRequest) {
		String requestUri = httpRequest.getRequestURI();
		
		LOGGER.debug(requestUri);
		
		// Config
		if(API_CONFIG_READ.equals(requestUri)) {
			return new ConfigReadRequest(httpRequest);
		}
		// Authentication
		else if(API_USER_AUTH.equals(requestUri)) {
			return new AuthRequest(httpRequest);
		}
		else if(API_USER_AUTH_TOKEN.equals(requestUri)) {
			return new AuthTokenRequest(httpRequest);
		}
		else if(API_USER_LOGOUT.equals(requestUri)) {
			return new AuthTokenLogoutRequest(httpRequest);
		}
		// Annotation
		else if(API_ANNOTATION_PROMPT_RESPONSE_CREATE.equals(requestUri)) {
			return new PromptResponseAnnotationCreationRequest(httpRequest);
		}
		else if(API_ANNOTATION_PROMPT_RESPONSE_READ.equals(requestUri)) {
			return new PromptResponseAnnotationReadRequest(httpRequest);
		}
		else if(API_ANNOTATION_SURVEY_RESPONSE_CREATE.equals(requestUri)) {
			return new SurveyResponseAnnotationCreationRequest(httpRequest);
		}
		else if(API_ANNOTATION_SURVEY_RESPONSE_READ.equals(requestUri)) {
			return new SurveyResponseAnnotationReadRequest(httpRequest);
		}
		else if(API_ANNOTATION_UDPATE.equals(requestUri)) {
			return new AnnotationUpdateRequest(httpRequest);
		}
		else if(API_ANNOTATION_DELETE.equals(requestUri)) {
			return new AnnotationDeleteRequest(httpRequest);
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
		else if(API_CAMPAIGN_SEARCH.equals(requestUri)) {
			return new CampaignSearchRequest(httpRequest);
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
		else if(API_CLASS_SEARCH.equals(requestUri)) {
			return new ClassSearchRequest(httpRequest);
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
		else if(API_IMAGE_BATCH_ZIP_READ.equals(requestUri)) {
			return new ImageBatchZipReadRequest(httpRequest);
		}
		// Mobility
		else if(API_MOBILITY_UPLOAD.equals(requestUri)) {
			return new MobilityUploadRequest(httpRequest);
		}
		else if(API_MOBILITY_READ.equals(requestUri)) {
			return new MobilityReadRequest(httpRequest);
		}
		else if(API_MOBILITY_READ_CHUNKED.equals(requestUri)) {
			return new MobilityReadChunkedRequest(httpRequest);
		}
		else if(API_MOBILITY_AGGREGATE_READ.equals(requestUri)) {
			return new MobilityAggregateReadRequest(httpRequest);
		}
		else if(API_MOBILITY_DATES_READ.equals(requestUri)) {
			return new MobilityDatesReadRequest(httpRequest);
		}
		else if(API_MOBILITY_READ_CSV.equals(requestUri)) {
			return new MobilityReadCsvRequest(httpRequest);
		}
		else if(API_MOBILITY_UPDATE.equals(requestUri)) {
			return new MobilityUpdateRequest(httpRequest);
		}
		// Observer
		else if(API_OBSERVER_CREATE.equals(requestUri)) {
			return new ObserverCreationRequest(httpRequest);
		}
		else if(requestUri.startsWith(API_OBSERVER_UPLOAD)) {
			return new StreamUploadRequest(httpRequest);
		}
		//Survey
		else if(API_SURVEY_UPLOAD.equals(requestUri)) {
			return new SurveyUploadRequest(httpRequest);
		}
		else if(API_SURVEY_RESPONSE_READ.equals(requestUri)) {
			return new SurveyResponseReadRequest(httpRequest);
		}
		else if(API_SURVEY_RESPONSE_UPDATE.equals(requestUri)) {
			return new SurveyResponseUpdateRequest(httpRequest);
		}
		else if(API_SURVEY_RESPONSE_DELETE.equals(requestUri)) {
			return new SurveyResponseDeleteRequest(httpRequest);
		}
		else if(API_SURVEY_RESPONSE_FUNCTION_READ.equals(requestUri)) {
			return new SurveyResponseFunctionReadRequest(httpRequest);
		}
		// User
		else if(API_USER_CREATE.equals(requestUri)) {
			return new UserCreationRequest(httpRequest);
		}
		else if(API_USER_REGISTER.equals(requestUri)) {
			return new UserRegistrationRequest(httpRequest);
		}
		else if(API_USER_ACTIVATE.equals(requestUri)) {
			return new UserActivationRequest(httpRequest);
		}
		else if(API_USER_PASSWORD_RESET.equals(requestUri)) {
			return new UserPasswordResetRequest(httpRequest);
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
		else if(API_USER_SEARCH.equals(requestUri)) {
			return new UserSearchRequest(httpRequest);
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
		// Registration
		else if(API_REGISTRATION_READ.equals(requestUri)) {
			return new RegistrationReadRequest(httpRequest);
		}
		// Visualization
		else if(API_VISUALIZATION_SURVEY_RESPONSE_COUNT.equals(requestUri)) {
			return new VizSurveyResponseCountRequest(httpRequest);
		}
		else if(API_VISUALIZATION_PROMPT_DISTRIBUTION.equals(requestUri)) {
			return new VizPromptDistributionRequest(httpRequest);
		}
		else if(API_VISUALIZATION_PROMPT_TIMESERIES.equals(requestUri)) {
			return new VizPromptTimeseriesRequest(httpRequest);
		}
		else if(API_VISUALIZATION_USER_TIMESERIES.equals(requestUri)) {
			return new VizUserTimeseriesRequest(httpRequest);
		}
		else if(API_VISUALIZATION_SCATTER_PLOT.equals(requestUri)) {
			return new VizScatterPlotRequest(httpRequest);
		}
		else if(API_VISUALIZATION_2D_DENSITY.equals(requestUri)) {
			return new VizTwoDDensityRequest(httpRequest);
		}
		else if(API_VISUALIZATION_SURVEY_RESPONSE_PRIVACY.equals(requestUri)) {
			return new VizSurveyResponsePrivacyStateRequest(httpRequest);
		}
		else if(API_VISUALIZATION_SURVEY_RESPONSE_PRIVACY_TIMESERIES.equals(requestUri)) {
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
	public static boolean knownUri(String uri) {
		if(
				// Config
				API_CONFIG_READ.equals(uri) ||
				// Annotation
				API_ANNOTATION_PROMPT_RESPONSE_CREATE.equals(uri) ||
				API_ANNOTATION_PROMPT_RESPONSE_READ.equals(uri) ||
				API_ANNOTATION_SURVEY_RESPONSE_CREATE.equals(uri) ||
				API_ANNOTATION_SURVEY_RESPONSE_READ.equals(uri) ||
				// Authentication
				API_USER_AUTH.equals(uri) ||
				API_USER_AUTH_TOKEN.equals(uri) ||
				API_USER_LOGOUT.equals(uri) ||
				// Audit
				API_AUDIT_READ.equals(uri) ||
				// Campaign
				API_CAMPAIGN_CREATE.equals(uri) ||
				API_CAMPAIGN_READ.equals(uri) ||
				API_CAMPAIGN_SEARCH.equals(uri) ||
				API_CAMPAIGN_UPDATE.equals(uri) ||
				API_CAMPAIGN_DELETE.equals(uri) ||
				// Class
				API_CLASS_CREATE.equals(uri) ||
				API_CLASS_READ.equals(uri) ||
				API_CLASS_ROSTER_READ.equals(uri) ||
				API_CLASS_SEARCH.equals(uri) ||
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
				API_IMAGE_BATCH_ZIP_READ.equals(uri) ||
				// Mobility
				API_MOBILITY_UPLOAD.equals(uri) ||
				API_MOBILITY_READ.equals(uri) ||
				API_MOBILITY_READ_CHUNKED.equals(uri) ||
				API_MOBILITY_AGGREGATE_READ.equals(uri) ||
				API_MOBILITY_DATES_READ.equals(uri) ||
				API_MOBILITY_READ_CSV.equals(uri) ||
				API_MOBILITY_UPDATE.equals(uri) ||
				// Observer
				API_OBSERVER_CREATE.equals(uri) ||
				uri.startsWith(API_OBSERVER_UPLOAD) ||
				// Survey
				API_SURVEY_UPLOAD.equals(uri) ||
				API_SURVEY_RESPONSE_READ.equals(uri) ||
				API_SURVEY_RESPONSE_UPDATE.equals(uri) ||
				API_SURVEY_RESPONSE_DELETE.equals(uri) ||
				API_SURVEY_RESPONSE_FUNCTION_READ.equals(uri) ||
				// User
				API_USER_CREATE.equals(uri) ||
				API_USER_REGISTER.equals(uri) ||
				API_USER_ACTIVATE.equals(uri) ||
				API_USER_PASSWORD_RESET.equals(uri) ||
				API_USER_READ.equals(uri) ||
				API_USER_INFO_READ.equals(uri) ||
				API_USER_STATS_READ.equals(uri) ||
				API_USER_SEARCH.equals(uri) ||
				API_USER_UPDATE.equals(uri) ||
				API_USER_CHANGE_PASSWORD.equals(uri) ||
				API_USER_DELETE.equals(uri) ||
				// Registration
				API_REGISTRATION_READ.equals(uri) ||
				// Visualization
				API_VISUALIZATION_SURVEY_RESPONSE_COUNT.equals(uri) ||
				API_VISUALIZATION_PROMPT_DISTRIBUTION.equals(uri) ||
				API_VISUALIZATION_PROMPT_TIMESERIES.equals(uri) ||
				API_VISUALIZATION_USER_TIMESERIES.equals(uri) ||
				API_VISUALIZATION_SCATTER_PLOT.equals(uri) ||
				API_VISUALIZATION_2D_DENSITY.equals(uri) ||
				API_VISUALIZATION_SURVEY_RESPONSE_PRIVACY.equals(uri) ||
				API_VISUALIZATION_SURVEY_RESPONSE_PRIVACY_TIMESERIES.equals(uri)) {
			return true;
		}
		
		// The URI is unknown.
		return false;
	}
}
