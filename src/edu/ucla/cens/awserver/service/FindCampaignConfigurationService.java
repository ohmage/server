package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.Configuration;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointQueryAwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;
import edu.ucla.cens.awserver.request.SurveyUploadAwRequest;

/**
 * @author selsky
 */
public class FindCampaignConfigurationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(FindCampaignConfigurationService.class);
	
    public FindCampaignConfigurationService(Dao dao) {
    	super(dao);
    }
	
	public void execute(AwRequest awRequest) {
		try {
			
			getDao().execute(awRequest);
			@SuppressWarnings("unchecked")
			List<Configuration> configurations = (List<Configuration>) awRequest.getResultList();
			
			if(null == configurations) { 
				_logger.error("no configuration found for campaign: " + awRequest.getCampaignUrn());
				throw new ServiceException("no configuration found for campaign: " + awRequest.getCampaignUrn());
			} 
			
			if(configurations.size() > 1) { // if this occurs, something broke the db constraints on URN uniqueness
				_logger.error(configurations.size() + " configurations found for campaign: " + awRequest.getCampaignUrn());
				throw new ServiceException(configurations.size() + " configurations found for campaign: " + awRequest.getCampaignUrn());
			}
			
			// hack 
			// fix by making Configuration a 'first-class' instance variable in the AwRequest or by using the toProcess() method
			
			if(awRequest instanceof SurveyResponseReadAwRequest) {
				((SurveyResponseReadAwRequest) awRequest).setConfiguration(configurations.get(0));
			} else if (awRequest instanceof DataPointQueryAwRequest) {
				((DataPointQueryAwRequest) awRequest).setConfiguration(configurations.get(0));
			} else if (awRequest instanceof SurveyUploadAwRequest) {
				((SurveyUploadAwRequest) awRequest).setConfiguration(configurations.get(0));
			} else {
				throw new ServiceException("cannot set a Configuration on the AwRequest because the AwRequest type is invalid: " + awRequest.getClass());
			}
			
			
		} catch (DataAccessException dae) {
			
			throw new ServiceException(dae);
			
		}
	}
}
