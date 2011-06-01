/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.service;

import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


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
