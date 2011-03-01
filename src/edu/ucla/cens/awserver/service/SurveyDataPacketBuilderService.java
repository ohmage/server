package edu.ucla.cens.awserver.service;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONArray;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.DataPacketBuilder;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;

/**
 * @author selsky
 */
public class SurveyDataPacketBuilderService implements Service {
	private static Logger _logger = Logger.getLogger(SurveyDataPacketBuilderService.class);
	private DataPacketBuilder _builder;
	
	/**
	 * @throws IllegalArgumentException if the provided Map is null or empty
	 */
	public SurveyDataPacketBuilderService(DataPacketBuilder builder) {
		if(null == builder) {
			throw new IllegalArgumentException("a builder is required");
		}
		_builder = builder;
	}
	
	/**
	 * Creates DataPackets for uploaded surveys using the builder set on construction. The DataPackets are then set on the 
	 * AwRequest.
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning to build data packets for db insertion using builder " + _builder.getClass());
		JSONArray jsonArray = awRequest.getJsonDataAsJsonArray();
		int length = jsonArray.length();
		List<DataPacket> dataPackets = new ArrayList<DataPacket>(length);
		 
		for(int i = 0; i < length; i++) {
			dataPackets.add(_builder.createDataPacketFrom(JsonUtils.getJsonObjectFromJsonArray(jsonArray, i), awRequest));
		}
		
		awRequest.setDataPackets(dataPackets);
		_logger.info("finished building data packets using builder " + _builder.getClass());
	}
}
