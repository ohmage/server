package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Reads the information about a class using the provided DAO.
 * 
 * @author John Jenkins
 */
public class ClassReadService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(ClassReadService.class);
	
	/**
	 * Creates the service with an annotator to respond with should there be
	 * an error and a DAO to call to make the request.
	 * 
	 * @param annotator The annotator to respond with should there be an
	 * 					error.
	 * 
	 * @param dao The DAO to use to make the request.
	 */
	public ClassReadService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}

	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Attempting to read the class information.");
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "There was an error reading the information about the classes.");
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}

}
