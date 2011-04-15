//package edu.ucla.cens.awserver.dao;
//
//import javax.sql.DataSource;
//
//import org.apache.log4j.Logger;
//import org.springframework.jdbc.core.SingleColumnRowMapper;
//
//import edu.ucla.cens.awserver.request.AwRequest;
//
///**
// * DAO for finding all of the users in a particular campaign.
// * 
// * @author selsky
// */
//public class FindAllUsersForCampaignUrnsDao extends AbstractDao {
//	private static Logger _logger = Logger.getLogger(FindAllUsersForCampaignUrnsDao.class);
//	
//	private String findAllUsersForCampaignUrnsSql = "SELECT u.login_id, ur.role, c.urn" +                            
//												    " FROM user_role_campaign urc, user u, campaign c, user_role ur" +    
// 												    " WHERE urc.campaign_id = c.id " +                    
//												    " AND urc.user_id = u.id" +
//												    " AND urc_user_role_id = ur.id" +
//												    " AND c.urn IN " + 
//												    " ORDER BY c.urn, ur.role, u.login_id";
//	
//	public FindAllUsersForCampaignUrnsDao(DataSource dataSource) {
//		super(dataSource);
//	}
//	
//	/**
//	 * Returns all of the distinct user ids for the current campaign id of the user in the AwRequest.
//	 */
//	@Override
//	public void execute(AwRequest awRequest) {
//		try {
//			
//			
//			awRequest.setResultList(
//				getJdbcTemplate().query(
//					findAllUsersForCampaignUrnsSql,
//					new Object[] {awRequest.getCampaignUrn()},
//					new 
//				)
//			);
//			
//		} catch (org.springframework.dao.DataAccessException dae) {
//			
//			_logger.error("an error occurred running the following SQL '" + findAllUsersForCampaignsSql + "' with the parameter " +
//				"list" + awRequest.getCampaignUrn() + ": " + dae.getMessage());
//			
//			throw new DataAccessException(dae);
//		}
//	}
//}
