package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ListIterator;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.UserInfoQueryAwRequest;

/**
 * Aggregates all the information about a user.
 * 
 * @author John Jenkins
 */
public class UserInfoAggregationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserInfoAggregationDao.class);
	
	private static final String SQL_GET_USER_CREATION_PRIVILEGE = "SELECT campaign_creation_privilege " +
															   	  "FROM user u " +
															   	  "WHERE u.login_id = ?";
	
	private final static String SQL_GET_USER_CAMPAIGNS = "SELECT c.urn, c.name " +
														 "FROM user u, campaign c, user_role_campaign urc " +
														 "WHERE u.login_id = ? " +
														 "AND u.id = urc.user_id " +
														 "AND urc.campaign_id = c.id";
	
	private final static String SQL_GET_CAMPAIGN_ROLES = "SELECT distinct(ur.role) " +
														 "FROM user u, user_role ur, user_role_campaign urc " +
														 "WHERE u.login_id = ? " +
														 "AND u.id = urc.user_id " +
														 "AND urc.user_role_id = ur.id";
	
	private static final String SQL_GET_USER_CLASSES = "SELECT c.urn, c.name " +
	  												   "FROM user u, class c, user_class uc " +
	  												   "WHERE u.login_id = ? " +
	  												   "AND u.id = uc.user_id " +
	  												   "AND uc.class_id = c.id";
	
	private static final String SQL_GET_CLASS_ROLES = "SELECT distinct(ucr.role) " +
												      "FROM user u, user_class uc, user_class_role ucr " +
												      "WHERE u.login_id = ? " +
												      "AND u.id = uc.user_id " +
												      "AND uc.user_class_role_id = ucr.id";
	
	/**
	 * Used when retrieving an entity's name and the ID for that entity.
	 * 
	 * @author John Jenkins
	 */
	private class NameAndUrn {
		public String _name;
		public String _urn;
		
		/**
		 * Basic constructor for inlining.
		 * 
		 * @param name The name of the entity.
		 * 
		 * @param urn The URN of the entity.
		 */
		public NameAndUrn(String urn, String name) {
			_name = name;
			_urn = urn;
		}
	}
	
	/**
	 * Default constructor that sets this DAO's DataSource.
	 * 
	 * @param dataSource The DataSource that this object will use when running
	 * 					 its queries.
	 */
	public UserInfoAggregationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Executes the query by aggregating all the applicable information for
	 * each of the users in the request.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get permissions.
		JSONObject permissionsJson = new JSONObject();
		try {
			int canCreate = getJdbcTemplate().queryForInt(SQL_GET_USER_CREATION_PRIVILEGE, new Object[] { awRequest.getUser().getUserName() });
			permissionsJson.put("can_create_campaigns", canCreate == 1);	
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_USER_CREATION_PRIVILEGE + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		catch(JSONException e) {
			_logger.error("Problem creating 'permissions' JSONObject.", e);
			throw new DataAccessException(e);
		}
		
		// Get the campaigns.
		JSONObject campaignsJson = new JSONObject();
		try {
			ListIterator<?> campaignListIter = getJdbcTemplate().query(
					SQL_GET_USER_CAMPAIGNS,
					new Object[] { awRequest.getUser().getUserName() },
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new NameAndUrn(rs.getString("urn"), rs.getString("name"));
						}
					}
					).listIterator();
			
			while(campaignListIter.hasNext()) {
				NameAndUrn nau = (NameAndUrn) campaignListIter.next();
				
				try {
					campaignsJson.put(nau._urn, nau._name);
				}
				catch(JSONException e) {
					_logger.error("Problem creating 'classes' JSONObject.", e);
					throw new DataAccessException(e);
				}
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_USER_CAMPAIGNS + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		
		// Get the campaign roles.
		JSONArray campaignRolesJson = new JSONArray();
		try {
			ListIterator<?> campaignRolesListIter = getJdbcTemplate().query(SQL_GET_CAMPAIGN_ROLES, 
																			new Object[] { awRequest.getUser().getUserName() }, 
																			new SingleColumnRowMapper()).listIterator();
			while(campaignRolesListIter.hasNext()) {
				campaignRolesJson.put((String) campaignRolesListIter.next());
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_CAMPAIGN_ROLES + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		
		// Get the classes.
		JSONObject classesJson = new JSONObject();
		try {
			ListIterator<?> classesListIter = getJdbcTemplate().query(
					SQL_GET_USER_CLASSES, 
					new Object[] { awRequest.getUser().getUserName() },
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							return new NameAndUrn(rs.getString("urn"), rs.getString("name"));
						}
					}
					).listIterator();

			while(classesListIter.hasNext()) {
				NameAndUrn nau = (NameAndUrn) classesListIter.next();
				
				try {
					classesJson.put(nau._urn, nau._name);
				}
				catch(JSONException e) {
					_logger.error("Problem creating 'classes' JSONObject.", e);
					throw new DataAccessException(e);
				}
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_USER_CLASSES + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		
		// Get the class roles.
		JSONArray classRolesJson = new JSONArray();
		try {
			ListIterator<?> classRolesListIter = getJdbcTemplate().query(SQL_GET_CLASS_ROLES, 
															 			 new Object[] { awRequest.getUser().getUserName() }, 
															 			 new SingleColumnRowMapper()).listIterator();
			while(classRolesListIter.hasNext()) {
				classRolesJson.put((String) classRolesListIter.next()); 
			}
		}
		catch(org.springframework.dao.DataAccessException dae) {
			_logger.error("Error while calling SQL '" + SQL_GET_CLASS_ROLES + "' with parameter: " + awRequest.getUser().getUserName(), dae);
			throw new DataAccessException(dae);
		}
		
		// Aggregate everything together and place it in the request object.
		try {
			JSONObject result = new JSONObject();
			JSONObject aggregationJson = new JSONObject();
			
			aggregationJson.put("permissions", permissionsJson);
			aggregationJson.put("campaigns", campaignsJson);
			aggregationJson.put("campaign_roles", campaignRolesJson);
			aggregationJson.put("classes", classesJson);
			aggregationJson.put("class_roles", classRolesJson);
			
			result.put(awRequest.getUser().getUserName(), aggregationJson);			
			awRequest.addToReturn(UserInfoQueryAwRequest.RESULT, result, true);
		}
		catch(JSONException e) {
			_logger.error("Error creating response.", e);
			throw new DataAccessException(e);
		}
	}
}