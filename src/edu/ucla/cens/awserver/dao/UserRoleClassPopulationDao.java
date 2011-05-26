package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.UserRoleClassResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Gets the list of classes that the currently logged in user belongs to and
 * sets its result list to that list.
 * 
 * @author John Jenkins
 */
public class UserRoleClassPopulationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(UserRoleClassPopulationDao.class);
	
	private static final String SQL_GET_CLASSES_AND_ROLE = "SELECT c.urn, c.name, c.description, ucr.role " +
														   "FROM user u, class c, user_class uc, user_class_role ucr " +
														   "WHERE u.login_id = ? " +
														   "AND u.id = uc.user_id " +
														   "AND c.id = uc.class_id " +
														   "AND ucr.id = uc.user_class_role_id";
	
	/**
	 * Sets up this DAO with the DataSource to use when running queries.
	 * 
	 * @param dataSource The DataSource to use to access the database.
	 */
	public UserRoleClassPopulationDao(DataSource dataSource) {
		super(dataSource);
	}

	/**
	 * Queries the database for all the classes the user belongs and their role
	 * in that class.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Querying database for class information and the currently logged in user's role in that class.");
		
		try {
			awRequest.setResultList(
				getJdbcTemplate().query(
					SQL_GET_CLASSES_AND_ROLE,
					new Object[] { awRequest.getUser().getUserName() },
					new RowMapper() {
						@Override
						public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
							UserRoleClassResult result = new UserRoleClassResult(rs.getString("urn"),
																				 rs.getString("name"),
																				 rs.getString("description"),
																				 rs.getString("role"));
							return result;
						}
					}
				)
			);
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("There was an error executing SQL '" + SQL_GET_CLASSES_AND_ROLE + "' with parameter: " + awRequest.getUser().getUserName());
			throw new DataAccessException(e);
		}
	}

}
