package edu.ucla.cens.awserver.service;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks that the user has sufficient permissions to delete the file.
 * 
 * @author John Jenkins
 */
public class UserHasPermissionForDocumentService extends AbstractAnnotatingDaoService {
	public static Logger _logger = Logger.getLogger(UserHasPermissionForDocumentService.class);
	
	private boolean _required;
	
	/**
	 * Creates this service.
	 * 
	 * @param annotator The annotator to respond with should the user not have
	 * 					the correct permissions to delete this document.
	 * 
	 * @param dao The DAO to run to execute this service.
	 */
	public UserHasPermissionForDocumentService(AwRequestAnnotator annotator, Dao dao, boolean required) {
		super(dao, annotator);
		
		_required = required;
	}

	/** 
	 * Gets the list of documents that the requesting user is allowed to delete
	 * and checks that the one in the request is among them.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the document ID from the request.
		String documentId;
		try {
			documentId = (String) awRequest.getToProcessValue(InputKeys.DOCUMENT_ID);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key '" + InputKeys.DOCUMENT_ID + "'.");
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the requesting user has sufficient permissions to perform this action on the document.");
		
		// Get the list of IDs the user is allowed to delete.
		try {
			getDao().execute(awRequest);
		} 
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// Validate that the user can delete the document.
		if(! awRequest.getResultList().contains(documentId)) {
			getAnnotator().annotate(awRequest, "The requesting user doesn't have sufficient permissions to perform this action on the document.");
		}
	}
}