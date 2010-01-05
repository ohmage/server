package edu.ucla.cens.awserver.service;


import java.util.List;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * Dispatches to a DAO to perform campaign existence checks.
 * 
 * @author selsky
 */
public class CampaignExistsService extends AbstractDaoService {
//	private static Logger _logger = Logger.getLogger(CampaignExistsService.class);
	
	/**
	 * Creates an instances of this class using the supplied DAO as the method of data access.
	 */
	public CampaignExistsService(Dao dao) {
		super(dao);
	}
	
	/**
	 * Uses the subdomain from the user's original request URI to determine whether the subdomain is mapped to an actual 
	 * AndWellness campaign. 
	 */
	public void execute(AwRequest request) {
		try {
			
			getDao().execute(request);
			List<?> results = (List<?>) request.getPayload().get("results");
			
			if(null != results &&  ! results.isEmpty()) { 
				
				request.getPayload().put("campaignExistsForSubdomain", "true");
				
			}
			
		} catch (DataAccessException dae) { 
			
			throw new ServiceException(dae);
		}
	}
}
