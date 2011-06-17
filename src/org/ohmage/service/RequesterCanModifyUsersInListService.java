package org.ohmage.service;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks if the requester has sufficient permission to modify the details of
 * all the users in a user list. Acceptable reasons are:
 * 
 * - The list is empty.
 * - The list only contains one entry and it is the requesting user's username.
 * - The requester is an admin.
 * 
 * @author John Jenkins
 */
public class RequesterCanModifyUsersInListService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(RequesterCanModifyUsersInListService.class);
	
	private final String _key;
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to use should the requester not have
	 * 					permissions to modify the users in the list.
	 * 
	 * @param dao The DAO to call to detect if the user is an admin.
	 * 
	 * @param key The key to us to get the list of users from the request.
	 */
	public RequesterCanModifyUsersInListService(AwRequestAnnotator annotator, Dao dao, String key) {
		super(dao, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
	}

	/**
	 * Checks that the list of users are all users that the requesting user has
	 * permissions to modify. This is true iff the list is empty, the list only
	 * contains one username and that's the requester's username, and/or the
	 * requester is an admin. 
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Ensuring that the user can modify the users in the list.");
		
		String userListString;
		try {
			userListString = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			_logger.error("Missing the required user list: " + _key);
			throw new ServiceException(e);
		}

		// If it is an empty string, there is no problem.
		if("".equals(userListString)) {
			return;
		}
		
		// If the only user is the requester, there is no problem.
		String[] userList = userListString.split(InputKeys.LIST_ITEM_SEPARATOR);
		if((userList.length == 1) && (userList[0].equals(awRequest.getUser().getUserName()))) {
			return;
		}
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		try {
			if(! (Boolean) awRequest.getResultList().get(0)) {
				getAnnotator().annotate(awRequest, "The user has username(s) in the request, but it doesn't have sufficient permissions to modify them.");
				awRequest.setFailedRequest(true);
				return;
			}
		}
		catch(NullPointerException e) {
			_logger.error("The DAO returned without error, but the result list was never set.");
			throw new ServiceException(e);
		}
		catch(IndexOutOfBoundsException e) {
			_logger.error("The DAO returned without error, but the result list was empty.");
			throw new ServiceException(e);
		}
		catch(ClassCastException e) {
			_logger.error("The DAO returned without error, but the result list's value was not of expected type Boolean.");
			throw new ServiceException(e);
		}
	}
}