package org.ohmage.request.clazz;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.exception.ServiceException;
import org.ohmage.exception.ValidationException;
import org.ohmage.request.InputKeys;
import org.ohmage.request.UserRequest;
import org.ohmage.service.CampaignClassServices;
import org.ohmage.service.ClassServices;
import org.ohmage.service.UserClassServices;
import org.ohmage.service.UserServices;

/**
 * Searches through the list of classes to find the ones that match the given
 * criteria.
 * 
 * @author John Jenkins
 */
public class ClassSearchRequest extends UserRequest {
	private static final Logger LOGGER = Logger.getLogger(ClassSearchRequest.class);
	
	private final String classId;
	private final String className;
	private final String classDescription;
	
	private final Map<Clazz, Collection<String>> classToUsernamesMap;
	private final Map<Clazz, Collection<String>> classToCampaignIdsMap;
	
	/**
	 * Builds this request based on the information in the HTTP request.
	 * 
	 * @param httpRequest A HttpServletRequest object that contains the
	 * 					  parameters to and metadata for this request.
	 */
	public ClassSearchRequest(final HttpServletRequest httpRequest) {
		super(httpRequest, TokenLocation.PARAMETER);
		
		String tClassId = null;
		String tClassName = null;
		String tClassDescription = null;
		
		if(! isFailed()) {
			LOGGER.info("Creating a class search request.");
			
			String[] t = null;
			try {
				t = getParameterValues(InputKeys.CLASS_URN);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_ID,
							"Multiple class IDs were given: " +
								InputKeys.CLASS_URN);
				}
				else if(t.length == 1) {
					tClassId = t[0];
				}
				
				t = getParameterValues(InputKeys.CLASS_NAME);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_NAME,
							"Multiple class names were given: " + 
								InputKeys.CLASS_NAME);
				}
				else if(t.length == 1) {
					tClassName = t[0];
				}
				
				t = getParameterValues(InputKeys.DESCRIPTION);
				if(t.length > 1) {
					throw new ValidationException(
							ErrorCode.CLASS_INVALID_DESCRIPTION,
							"Multiple class descriptions were given: " +
								InputKeys.DESCRIPTION);
				}
				else if(t.length == 1) {
					tClassDescription = t[0];
				}
			}
			catch(ValidationException e) {
				e.failRequest(this);
				e.logException(LOGGER);
			}
		}
		
		classId = tClassId;
		className = tClassName;
		classDescription = tClassDescription;
		
		classToUsernamesMap = new HashMap<Clazz, Collection<String>>();
		classToCampaignIdsMap = new HashMap<Clazz, Collection<String>>();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing the class search request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}

		try {
			LOGGER.info("Checking that the user is an admin.");
			UserServices.instance().verifyUserIsAdmin(getUser().getUsername());
			
			LOGGER.info("Searching for the classes that satisfy the parameters.");
			Set<String> classIds = 
				ClassServices.instance().
					classIdSearch(classId, className, classDescription);
			
			LOGGER.info("Gathering the detailed information about the classes.");
			List<Clazz> classes =
				ClassServices.instance().getClassesInformation(classIds);
			
			LOGGER.info("Gathering the usernames for the users in each class.");
			for(Clazz clazz : classes) {
				classToUsernamesMap.put(
						clazz, 
						UserClassServices.instance().getUsersInClass(clazz.getId())
					);
			}
			
			LOGGER.info("Gathering the IDs for the campaigns associated with each class.");
			for(Clazz clazz : classes) {
				classToCampaignIdsMap.put(
						clazz, 
						CampaignClassServices.instance().getCampaignIdsForClass(
								clazz.getId())
							);
			}
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#respond(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public void respond(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
		JSONObject result = new JSONObject();
		
		try {
			for(Clazz clazz : classToUsernamesMap.keySet()) {
				JSONObject classJson = clazz.toJson(false);
				
				classJson.put("usernames", classToUsernamesMap.get(clazz));
				classJson.put("campaign_ids", classToCampaignIdsMap.get(clazz));
				
				result.put(clazz.getId(), classJson);
			}
		}
		catch(JSONException e) {
			LOGGER.error("There was an error building the result.", e);
			setFailed();
		}
		
		super.respond(httpRequest, httpResponse, result);
	}
}