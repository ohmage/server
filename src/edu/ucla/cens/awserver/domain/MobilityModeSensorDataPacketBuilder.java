package edu.ucla.cens.awserver.domain;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.JsonUtils;
import edu.ucla.cens.mobilityclassifier.Sample;

/**
 * @author selsky
 */
public class MobilityModeSensorDataPacketBuilder extends AbstractDataPacketBuilder {

	public MobilityModeSensorDataPacketBuilder() {
		
	}
	
	public MetadataDataPacket createDataPacketFrom(JSONObject source, AwRequest awRequest) {
		MobilitySensorDataPacket packet = new MobilitySensorDataPacket();
		createCommonFields(source, packet);
		JSONObject dataObject = JsonUtils.getJsonObjectFromJsonObject(source, "data");
		packet.setSensorDataString(dataObject.toString());
		packet.setSpeed(JsonUtils.getDoubleFromJsonObject(dataObject, "speed"));
		packet.setMode(JsonUtils.getStringFromJsonObject(dataObject, "mode"));
		
		JSONArray array = JsonUtils.getJsonArrayFromJsonObject(dataObject, "accel_data");
		List<Sample> sampleList = new ArrayList<Sample>();
		int arraySize = array.length();
		for(int i = 0; i < arraySize; i++) {
			JSONObject sample = JsonUtils.getJsonObjectFromJsonArray(array, i);
			Double x = JsonUtils.getDoubleFromJsonObject(sample, "x");
			Double y = JsonUtils.getDoubleFromJsonObject(sample, "y");
			Double z = JsonUtils.getDoubleFromJsonObject(sample, "z");
			Sample s = new Sample();
			s.setX(x);
			s.setY(y);
			s.setZ(z);
			sampleList.add(s);
		}
		packet.setSamples(sampleList);
		
		return packet;
	}
}
