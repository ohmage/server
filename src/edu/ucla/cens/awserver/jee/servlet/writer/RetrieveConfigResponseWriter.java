package edu.ucla.cens.awserver.jee.servlet.writer;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.ConfigQueryResult;
import edu.ucla.cens.awserver.domain.ErrorResponse;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class RetrieveConfigResponseWriter extends AbstractResponseWriter {
	private static Logger _logger = Logger.getLogger(RetrieveConfigResponseWriter.class);
	private List<String> _dataPointApiSpecialIds;
	
	public RetrieveConfigResponseWriter(List<String> dataPointApiSpecialIds, ErrorResponse errorResponse) {
		super(errorResponse);
		if(null == dataPointApiSpecialIds) {
			throw new IllegalArgumentException("a list of data point API special ids is required. An empty list is allowed.");
		}
		if(dataPointApiSpecialIds.isEmpty()) {
			_logger.warn("no data point API special ids found");
		}
		_dataPointApiSpecialIds = dataPointApiSpecialIds;  
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

				JSONObject jsonObject 
					= new JSONObject().put("result", "success")
					                  .put("special_ids", new JSONArray(_dataPointApiSpecialIds));
				
				List<?> resultList = awRequest.getResultList();
				int numberOfResults = resultList.size();
				
				JSONArray campaignArray = new JSONArray();
				for(int i = 0; i < numberOfResults; i++) {
					ConfigQueryResult result = (ConfigQueryResult) resultList.get(i);
					JSONObject campaignObject = new JSONObject();
					campaignObject.put("urn", result.getCampaignUrn());
					campaignObject.put("user_role", result.getUserRole());
					campaignObject.put("user_list", new JSONArray(result.getUserList()));
					campaignObject.put("configuration", result.getXml());
//					
//					JSONArray configArray = new JSONArray();
//					Map<String, String> versionXmlMap = result.getXml();
//					Iterator<String> mapIterator = versionXmlMap.keySet().iterator();
//					while(mapIterator.hasNext()) {
//						String k = mapIterator.next();
//						configArray.put(new JSONObject().put("version", k).put("configuration", versionXmlMap.get(k)));
//					}
//					campaignObject.put("configurations", configArray);
					campaignArray.put(campaignObject);
				}
				
				jsonObject.put("campaigns", campaignArray);
				responseText = jsonObject.toString();
				
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
}
