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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.SurveyResponseReadAwRequest;


/**
 * Compare the start and end dates for a new data point query to make sure they are not longer than a specified period. 
 * 
 * TODO make this applicable to other types of requests
 * 
 * @author selsky
 */
public class DateRangeValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(DateRangeValidator.class);
	private long _numberOfMilliseconds;
	
	public DateRangeValidator(AwRequestAnnotator annotator, long numberOfMilliseconds) {
		super(annotator);
		_numberOfMilliseconds = numberOfMilliseconds;
	}
	
	/**
	 * Assumes both a start date and an end date exist in the request and determines if they fall within the range set on 
	 * construction. 
	 */
	public boolean validate(AwRequest awRequest) {
		_logger.info("validating date range");
		
		SurveyResponseReadAwRequest req = (SurveyResponseReadAwRequest) awRequest; 
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		formatter.setLenient(false);
		Date startDate = null;
		Date endDate = null;
		
		try {
		
			startDate = formatter.parse(req.getStartDate());
			endDate = formatter.parse(req.getEndDate());
		
		} catch (ParseException pe) {
			
			_logger.error("could not parse date", pe);
			throw new ValidatorException(pe);
		
		}
		
		long t = endDate.getTime() - startDate.getTime();
		
		if(t > _numberOfMilliseconds || t < 0) {
			
			getAnnotator().annotate(awRequest, "invalid date range");
			return false;	
		}
		
		return true;
	}
}
