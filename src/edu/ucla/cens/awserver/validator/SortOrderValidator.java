package edu.ucla.cens.awserver.validator;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;

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
		_logger.info("validating the sort_order parameter");
		
		String sortOrder = (String) awRequest.getToValidate().get(InputKeys.SORT_ORDER);
		
		if(null != sortOrder) { // this parameter is always optional
		
			String[] splitSortOrder = sortOrder.split(",");
			
			if(splitSortOrder.length != 3) {
				
				getAnnotator().annotate(awRequest, "sort_order is too long or malformed: " + sortOrder);
				return false;
			}
			
			for(String sortParameter : splitSortOrder) {
				
				if(! "user".equals(sortParameter) && ! "timestamp".equals(sortParameter) && ! "survey".equals(sortParameter)) {
					
					getAnnotator().annotate(awRequest, "sort_order contains an invalid sort parameter: " + sortParameter);
					return false;
				}
			}
		}
		
		return true;
	}

}
