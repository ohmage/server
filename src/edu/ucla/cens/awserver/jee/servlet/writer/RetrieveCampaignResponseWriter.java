package edu.ucla.cens.awserver.jee.servlet.writer;

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

import edu.ucla.cens.awserver.domain.CampaignQueryResult;
import edu.ucla.cens.awserver.domain.CampaignUrnClassUrn;
import edu.ucla.cens.awserver.domain.CampaignUrnLoginIdUserRole;
import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.RetrieveCampaignAwRequest;

/**
 * @author selsky
 */
public class RetrieveCampaignResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(RetrieveCampaignResponseWriter.class);
	
	public RetrieveCampaignResponseWriter(ErrorResponse errorResponse) {
		super(errorResponse);
	}
	
	@Override
	public void write(HttpServletRequest request, HttpServletResponse response, AwRequest awRequest) {
		Writer writer = null;
		
		try {
			// Prepare for sending the response to the client
			writer = new BufferedWriter(new OutputStreamWriter(getOutputStream(request, response)));
			String responseText = null;
			
			// Sets the HTTP headers to disable caching
			expireResponse(response);
			
			response.setContentType("application/json");
			
			// Build the appropriate response 
			if(! awRequest.isFailedRequest()) {
				
				RetrieveCampaignAwRequest req = (RetrieveCampaignAwRequest) awRequest;
				
				@SuppressWarnings("unchecked")
				List<CampaignQueryResult> results = (List<CampaignQueryResult>) awRequest.getResultList();
				int numberOfResults = results.size();
				
				JSONObject rootObject = new JSONObject().put("result", "success");
				JSONObject metadata = new JSONObject();
				metadata.put("number_of_results", numberOfResults);
				rootObject.put("metadata", metadata);
				
				JSONArray itemArray = new JSONArray();
				metadata.put("items", itemArray);
				JSONObject dataArray = new JSONObject();
				rootObject.put("data", dataArray);
				
				for(int i = 0; i < numberOfResults; i++) {
					CampaignQueryResult result = results.get(i);
					JSONObject campaignObject = new JSONObject();
					campaignObject.put("name", result.getName());
					campaignObject.put("running_state", result.getRunningState());
					campaignObject.put("privacy_state", result.getPrivacyState());
					campaignObject.put("creation_timestamp", result.getCreationTimestamp());
					campaignObject.put("user_roles", new JSONArray(result.getUserRoles()));
					
					
					if("long".equals(req.getOutputFormat())) {
						campaignObject.put("xml", result.getXml().replaceAll("\\n",""));
						campaignObject.put("classes", new JSONArray(generateClassList(req, result.getUrn())));
						campaignObject.put("user_role_campaign", new JSONObject(generateUserRoleCampaign(req, result.getUrn())));
					}
					
					dataArray.put(result.getUrn(), campaignObject);
					itemArray.put(result.getUrn());
				}
				
				responseText = rootObject.toString();
				
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
	
	private List<String> generateClassList(RetrieveCampaignAwRequest req, String campaignUrn) {
		
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
	
	private Map<String, List<String>> generateUserRoleCampaign(RetrieveCampaignAwRequest req, String campaignUrn) {
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
