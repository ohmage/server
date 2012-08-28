package org.ohmage.domain;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;

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
	
	/**
	 * Creates a UserRequest object from the given parameters.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters already decoded from the HTTP request.
	 * 
	 * @param hashPassword If null, a username and password are not allowed for
	 * 					   this request. If true, the password will be hashed
	 * 					   before it is used.
	 * 
	 * @param tokenLocation If null, an authentication token is not allowed for
	 * 						this request. Otherwise, it describes where to look
	 * 						for the token.
	 * 
	 * @param version The version of the payload from which to pull data.
	 * 
	 * @param startDate Limits the data to only those points on or after this
	 * 					date.
	 * 
	 * @param endDate Limits the data to only those points on or before this
	 * 				  date.
	 * 
	 * @param columns Limits the data by only outputting the data that is
	 * 				  specified.
	 * 
	 * @param numToSkip The number of points to skip.
	 * 
	 * @param numToReturn The number of points to return.
	 * 
	 * @return The UserRequest object.
	 * 
	 * @throws DomainException There was an error building the request.
	 */
	UserRequest generateSubRequest(
		HttpServletRequest httpRequest,
		Map<String, String[]> parameters,
		Boolean hashPassword,
		TokenLocation tokenLocation,
		String client,
		long version,
		DateTime startDate,
		DateTime endDate,
		ColumnNode<String> columns,
		long numToSkip,
		long numToReturn)
		throws DomainException;
}
