package edu.ucla.cens.genjson;

/**
 * Factory for creating JsonMessageCreators.
 * 
 * TODO change to an enum factory?
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
			
		} else if ("mobility:mode_features".equals(messageCreatorName)){
			
			return new MobilityModeFeaturesJsonMessageCreator();
			
		} else if ("prompt:0".equals(messageCreatorName)) {
			
			return new PromptGroupZeroJsonMessageCreator();
			
		} else if ("prompt:1".equals(messageCreatorName)) {
			
			return new PromptGroupOneJsonMessageCreator();
			
		} else if ("prompt:2".equals(messageCreatorName)) {
			
			return new PromptGroupTwoJsonMessageCreator();
			
		} else if ("prompt:3".equals(messageCreatorName)) {
			
			return new PromptGroupThreeJsonMessageCreator();
			
		} else {
			
			throw new IllegalArgumentException("cannot create JSON messages, invalid message type: " + messageCreatorName);
		}
		 
	}
	
}
