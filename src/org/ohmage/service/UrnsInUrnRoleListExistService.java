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

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;


/**
 * Parses a list of URN-role pairs for their URNs and checks that each of the
 * URNs exist according to some DAO.
 * 
 * @author John Jenkins
 *
 */
public class UrnsInUrnRoleListExistService extends AbstractAnnotatingDaoService {
	private static Logger _logger = Logger.getLogger(UrnsInUrnRoleListExistService.class);
	
	private String _mapKey;
	private String _daoKey;
	
	private boolean _required;
	
	/**
	 * Sets up this service that checks if the URNs in a list of URNs and roles
	 * exist.
	 * 
	 * @param annotator The annotator to reply with should the validation fail.
	 * 
	 * @param dao The DAO to use when checking if an URN exists.
	 * 
	 * @param mapKey The key to use to get the list from the maps.
	 * 
	 * @param daoKey The key to use to temporarily store the URN to be checked
	 * 				 in the request.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public UrnsInUrnRoleListExistService(AwRequestAnnotator annotator, Dao dao, String mapKey, String daoKey, boolean required) {
		super(dao, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(mapKey)) {
			throw new IllegalArgumentException("The 'mapkey' cannot be null nor an empty string.");
		}
		else if(StringUtils.isEmptyOrWhitespaceOnly(daoKey)) {
			throw new IllegalArgumentException("The 'daoKey' cannot be null nor an empty string.");
		}
		
		_mapKey = mapKey;
		_daoKey = daoKey;
		
		_required = required;
	}

	/**
	 * Checks if the list exists and if not that it isn't required. Then, gets
	 * the list and parses it checking that each URN in the list exists.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the URN-role list from the request.
		String urnRoleList;
		try {
			urnRoleList = (String) awRequest.getToProcessValue(_mapKey);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				throw new ServiceException("The required URN-role list is missing from the toProcess map with key: " + _mapKey);
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating the URNs in the list set by key '" + _mapKey + "'.");
		
		String[] urnRoleArray = urnRoleList.split(InputKeys.LIST_ITEM_SEPARATOR);
		for(int i = 0; i < urnRoleArray.length; i++) {
			String[] urnRole = urnRoleArray[i].split(InputKeys.ENTITY_ROLE_SEPARATOR);
			
			awRequest.addToProcess(_daoKey, urnRole[0], true);
			try {
				getDao().execute(awRequest);
			}
			catch(DataAccessException e) {
				throw new ServiceException(e);
			}
			
			if(awRequest.isFailedRequest()) {
				getAnnotator().annotate(awRequest, "Non-existant campaign URN in request at index: " + i);
				return;
			}
		}
	}
}
