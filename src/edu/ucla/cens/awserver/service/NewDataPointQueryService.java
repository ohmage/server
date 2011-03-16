package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
 */
public class NewDataPointQueryService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(NewDataPointQueryService.class);
	
	public NewDataPointQueryService(Dao dao) {
		super(dao);
	}
	
	
	@Override
	public void execute(AwRequest awRequest) {
		
		getDao().execute(awRequest);
		
	}

}
