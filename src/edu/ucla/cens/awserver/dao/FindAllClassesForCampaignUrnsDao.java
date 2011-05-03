package edu.ucla.cens.awserver.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import edu.ucla.cens.awserver.domain.CampaignQueryResult;
import edu.ucla.cens.awserver.domain.CampaignUrnClassUrn;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.CampaignReadAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * DAO for finding all of the class URNs for a list of campaign URNs.
 * 
 * @author selsky
 */
public class FindAllClassesForCampaignUrnsDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(FindAllClassesForCampaignUrnsDao.class);
	
	private String _select = "SELECT campaign.urn, class.urn" +                            
							 " FROM class, campaign, campaign_class" +    
 							 " WHERE campaign_class.campaign_id = campaign.id" +                    
							 " AND campaign_class.class_id = class.id" +
							 " AND campaign.urn IN ";
	
    private String _orderBy = " ORDER BY campaign.urn, class.urn";
	
	public FindAllClassesForCampaignUrnsDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public void execute(AwRequest awRequest) {
		_logger.info("looking up all users and their roles for campaign URNs");
		CampaignReadAwRequest req = (CampaignReadAwRequest) awRequest; // ugly cast
		StringBuilder builder = new StringBuilder();
		
		if("long".equals(req.getOutputFormat())) {
		
			List<String> pList = new ArrayList<String>();
			List<?> results = awRequest.getResultList();
			int size = results.size();
			
			for(int i = 0; i < size; i++) {
				String urn = ((CampaignQueryResult)results.get(i)).getUrn(); // FIXME another ugly cast ...
				
				if(req.getClassUrnList().isEmpty() || req.getClassUrnList().contains(urn)) {
					pList.add(urn); 	
				}
			}
			
			builder.append(_select);
			builder.append(StringUtils.generateStatementPList(size));
			builder.append(_orderBy);
			
			try {
				req.setCampaignUrnClassUrnList(
					getJdbcTemplate().query(
						builder.toString(),
						pList.toArray(),
						new RowMapper() {
							public Object mapRow(ResultSet rs, int rowNum) throws SQLException {
								CampaignUrnClassUrn cc = new CampaignUrnClassUrn();
								cc.setCampaignUrn(rs.getString(1));
								cc.setClassUrn(rs.getString(2));
								return cc;
							}
						}
					)
				);
				
				_logger.info("found " + req.getCampaignUrnClassUrnList().size() + " results");
				
			} catch (org.springframework.dao.DataAccessException dae) {
				
				_logger.error("an error occurred running the following SQL '" + builder.toString() + "' with the" +
					" parameter list " + pList + ": " + dae.getMessage());
				
				throw new DataAccessException(dae);
			}
			
		} else {
			_logger.info("not retrieving classes because output_format=" + req.getOutputFormat());
		}
	}
}
