package org.ohmage.domain;

/**
 * This is a superclass for all of the types of payload IDs.
 *
 * @author John Jenkins
 */
public abstract interface PayloadId {
	/**
	 * Returns the root ID for this payload. For example, Campaign-based 
	 * payload IDs will return the Campaign ID; Observer-based payload IDs will
	 * return the Observer ID.
	 * 
	 * @return The root ID for this payload.
	 */
	String getId();
	
	/**
	 * Returns the sub-ID for this payload or null if one doesn't exist. For 
	 * example, Campaign-based payload IDs may return a survey ID or a prompt
	 * ID; Observer-based payload IDs may return a Stream ID.
	 *  
	 * @return The sub-ID for this payload.
	 */
	String getSubId();
}
