package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

public class UserInfoAggregationService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UserInfoAggregationService.class);
	
	public UserInfoAggregationService(AwRequestAnnotator awRequestAnnotator, Dao dao) {
		super(dao, awRequestAnnotator);
	}

	@Override
	public void execute(AwRequest awRequest) {
		try {
			_logger.info("Gathering the information about the requested users.");
			
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "logged-in user attempting to lookup information about another user to which he does not have permissions");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
