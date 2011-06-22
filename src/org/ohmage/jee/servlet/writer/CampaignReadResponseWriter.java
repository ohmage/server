/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.ohmage.domain.CampaignQueryResult;
import org.ohmage.domain.CampaignUrnClassUrn;
import org.ohmage.domain.CampaignUrnLoginIdUserRole;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.CampaignReadAwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.CookieUtils;


/**
 * @author selsky
 */
public class CampaignReadResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(CampaignReadResponseWriter.class);
	
	public CampaignReadResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = "";
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				
				CampaignReadAwRequest req = (CampaignReadAwRequest) awRequest;
				
				@SuppressWarnings("unchecked")
				List<CampaignQueryResult> results = (List<CampaignQueryResult>) awRequest.getResultList();
				int numberOfResults = results.size();
				
				JSONObject rootObject = new JSONObject().put("result", "success");
				JSONArray itemArray = new JSONArray();
				JSONObject dataArray = new JSONObject();
				
				if(! "xml".equals(req.getOutputFormat())) {
					JSONObject metadata = new JSONObject();
					metadata.put("number_of_results", numberOfResults);
					rootObject.put("metadata", metadata);
					metadata.put("items", itemArray);
					rootObject.put("data", dataArray);
				}
					
				for(int i = 0; i < numberOfResults; i++) {
					CampaignQueryResult result = results.get(i);
					JSONObject campaignObject = new JSONObject();
					
					if(! "xml".equals(req.getOutputFormat())) {						
						campaignObject.put("name", result.getName());
						campaignObject.put("running_state", result.getRunningState());
						campaignObject.put("privacy_state", result.getPrivacyState());
						campaignObject.put("creation_timestamp", result.getCreationTimestamp());
						campaignObject.put("user_roles", new JSONArray(result.getUserRoles()));
						campaignObject.put("description", result.getDescription());
					}
					
					if("long".equals(req.getOutputFormat())) {
						campaignObject.put("xml", result.getXml().replaceAll("\\n",""));
						campaignObject.put("classes", new JSONArray(generateClassList(req, result.getUrn())));
						campaignObject.put("user_role_campaign", new JSONObject(generateUserRoleCampaign(req, result.getUrn())));
					}
					
					if(! "xml".equals(req.getOutputFormat())) {
						dataArray.put(result.getUrn(), campaignObject);
						itemArray.put(result.getUrn());
					} else {
						response.setHeader("Content-Disposition", "attachment; filename=" + result.getName() + ".xml");
						response.setContentType("text/xml");
						responseText = result.getXml();
					}
				}

				if(! "xml".equals(req.getOutputFormat())) {
					responseText = rootObject.toString();
				}

				CookieUtils.setCookieValue(response, InputKeys.AUTH_TOKEN, awRequest.getUserToken(), AUTH_TOKEN_COOKIE_LIFETIME_IN_SECONDS);
				
			} else {
				
				if(null != awRequest.getFailedRequestErrorMessage()) {
					responseText = awRequest.getFailedRequestErrorMessage();
				} else {
					responseText = generalJsonErrorMessage();
				}
			}
			
			_logger.info("about to write output");
			writer.write(responseText);
		}
		
		catch(Exception e) { // catch Exception in order to avoid redundant catch block functionality
			
			_logger.error("an unrecoverable exception occurred while running an retrieve config query", e);
			
			try {
				
				writer.write(this.generalJsonErrorMessage());
				
			} catch (Exception ee) {
				
				_logger.error("caught Exception when attempting to write to HTTP output stream", ee);
			}
			
		} finally {
			
			if(null != writer) {
				
				try {
					
					writer.flush();
					writer.close();
					writer = null;
					
				} catch (IOException ioe) {
					
					_logger.error("caught IOException when attempting to free resources", ioe);
				}
			}
		}
	}
	
	private List<String> generateClassList(CampaignReadAwRequest req, String campaignUrn) {
		
		if(! req.getClassUrnList().isEmpty()) {
			return req.getClassUrnList();
		} 
		
		List<CampaignUrnClassUrn> list = req.getCampaignUrnClassUrnList();
		List<String> out = new ArrayList<String>();
		for(CampaignUrnClassUrn cc : list) {
			if(cc.getCampaignUrn().equals(campaignUrn)) {
				out.add(cc.getClassUrn());
			}
		}
		
		return out;
	}
	
	private Map<String, List<String>> generateUserRoleCampaign(CampaignReadAwRequest req, String campaignUrn) {
		if(req.getCampaignUrnLoginIdUserRoleList().isEmpty()) {
			return Collections.emptyMap();
		}
		
		Map<String, List<String>> out = new HashMap<String, List<String>>();
		out.put("author", new ArrayList<String>());
		out.put("analyst", new ArrayList<String>());
		out.put("supervisor", new ArrayList<String>());
		out.put("participant", new ArrayList<String>());
		
		List<CampaignUrnLoginIdUserRole> list = req.getCampaignUrnLoginIdUserRoleList();
		for(CampaignUrnLoginIdUserRole clu : list) {
			if(clu.getCampaignUrn().equals(campaignUrn)) {
				out.get(clu.getRole()).add(clu.getLoginId());
			}
		}
		
		return out;
	}	
}
