package org.ohmage.request;

import javax.servlet.http.HttpServletRequest;

import org.ohmage.request.auth.AuthRequest;
import org.ohmage.request.auth.AuthTokenRequest;
import org.ohmage.request.campaign.CampaignCreationRequest;
import org.ohmage.request.clazz.ClassCreationRequest;
import org.ohmage.request.clazz.ClassDeletionRequest;
import org.ohmage.request.clazz.ClassReadRequest;
import org.ohmage.request.clazz.ClassUpdateRequest;
import org.ohmage.request.document.DocumentCreationRequest;
import org.ohmage.request.document.DocumentReadContentsRequest;
import org.ohmage.request.document.DocumentReadRequest;
import org.ohmage.request.document.DocumentUpdateRequest;

/**
 * Request builder from an HTTP request.
 * 
 * @author John Jenkins
 */
public final class RequestBuilder {
	/**
	 * Default constructor. Made private because this class should never be
	 * instantiated. Instead, the static builder methods should be called.
	 */
	private RequestBuilder() {}
	
	private static final String API_ROOT = "/app";
	
	// Config
	private static final String API_CONFIG_READ = API_ROOT + "/config/read";
	
	// Authentication
	private static final String API_USER_AUTH = API_ROOT + "/user/auth";
	private static final String API_USER_AUTH_TOKEN = API_ROOT + "/user/auth_token";
	
	// Campaign
	private static final String API_CAMPAIGN_CREATE = API_ROOT + "/campaign/create";
	
	// Class
	private static final String API_CLASS_CREATE = API_ROOT + "/class/create";
	private static final String API_CLASS_READ = API_ROOT + "/class/read";
	private static final String API_CLASS_UPDATE = API_ROOT + "/class/update";
	private static final String API_CLASS_DELETE = API_ROOT + "/class/delete";
	
	// Document
	private static final String API_DOCUMENT_CREATE = API_ROOT + "/document/create";
	private static final String API_DOCUMENT_READ = API_ROOT + "/document/read";
	private static final String API_DOCUMENT_READ_CONTENTS = API_ROOT + "/document/read/contents";
	private static final String API_DOCUMENT_UPDATE = API_ROOT + "/document/update";
	
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
		// Campaign
		else if(API_CAMPAIGN_CREATE.equals(requestUri)) {
			return new CampaignCreationRequest(httpRequest);
		}
		// Class
		else if(API_CLASS_CREATE.equals(requestUri)) {
			return new ClassCreationRequest(httpRequest);
		}
		else if(API_CLASS_READ.equals(requestUri)) {
			return new ClassReadRequest(httpRequest);
		}
		else if(API_CLASS_UPDATE.equals(requestUri)) {
			return new ClassUpdateRequest(httpRequest);
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
		
		// The URI is unknown.
		return new FailedRequest();
	}
}