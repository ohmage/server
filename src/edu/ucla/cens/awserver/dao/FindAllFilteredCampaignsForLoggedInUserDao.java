package edu.ucla.cens.awserver.dao;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.SingleColumnRowMapper;

import edu.ucla.cens.awserver.domain.CampaignUrnUserRole;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.RetrieveCampaignAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Performs a filtered search on campaigns for the logged-in user based on the details of the 2.2+ Campaign Read API spec.
 * 
 * @author selsky
 */
public class FindAllFilteredCampaignsForLoggedInUserDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllFilteredCampaignsForLoggedInUserDao.class);
//	private boolean _useLoggedInUser;
	
	private String _select = "SELECT urn, name, description, xml, running_state, privacy_state, creation_timestamp " +
			                 "FROM campaign " +
			                 "WHERE urn = ? ";
	
	private String _selectWithClass = "SELECT urn, name, description, xml, running_state, privacy_state, creation_timestamp " +
                                      "FROM campaign c, campaign_class cc, class css" +
                                      "WHERE cc.class_id = css.id " +
                                      "AND cc.campaign_id = c.id " +
                                      "AND campaign.urn = ? ";
                                            
    private String _andClassUrnIn = "AND class.urn IN ";
	
	private String _andPrivacyState = "AND privacy_state = ? ";
	private String _andRunningState = "AND running_state = ? ";
	private String _andStartDate = "AND creation_timestamp >= ? ";
	private String _andEndDate = "AND creation_timestamp <= ?";
	
	public FindAllFilteredCampaignsForLoggedInUserDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		StringBuilder sql = new StringBuilder();
		try {
			
			// The venerable cast to the correct request type
			RetrieveCampaignAwRequest req = (RetrieveCampaignAwRequest) awRequest;
			
			@SuppressWarnings("unchecked")
			List<CampaignUrnUserRole> campaignUrnUserRoleList = (List<CampaignUrnUserRole>) req.getResultList(); 

			// Convert to a Map for easier traversal and filtering based on whether the query params contain a user role and/or
			// campaign URNs
			Map<String, List<String>> urnRoleMap = new HashMap<String, List<String>>();
			
			for(CampaignUrnUserRole cuur : campaignUrnUserRoleList) {

				if(! req.getCampaignUrnList().isEmpty() && ! req.getCampaignUrnList().contains(cuur.getUrn())) {
				
					if(! urnRoleMap.containsKey(cuur.getUrn())) {
						List<String> roleList = new ArrayList<String>();
						if(null != req.getUserRole() && ! req.getUserRole().equals(cuur.getRole())) {
							roleList.add(cuur.getRole());
						}
						urnRoleMap.put(cuur.getUrn(), roleList);
					} else {
						if(null != req.getUserRole() && ! req.getUserRole().equals(cuur.getRole())) {
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
			if(numberOfEmptyLists == (urnRoleMap.size() - 1)) {
				awRequest.setResultList(Collections.emptyList());
				return;
			}
			
			// Now generate the SQL based on each campaign URN and the user's most permissive role for that URN.
			// For the purposes here, both author and supervisor have the maximum permission against running_state and privacy_state
			
			iterator = urnRoleMap.keySet().iterator();
			while(iterator.hasNext()) {
				
				String urn = iterator.next();
				List<Object> pList = new ArrayList<Object>();
				pList.add(urn);
				
				// Check to see if the query is requesting specific classes
				if(! req.getClassUrnList().isEmpty()) {
					sql.append(_selectWithClass);
					sql.append(_andClassUrnIn);
					sql.append(StringUtils.generateStatementPList(req.getClassUrnList().size()));
					pList.addAll(req.getClassUrnList());
				} else {
					sql.append(_select);	
				}
				
				List<String> roles = urnRoleMap.get(urn); 
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
						// participants cannot view stopped campaigm
						if(roles.contains("analyst") && roles.size() == 1) {
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
				
				awRequest.setResultList(
					getJdbcTemplate().query(
						sql.toString(), 
						new Object[] { awRequest.getUser().getUserName() },
						new SingleColumnRowMapper()
					)
				);
			}
			
		}	
		
		catch (org.springframework.dao.DataAccessException dae) {
			_logger.error("a DataAccessException occurred when running the following sql '" + sql + "' with the parameter"
				+ (awRequest.getUser().getUserName()), dae);
			throw new DataAccessException(dae);
		}
		
		catch (ClassCastException cce) {
			_logger.error("attempted and could not cast to RetrieveCampaignAwRequest");
			throw new DataAccessException(cce);
		}
	}
}
