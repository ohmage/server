package edu.ucla.cens.awserver.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.ListIterator;
import java.util.Set;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;

/**
 * Gets the list of document IDs based on the DAO and saves them in the request
 * with any other document IDs that have been found by previous calls to this
 * Service.
 * 
 * @author John Jenkins
 */
public class DocumentIdAggregationService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(DocumentIdAggregationService.class);
	
	public static final String KEY_DOCUMENT_ID_AGGREGATION_SERVICE_RESULT = "document_id_aggregation_service_result";
	
	private String _key;
	private boolean _required;
	
	/**
	 * Creates this Service.
	 * 
	 * @param dao The DAO to run to get the list of document IDs.
	 * 
	 * @param key The key to check for its existance in the request. If it
	 * 			  doesn't exist then the DAO will throw an exception, so we
	 * 			  preempt it here then check if it's required if it doesn't
	 * 			  exist.
	 * 
	 * @param required Whether or not this aggregation is required. If not, it
	 * 				   will still be done if the key exists.
	 */
	public DocumentIdAggregationService(Dao dao, String key, boolean required) {
		super(dao);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Checks that the key exists and, if not and required, throws an
	 * exception. Then, calls the DAO and transfers its results into the Set
	 * stored in the request's toReturn map.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Check that the 'key' exists and, if not, check if it is required.
		try {
			awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required key: " + _key);
			}
			else {
				return;
			}
		}
		
		_logger.info("Retrieving the list of documents associated with the requesting user and the list from key: " + _key);
		
		// Get the result set this far.
		Set<String> result;
		try {
			String resultString = (String) awRequest.getToReturnValue(DocumentIdAggregationService.KEY_DOCUMENT_ID_AGGREGATION_SERVICE_RESULT);
			if("".equals(resultString)) {
				result = new HashSet<String>();
			}
			else {
				result = new HashSet<String>(Arrays.asList(resultString.split(InputKeys.LIST_ITEM_SEPARATOR)));
			}
		}
		catch(IllegalArgumentException e) {
			result = new HashSet<String>();
		}
		
		// Run the DAO.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}

		// Add the results from the DAO into the Set.
		ListIterator<?> daoResultIter = awRequest.getResultList().listIterator();
		while(daoResultIter.hasNext()) {
			result.add((String) daoResultIter.next());
		}
		
		// Put the list of items back in the request as a String.
		awRequest.addToReturn(KEY_DOCUMENT_ID_AGGREGATION_SERVICE_RESULT, StringUtils.collectionToDelimitedString(result, InputKeys.LIST_ITEM_SEPARATOR), true);
	}

}
