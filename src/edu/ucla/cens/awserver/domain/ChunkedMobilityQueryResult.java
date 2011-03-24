package edu.ucla.cens.awserver.domain;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author selsky
 */
public class ChunkedMobilityQueryResult extends MobilityQueryResult {
	private static Logger _logger = Logger.getLogger(ChunkedMobilityQueryResult.class);
	private long _duration;
	private List<MobilityQueryResult> _items;
	
	public ChunkedMobilityQueryResult() {
		_items = new ArrayList<MobilityQueryResult>();
	}
	
	public long getDuration() {
		return _duration;
	}
	
	public void setDuration(long duration) {
		_duration = duration;
	}
	
	public void addItem(MobilityQueryResult result) {
		_items.add(result);
	}
	
	public void calcModes() {
		if(! _items.isEmpty()) {
			int still = 0;
			int walk = 0;
			int run = 0;
			int bike = 0;
			int drive = 0;
			
			if(_logger.isDebugEnabled()) {
				_logger.debug("found mobility chunk with " + _items.size() + " items");
			}
			
			setLocation(_items.get(0).getLocation());
			setLocationStatus(_items.get(0).getLocationStatus());
			setTimezone(_items.get(0).getTimezone());
			
			for(MobilityQueryResult mqr : _items) {
				
				if("still".equals(mqr.getValue())) {
					still++;
				} else if("walk".equals(mqr.getValue())) {
					walk++;
				} else if("run".equals(mqr.getValue())) {
					run++;
				} else if("bike".equals(mqr.getValue())) {
					bike++;
				} else if("drive".equals(mqr.getValue())) {
					drive++;
				} else {
					_logger.warn("unknown mobility mode found: " + mqr.getValue());
				}
			}
			
			JSONObject o = new JSONObject();
			
			try {
				o.put("still", still);
				o.put("walk", walk);
				o.put("run", run);
				o.put("bike", bike);
				o.put("drive", drive);
			} catch (JSONException jsone) { // can never happen unless there is a logical error in the try block
				_logger.error(jsone);
			}
			
			setValue(o);
		}
	}
}
