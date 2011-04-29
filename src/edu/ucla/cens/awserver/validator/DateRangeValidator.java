package edu.ucla.cens.awserver.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.SurveyResponseReadAwRequest;

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
