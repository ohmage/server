package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UserInfo;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

/**
 * Gets the union of all the users in the campaigns and classes, gets the
 * information about each of the users, and returns it as the result list in 
 * the request.
 * 
 * @author John Jenkins
 */
public class UserReadDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserReadDao.class);
	
	private static final String SQL_GET_USERS_FROM_CAMPAIGN = "SELECT u.login_id " +
															  "FROM user u, campaign c, user_role_campaign urc " +
															  "WHERE c.urn = ? " +
															  "AND c.id = urc.campaign_id " +
															  "AND u.id = urc.user_id";
	
	private static final String SQL_GET_USERS_FROM_CLASS = "SELECT u.login_id " +
														   "FROM user u, class c, user_class uc " +
														   "WHERE c.urn = ? " +
														   "AND c.id = uc.class_id " +
														   "AND u.id = uc.user_id";
	
	private static final String SQL_GET_USER_INFORMATION = "SELECT up.first_name, up.last_name, up.organization, up.personal_id, up.email_address, up.json_data " +
														   "FROM user u, user_personal up " +
														   "WHERE u.login_id = ? " +
														   "AND u.id = up.user_id";
	
	/**
	 * Creates this DAO.
	 * 
	 * @param dataSource The DataSource to use to query the database.
	 */
	public UserReadDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Gets all the users from all the classes and campaigns, gets the  
	 * information about each of the users, and stores it as the result set for
	 * the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// The list of users whose information want.
		final Set<String> users = new HashSet<String>();
		
		// Get the list of campaigns.
		String campaignIdList;
		try {
			campaignIdList = (String) awRequest.getToProcessValue(InputKeys.CAMPAIGN_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			campaignIdList = "";
		}
		
		// If the list isn't empty, parse it and get all the users from all the
		// campaigns.
		if(! "".equals(campaignIdList)) {
			String[] campaignIdArray = campaignIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
			for(int i = 0; i < campaignIdArray.length; i++) {
				try {
					getJdbcTemplate().query(
							SQL_GET_USERS_FROM_CAMPAIGN, 
							new Object[] { campaignIdArray[i] }, 
							new RowMapper() {
								@Override
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									users.add(rs.getString("login_id"));
									return null;
								}
							}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USERS_FROM_CAMPAIGN + "' with parameter: " + campaignIdArray[i], e);
					throw new DataAccessException(e);
				}
			}
		}
		
		// Get the list of classes.
		String classIdList;
		try {
			classIdList = (String) awRequest.getToProcessValue(InputKeys.CLASS_URN_LIST);
		}
		catch(IllegalArgumentException e) {
			classIdList = "";
		}
		
		// If the list isn't empty, parse it and get all the users from all the
		// classes.
		if(! "".equals(classIdList)) {
			String[] classIdArray = classIdList.split(InputKeys.LIST_ITEM_SEPARATOR);
			for(int i = 0; i < classIdArray.length; i++) {
				try {
					getJdbcTemplate().query(
							SQL_GET_USERS_FROM_CLASS, 
							new Object[] { classIdArray[i] }, 
							new RowMapper() {
								@Override
								public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
									users.add(rs.getString("login_id"));
									return null;
								}
							}
					);
				}
				catch(org.springframework.dao.DataAccessException e) {
					_logger.error("Error executing SQL '" + SQL_GET_USERS_FROM_CLASS + "' with parameter: " + classIdArray[i], e);
					throw new DataAccessException(e);
				}
			}
		}
		
		// The result list to be returned from this DAO.
		final List<UserInfo> result = new LinkedList<UserInfo>();
		
		// Go through the user list and get the information about each of the
		// users.
		for(final String user : users) {
			try {
				getJdbcTemplate().query(
						SQL_GET_USER_INFORMATION, 
						new Object[] { user }, 
						new RowMapper() {
							@Override
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								try {
									result.add(new UserInfo(user, 
															rs.getString("first_name"),
															rs.getString("last_name"),
															rs.getString("organization"),
															rs.getString("personal_id"),
															rs.getString("email_address"),
															new JSONObject(rs.getString("json_data"))));
								}
								catch(JSONException e) {
									throw new SQLException("Failed to create JSONObject from JSONObject in the database.", e);
								}
								
								return null;
							}
						}
				);
			}
			catch(org.springframework.dao.DataAccessException e) {
				_logger.error("Error executing SQL '" + SQL_GET_USER_INFORMATION + "' with parameter: " + user);
				throw new DataAccessException(e);
			}
		}
		
		awRequest.setResultList(result);
	}
}