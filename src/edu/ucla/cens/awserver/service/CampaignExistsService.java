package edu.ucla.cens.awserver.service;


import java.util.List;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.util.StringUtils;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Dispatches to a DAO to perform campaign existence checks.
 * 
 * TODO - this class could be used in any situation where all that needs to be checked is the existence of a single row in the DB
 * 
 * @author selsky
 */
public class CampaignExistsService extends AbstractAnnotatingDaoService {
//	private static Logger _logger = Logger.getLogger(CampaignExistsService.class);
	private String _errorMessage;
	
	/**
	 * Creates an instances of this class using the supplied DAO as the method of data access.
	 * 
	 * @throws IllegalArgumentException if the error message is empty, all whitespace, or null
	 */
	public CampaignExistsService(Dao dao, AwRequestAnnotator awRequestAnnotator, String errorMessage) {
		super(dao, awRequestAnnotator);
		if(StringUtils.isEmptyOrWhitespaceOnly(errorMessage)) {
			throw new IllegalArgumentException("error message cannot be empty or null");
		}
		_errorMessage = errorMessage;
	}
	
	/**
	 * Uses the subdomain from the user's original request URI to determine whether the subdomain is mapped to an actual 
	 * AndWellness campaign. 
	 */
	public void execute(AwRequest request) {
		try {
			
			getDao().execute(request);
			List<?> results = (List<?>) request.getAttribute("results");
			
			if(null == results || results.isEmpty()) { 
				
				getAnnotator().annotate(request, _errorMessage);
				
			}
			
		} catch (DataAccessException dae) { 
			
			throw new ServiceException(dae);
		}
	}
}
