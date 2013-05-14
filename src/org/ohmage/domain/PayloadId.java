package org.ohmage.domain;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.joda.time.DateTime;
import org.ohmage.exception.DomainException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.UserRequest.TokenLocation;

/**
 * This is a superclass for all of the types of payload IDs.
 *
 * @author John Jenkins
 */
public abstract interface PayloadId {
	/**
	 * Creates a UserRequest object from the given parameters for reading data.
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
	 * @param callClientRequester Refers to the "client" parameter as the
	 * 							  "requester".
	 * 
	 * @param version The version of the payload from which to pull data.
	 * 
	 * @param owner The user whose data is being requested. If null, the 
	 * 				requester is requesting data about themselves.
	 * 
	 * @param startDate Limits the data to only those points on or after this
	 * 					date.
	 * 
	 * @param endDate Limits the data to only those points on or before this
	 * 				  date.
	 * 
	 * @param numToSkip The number of points to skip.
	 * 
	 * @param numToReturn The number of points to return.
	 * 
	 * @return The UserRequest object.
	 * 
	 * @throws DomainException There was an error building the request.
	 */
	public UserRequest generateReadRequest(
		HttpServletRequest httpRequest,
		Map<String, String[]> parameters,
		Boolean hashPassword,
		TokenLocation tokenLocation,
		boolean callClientRequester,
		long version,
		String owner,
		DateTime startDate,
		DateTime endDate,
		long numToSkip,
		long numToReturn)
		throws DomainException;
	
	/**
	 * Creates a UserRequest object from the given parameters for writing data.
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
	 * @param callClientRequester Refers to the "client" parameter as the
	 * 							  "requester".
	 * 
	 * @param version The version of the payload from which to pull data.
	 * 
	 * @param data The Open mHealth data being uploaded.
	 * 
	 * @return The UserRequest object.
	 * 
	 * @throws DomainException There was an error building the request.
	 */
	public UserRequest generateWriteRequest(
		HttpServletRequest httpRequest,
		Map<String, String[]> parameters,
		Boolean hashPassword,
		TokenLocation tokenLocation,
		boolean callClientRequester,
		long version,
		String data)
		throws DomainException;
}
