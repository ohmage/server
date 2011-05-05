package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.CampaignQueryResult;
import edu.ucla.cens.awserver.domain.CampaignUrnUserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignReadAwRequest;

/**
 * Performs a filtered search on campaigns for the logged-in user based on the details of the 2.2+ Campaign Read API spec.
 * 
 * @author selsky
 */
public class FindAllFilteredCampaignsForLoggedInUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllFilteredCampaignsForLoggedInUserDao.class);
	
	private String _select = "SELECT c.urn, c.name, c.description, c.xml, crs.running_state, cps.privacy_state, c.creation_timestamp " +
			                 "FROM campaign c, campaign_running_state crs, campaign_privacy_state cps " +
			                 "WHERE c.urn = ? " +
			                 "AND c.privacy_state_id = cps.id " +
			                 "AND c.running_state_id = crs.id";
	
//	private String _select = "SELECT c.urn, c.name, c.description, c.xml, c.running_state, c.privacy_state, c.creation_timestamp," +
//			                  " css.urn " +
//                              "FROM campaign c, campaign_class cc, class css " +
//                              "WHERE cc.class_id = css.id " +
//                              "AND cc.campaign_id = c.id " +
//                              "AND c.urn = ? ";
//                                            
//    private String _andClassUrnIn = "AND css.urn IN ";
	
	private String _andPrivacyState = "AND cps.privacy_state = ? ";
	private String _andRunningState = "AND crs.running_state = ? ";
	private String _andStartDate = "AND c.creation_timestamp >= ? ";
	private String _andEndDate = "AND c.creation_timestamp <= ? ";
	private String _orderBy = " ORDER BY c.urn";
	
	public FindAllFilteredCampaignsForLoggedInUserDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(AwRequest awRequest) {
		_logger.info("about to generate SQL and run filtered campaign query");
		
		List<CampaignQueryResult> campaignList = new ArrayList<CampaignQueryResult>();
		
		try {
			
			// The venerable cast to the correct request type
			CampaignReadAwRequest req = (CampaignReadAwRequest) awRequest;
			
			List<CampaignUrnUserRole> campaignUrnUserRoleList = (List<CampaignUrnUserRole>) req.getResultList(); 

			// Convert to a Map for easier traversal and filtering based on whether the query params contain a user role and/or
			// campaign URNs
			Map<String, List<String>> urnRoleMap = new HashMap<String, List<String>>();
			
			for(CampaignUrnUserRole cuur : campaignUrnUserRoleList) {

				if(req.getCampaignUrnList().isEmpty() 
					|| (! req.getCampaignUrnList().isEmpty() && req.getCampaignUrnList().contains(cuur.getUrn()))) {
				
					if(! urnRoleMap.containsKey(cuur.getUrn())) {
						List<String> roleList = new ArrayList<String>();
						
						if(null == req.getUserRole() 
							|| (null != req.getUserRole() && req.getUserRole().equals(cuur.getRole()))) {
							
							_logger.info("adding to role list: " + cuur.getRole());
							roleList.add(cuur.getRole());
						}
						
						urnRoleMap.put(cuur.getUrn(), roleList);
						
					} else {
						
						if(null == req.getUserRole() 
							|| (null != req.getUserRole() && ! req.getUserRole().equals(cuur.getRole()))) {
						
							urnRoleMap.get(cuur.getUrn()).add(cuur.getRole());
						}
					}
				}
			}
			
			// Check if all of the lists in the map are empty because, if so, there is nothing to do
			Iterator<String> iterator = urnRoleMap.keySet().iterator();
			int numberOfEmptyLists = 0;
			while(iterator.hasNext()) {
				if(urnRoleMap.get(iterator.next()).isEmpty()) {
					numberOfEmptyLists++;
				}
			}
			if(numberOfEmptyLists > 0 && numberOfEmptyLists == (urnRoleMap.size() - 1)) {
				awRequest.setResultList(Collections.emptyList());
				return;
			}
			
			// clean out the result list before adding objects of a different type to it
			req.getResultList().clear();
			
			// Now generate the SQL based on each campaign URN and the user's most permissive role for that URN.
			// For the purposes here, both author and supervisor have the maximum permission against running_state and privacy_state
			// Should this logic be changed to a very simple SQL statement that retrieves all campaigns for the current user
			// and then does an in-memory filter based on the params and logged-in user's role in each campaign returned?
			
			iterator = urnRoleMap.keySet().iterator();
			while(iterator.hasNext()) {
				
				String urn = iterator.next();
				List<Object> pList = new ArrayList<Object>();
				pList.add(urn);
				
				StringBuilder sql = new StringBuilder();
				sql.append(_select);
				
				final List<String> roles = urnRoleMap.get(urn); 
				
				if(null != req.getPrivacyState()) { // shared, private
					String privacyState = req.getPrivacyState();
					if("private".equals(privacyState)) {
						// analysts cannot view
						if(roles.contains("analyst") && roles.size() == 1) {
							continue;
						}
					}
					sql.append(_andPrivacyState);
					pList.add(privacyState);
				}
				
				if(null != req.getRunningState()) { // running, stopped
					String runningState = req.getRunningState();
					if("stopped".equals(runningState)) {
						// participants cannot view a stopped campaign
						if(roles.contains("participant") && roles.size() == 1) {
							continue;
						}
					}
					sql.append(_andRunningState);
					pList.add(runningState);
				}
				
				if(null != req.getStartDate()) {
					sql.append(_andStartDate);
					pList.add(req.getStartDate());
				}
				
				if(null != req.getEndDate()) {
					sql.append(_andEndDate);
					pList.add(req.getEndDate());
				}
				
				sql.append(_orderBy);
				
				_logger.info("about to run the following SQL: " + sql.toString() + " with the following p list:" + pList);
				
				campaignList.addAll( 
					getJdbcTemplate().query(
						sql.toString(), 
						pList.toArray(),
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								CampaignQueryResult result = new CampaignQueryResult();
								result.setUrn(rs.getString(1));
								result.setName(rs.getString(2));
								result.setDescription(rs.getString(3));
								result.setXml(rs.getString(4));
								result.setRunningState(rs.getString(5));
								result.setPrivacyState(rs.getString(6));
								result.setCreationTimestamp(rs.getTimestamp(7).toString());
								// the user roles come from the urnRoleMap above
								result.setUserRoles(roles);
								
								return result;
							}
						}
					)
				);
			}
			
			req.setResultList(campaignList);
			_logger.info("found " + campaignList.size() + " query results");
		}	
		
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following some SQL.", dae);
			throw new DataAccessException(dae);
		}
		
		catch (ClassCastException cce) {
			_logger.error("attempted and could not cast to RetrieveCampaignAwRequest");
			throw new DataAccessException(cce);
		}
	}
}
