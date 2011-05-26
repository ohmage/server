package edu.ucla.cens.awserver.service;

import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.UserInfo;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserReadRequest;

/**
 * Gets the information for all the users in all the campaigns and classes in
 * their respective lists and creates a JSONObject that is returned in the
 * request to be returned to the user.
 * 
 * @author John Jenkins
 */
public class UserReadService extends AbstractDaoService {
	public static Logger _logger = Logger.getLogger(UserReadService.class);
	
	/**
	 * Creates this service.
	 * 
	 * @param dao DAO to run to get the list of user information.
	 */
	public UserReadService(Dao dao) {
		super(dao);
	}
	
	/**
	 * Runs the DAO to get the list of UserInfo objects, then adds them all to
	 * a JSONObject and places the resulting JSONObject in the request to be
	 * returned to the user.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Getting the information about all the users in the campaigns and/or classes.");
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		JSONObject result = new JSONObject();
		ListIterator<?> userInfoListIter = awRequest.getResultList().listIterator();
		while(userInfoListIter.hasNext()) {
			UserInfo userInfo = (UserInfo) userInfoListIter.next();
			
			try {
				result.put(userInfo.getUsername(), userInfo.toJsonObject(false));
			}
			catch(JSONException e) {
				_logger.warn("Error while building JSONObject for a user's information.", e);
			}
		}
		
		awRequest.addToReturn(UserReadRequest.RESULT, result, true);
	}

}
