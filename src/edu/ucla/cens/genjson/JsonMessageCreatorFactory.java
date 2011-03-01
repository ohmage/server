package edu.ucla.cens.genjson;

/**
 * Factory for creating JsonMessageCreators.
 * 
 * TODO change to an enum factory?
 * 
 * Check out the AndWellnessDataGenerator project for components that create survey messages.
 * 
 * @author selsky
 */
public class JsonMessageCreatorFactory {
	
	private JsonMessageCreatorFactory() { };
	
	
    /**
     * Returns a new JsonMessageCreator based on messageCreatorName. 
     * 
     * @throws IllegalArgumentException if an unknown messageCreatorName is provided 
     */
	public static JsonMessageCreator make(String messageCreatorName) {
		
		if("mobility:mode_only".equals(messageCreatorName)) {
			
			return new MobilityModeOnlyJsonMessageCreator();
			
		} else if ("mobility:mode_extended".equals(messageCreatorName)){
			
			return new MobilityModeExtendedJsonMessageCreator();

		} else {
			
			throw new IllegalArgumentException("cannot create JSON messages, invalid message type: " + messageCreatorName);
		}
	}
}
