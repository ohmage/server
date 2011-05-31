package edu.ucla.cens.awserver.service;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Checks a list in the request against a list returned by a DAO to ensure that
 * all the items in the request list exist in the DAO list.
 * 
 * @author John Jenkins
 */
public class ResultsFromDaoContainAllItemsInListService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(ResultsFromDaoContainAllItemsInListService.class);
	
	private String _key;
	private boolean _required;
	private String _loggerText;
	private boolean _union;
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to respond with should an item exist in
	 * 					the list but not be returned by the DAO.
	 *  
	 * @param dao The DAO to run to get the list to check against.
	 * 
	 * @param key They to retrieve the list from the request.
	 * 
	 * @param loggerText The text to display before running the DAO if the list
	 * 					 was found in the request.
	 * 
	 * @param union If true then all items in the list must exist in the
	 * 				results from the DAO. If false, then no items in the list
	 * 				may exist in the DAO.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public ResultsFromDaoContainAllItemsInListService(AwRequestAnnotator annotator, Dao dao, String key, String loggerText, boolean union, boolean required) {
		super(dao, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(loggerText)) {
			throw new IllegalArgumentException("The logger text cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
		_loggerText = loggerText;
		_union = union;
	}

	/**
	 * Ensures that the list exists if required. Then, if found, runs the DAO
	 * and checks that each of the items in the list were returned by the DAO.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String list;
		try {
			list = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("Missing required parameter: " + _key);
			}
			else {
				return;
			}
		}
		
		_logger.info(_loggerText);
		
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		List<?> resultList = awRequest.getResultList();
		String[] array = list.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < array.length; i++) {
			if(resultList.contains(array[i]) != _union) {
				getAnnotator().annotate(awRequest, "Value found in request list '" + _key + "' that the DAO didn't find: " + array[i]);
				awRequest.setFailedRequest(true);
				return;
			}
		}
	}
}