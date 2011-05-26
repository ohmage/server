package edu.ucla.cens.awserver.service;

import java.util.ListIterator;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.Document;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DocumentReadAwRequest;

public class DocumentReadResponseBuilderService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(DocumentReadResponseBuilderService.class);
	
	public DocumentReadResponseBuilderService(Dao dao) {
		super(dao);
	}

	/**
	 * Runs the DAO to set the result list as the list of Documents and their
	 * information.
	 * 
	 * Gets the result from the result list which should be a list of Document
	 * objects. Then, adds each of them to a JSONObject with the key being 
	 * their unique IDs. Finally, it returns that JSONObject as a String to the
	 * request to be written by the writer.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Creating response for document read request.");
		
		// Run the DAO to set the result list as the Documents and their info.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// Create the JSONObject that will be the response.
		JSONObject result = new JSONObject();
		ListIterator<?> resultIter = awRequest.getResultList().listIterator();
		while(resultIter.hasNext()) {
			Document currDocument = (Document) resultIter.next();
			
			try {
				result.put(currDocument.getDocumentId(), currDocument.toJsonObject());
			}
			catch(JSONException e) {
				_logger.error("Error creating response object for document: " + currDocument.getDocumentId(), e);
			}
		}
		
		// Set the response in the request.
		awRequest.addToReturn(DocumentReadAwRequest.KEY_DOCUMENT_INFORMATION, result, true);
	}
}