package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Creates the document on the disk and places an entry in the database.
 * 
 * @author John Jenkins
 */
public class DocumentCreationService extends AbstractAnnotatingDaoService {
	/**
	 * Creates this service with an annotator to use if the request fails and
	 * a DAO to call to run the actual creation.
	 * 
	 * @param annotator The annotator to respond with should the request fail.
	 * 
	 * @param dao The DAO to call to create the document.
	 */
	public DocumentCreationService(AwRequestAnnotator annotator, Dao dao) {
		super(dao, annotator);
	}
	
	/**
	 * Calls the DAO to create the document and insert it into the database.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			getDao().execute(awRequest);
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "Document creation failed.");
			}
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
}