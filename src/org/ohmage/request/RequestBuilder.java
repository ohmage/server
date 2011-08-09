package org.ohmage.request;

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
import org.ohmage.request.user.UserChangePasswordRequest;
import org.ohmage.request.user.UserCreationRequest;
import org.ohmage.request.user.UserDeletionRequest;
import org.ohmage.request.user.UserInfoReadRequest;
import org.ohmage.request.user.UserReadRequest;
import org.ohmage.request.user.UserStatsReadRequest;
import org.ohmage.request.user.UserUpdateRequest;

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
		// Config
		if(API_CONFIG_READ.equals(uri) ||
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
}