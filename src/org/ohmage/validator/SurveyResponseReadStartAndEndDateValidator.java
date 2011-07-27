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


/**
 * Validator that checks that both start date and end date exist. (If one exists, they both must).
 * 
 * @author selsky
 */
public class SurveyResponseReadStartAndEndDateValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(SurveyResponseReadStartAndEndDateValidator.class);
	
	public SurveyResponseReadStartAndEndDateValidator(AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
	}
	
	/**
	 * 
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating that the start and end date both exist or both don't exist");
		
		String startDate = awRequest.getStartDate();
		String endDate = awRequest.getEndDate();
		
		if((null != startDate && null != endDate) || (null == startDate && null == endDate)) {
			return true;
		}
		else {
			getAnnotator().annotate(awRequest, "Both the start and end date must be present or omitted. One alone is not allowed.");
			return false;
		}
	}
}
