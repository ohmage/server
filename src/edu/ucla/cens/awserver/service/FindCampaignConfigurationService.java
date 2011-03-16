package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;

/**
 * @author selsky
 */
public class FindCampaignConfigurationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(FindCampaignConfigurationService.class);
	
    public FindCampaignConfigurationService(Dao dao) {
    	super(dao);
    }
	
	public void execute(AwRequest awRequest) {
		NewDataPointQueryAwRequest req = (NewDataPointQueryAwRequest) awRequest;
		
		try {
			getDao().execute(awRequest);
			@SuppressWarnings("unchecked")
			List<Configuration> configurations = (List<Configuration>) awRequest.getResultList();
			
			if(null == configurations) { // logical error!
				_logger.error("no configuration found for campaign: " + req.getCampaignName());
				throw new ServiceException("no configuration found for campaign: " + req.getCampaignName());
			} 
			
			// Now find the correct campaign for the version in the request
			
			for(Configuration c : configurations) {
				
				if(c.getCampaignVersion().equals(req.getCampaignVersion())) {
					
					req.setConfiguration(c);
					req.setResultList(null); // clean up in case there are many versions for a campaign. This will go away soon.
					return;
				}
			}
			
			_logger.error("no configuration found for campaign " + req.getCampaignName() + " version " + req.getCampaignVersion());
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}
}
