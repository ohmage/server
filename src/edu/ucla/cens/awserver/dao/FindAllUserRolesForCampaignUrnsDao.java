package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.CampaignQueryResult;
import edu.ucla.cens.awserver.domain.CampaignUrnLoginIdUserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.RetrieveCampaignAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Super-specific DAO for finding all of the users for a list of campaign URNs for /app/campaign/read.
 * 
 * @author selsky
 */
public class FindAllUserRolesForCampaignUrnsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllUserRolesForCampaignUrnsDao.class);
	
	private String _select = "SELECT c.urn, ur.role, u.login_id" +                            
					 		 " FROM user_role_campaign urc, user u, campaign c, user_role ur" +    
 					 		 " WHERE urc.campaign_id = c.id" +                    
							 " AND urc.user_id = u.id" +
							 " AND urc.user_role_id = ur.id" +
							 " AND c.urn IN ";
	
    private String _orderBy = " ORDER BY c.urn, ur.role, u.login_id";
	
	public FindAllUserRolesForCampaignUrnsDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(AwRequest awRequest) {
		_logger.info("looking up all users and their roles for campaign URNs");
		RetrieveCampaignAwRequest req = (RetrieveCampaignAwRequest) awRequest; // ugly cast
		StringBuilder builder = new StringBuilder();
		
		if("long".equals(req.getOutputFormat())) { // only need the roles if long output is selected
		
			List<String> pList = new ArrayList<String>();
			List<?> results = awRequest.getResultList();
			int size = results.size();
			if(0 == size) {
				return; // no campaign URNs to use to perform the lookup
			}
			
			for(int i = 0; i < size; i++) {
				pList.add(((CampaignQueryResult)results.get(i)).getUrn()); // another ugly cast ...
			}
			
			builder.append(_select);
			builder.append(StringUtils.generateStatementPList(size));
			builder.append(_orderBy);
			
			try {
				req.setCampaignUrnLoginIdUserRoleList(
					getJdbcTemplate().query(
						builder.toString(),
						pList.toArray(),
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								CampaignUrnLoginIdUserRole clu = new CampaignUrnLoginIdUserRole();
								clu.setCampaignUrn(rs.getString(1));
								clu.setRole(rs.getString(2));
								clu.setLoginId(rs.getString(3));
								return clu;
							}
						}
					)
				);
				
				_logger.info("found " + req.getCampaignUrnLoginIdUserRoleList().size() + " results");
				
			} catch (org.springframework.dao.DataAccessException dae) {
				
				_logger.error("an error occurred running the following SQL '" + builder.toString() + "' with the" +
					" parameter list " + pList + ": " + dae.getMessage());
				
				throw new DataAccessException(dae);
			}
			
		} else {
			_logger.info("not retrieving roles because output_format=" + req.getOutputFormat());
		}
	}
}
