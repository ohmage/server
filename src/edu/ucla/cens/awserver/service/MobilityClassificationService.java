package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.MobilitySensorDataPacket;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.mobilityclassifier.Classification;
import edu.ucla.cens.mobilityclassifier.MobilityClassifier;

/**
 * @author selsky
 */
public class MobilityClassificationService implements Service {
	private static Logger _logger = Logger.getLogger(MobilityClassificationService.class);
	private MobilityClassifier _classifier;
	
	public MobilityClassificationService(MobilityClassifier classifier) {
		if(null == classifier) {
			throw new IllegalArgumentException("the MobilityClassifier cannot be null");
		}
		_classifier = classifier;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		List<DataPacket> dataPackets = awRequest.getDataPackets();
		
		for(DataPacket dataPacket : dataPackets) {
			if(dataPacket instanceof MobilitySensorDataPacket) { // most likely there will only be one subtype per upload, but
				                                                 // the subtypes could be mixed
				try {
					
					MobilitySensorDataPacket mdp = (MobilitySensorDataPacket) dataPacket;
					Classification c = _classifier.classify(mdp.getSamples(), mdp.getSpeed());
					
					JSONObject o = new JSONObject();
					
					if(c.hasFeatures()) {
						
						Double average = c.getAverage();
						Double n95Variance = c.getN95Variance();
						Double variance = c.getVariance();
						List<Double> fft = c.getFft();
						List<Double> n95Fft = c.getN95Fft();
						
						o.put("average", average);
						o.put("variance", variance);
						o.put("N95Variance", n95Variance);
						o.put("fft", fft);
						o.put("N95Fft", n95Fft);
							
					} else {
						
						if(_logger.isDebugEnabled()) { // this will be very verbose for large messages/uploads!
							_logger.debug("no features calculated by mobility classifier for user " + awRequest.getUser().getUserName());
						}
					}
					
					o.put("mode", c.getMode());
					mdp.setFeatures(o.toString());
					mdp.setClassifierVersion(MobilityClassifier.getVersion());
			
				} catch (JSONException jsone) { // if this happens, it means the o.put() calls above failed. missing data in the 
					                            // Classification object?
				
					_logger.error("failed to build JSON Object using mobility features data", jsone);
				}
			}
		}
	}
}
