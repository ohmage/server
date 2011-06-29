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

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;


/**
 * Validator that checks the adherence of the sort_order parameter to the rules specified by the survey response read API. The 
 * allowed sort_order is user, timestamp, survey, in any order. 
 * 
 * @author Joshua Selsky
 */
public class SortOrderValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(SortOrderValidator.class);
	
	public SortOrderValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}

	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the sort_order parameter");
		
		String sortOrder = (String) awRequest.getToValidate().get(InputKeys.SORT_ORDER);
		
		if((null != sortOrder) && (! "".equals(sortOrder))) { // this parameter is always optional
		
			String[] splitSortOrder = sortOrder.split(InputKeys.LIST_ITEM_SEPARATOR);
			
			if(splitSortOrder.length != 3) {
				
				getAnnotator().annotate(awRequest, "sort_order is too long or malformed: " + sortOrder);
				return false;
			}
			
			for(String sortParameter : splitSortOrder) {
				
				if(! InputKeys.SORT_ORDER_USER.equals(sortParameter) 
						&& ! InputKeys.SORT_ORDER_TIMESTAMP.equals(sortParameter) 
						&& ! InputKeys.SORT_ORDER_SURVEY.equals(sortParameter)) {
					
					getAnnotator().annotate(awRequest, "sort_order contains an invalid sort parameter: " + sortParameter);
					return false;
				}
			}
		}
		
		return true;
	}

}
