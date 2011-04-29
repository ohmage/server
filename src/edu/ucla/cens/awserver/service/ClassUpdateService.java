package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Updates the class with the information in the request.
 * 
 * @author John Jenkins
 */
public class ClassUpdateService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(ClassUpdateService.class);
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to respond with if there is a problem.
	 * 
	 * @param dao The DAO to call.
	 */
	public ClassUpdateService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
		
	}

	
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to update the class.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "There was an error while updating the class.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
