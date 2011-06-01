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
package org.ohmage.validator;

import java.util.List;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;
import org.ohmage.util.StringUtils;


/**
 * Validates the column list for a new data point query.
 * 
 * @author selsky
 */
public class ColumnListValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(ColumnListValidator.class);
	private List<String> _allowedColumnValues;
	
	public ColumnListValidator(AwRequestAnnotator awRequestAnnotator, List<String> allowedColumnValues) {
		super(awRequestAnnotator);
		if(null == allowedColumnValues || allowedColumnValues.isEmpty()) {
			throw new IllegalArgumentException("a non-null, non-empty column list is required");
		}
		_allowedColumnValues = allowedColumnValues;
	}
	
	/**
	 * @throws ValidatorException if the awRequest is not a NewDataPointQueryAwRequest
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating column list");
		
		if(! (awRequest instanceof SurveyResponseReadAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String columnListString = ((SurveyResponseReadAwRequest) awRequest).getColumnListString();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(columnListString)) {
			
			getAnnotator().annotate(awRequest, "empty column list found");
			return false;
		
		}
		
		// first check for the special "all users" value
		if("urn:ohmage:special:all".equals(columnListString)) {
			
			return true;
			
		} else {
			
			String[] columns = columnListString.split(",");
			
			if(columns.length > _allowedColumnValues.size()) {
				
				getAnnotator().annotate(awRequest, "more than " + _allowedColumnValues.size() + " users in query: " + columnListString);
				return false;
				
			} else {
				
				for(int i = 0; i < columns.length; i++) {
					
					if(! _allowedColumnValues.contains(columns[i])) {
						
						getAnnotator().annotate(awRequest, "found a disallowed column name: " + columns[i]);
						return false;
						
					}
				}
			}
		}
		
		return true;
	}
}
