package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * "Generic" Service that dispatches to a DAO where the DAO controls whether the request has failed. This class exists
 * in order separate the annotation of the request up into the Service layer instead of handling it in the DAO. 
 * 
 * @author Joshua Selsky
 */
public class GenericAnnotatingDaoService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(GenericAnnotatingDaoService.class);
	private String _annotationMessage;
	private String _loggingMessage;
	
	/**
	 * Builds a validation service to check if a survey key (survey_id) exists.
	 * 
	 * @param annotator The annotator to report back to the user if there is a
	 * 					problem.
	 * 
	 * @param dao The DAO to use to perform the check against the database.
	 */
	public GenericAnnotatingDaoService(AwRequestAnnotator annotator, Dao dao, String annotationMessage, String loggingMessage) {
		super(dao, annotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(annotationMessage)) {
			throw new IllegalArgumentException("The annotation message cannot be null or empty.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(loggingMessage)) {
			throw new IllegalArgumentException("The logging message cannot be null or empty.");
		}
		
		_annotationMessage = annotationMessage;
		_loggingMessage = loggingMessage;
	}
	
	/**
	 * Dispatches to the DAO and annotates the request if the request has failed in the DAO layer.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info(_loggingMessage);
		
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, _annotationMessage);
			}
		}
		catch(DataAccessException dae) {
			throw new ServiceException(dae);
		}
	}
}
