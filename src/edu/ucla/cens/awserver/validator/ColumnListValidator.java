package edu.ucla.cens.awserver.validator;

import java.util.List;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.NewDataPointQueryAwRequest;
import edu.ucla.cens.awserver.util.StringUtils;

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
		_logger.info("validating column list");
		
		if(! (awRequest instanceof NewDataPointQueryAwRequest)) { // lame
			throw new ValidatorException("awRequest is not a NewDataPointQueryAwRequest: " + awRequest.getClass());
		}
		
		String columnListString = ((NewDataPointQueryAwRequest) awRequest).getColumnListString();
		
		if(StringUtils.isEmptyOrWhitespaceOnly(columnListString)) {
			
			getAnnotator().annotate(awRequest, "empty column list found");
			return false;
		
		}
		
		// first check for the special "all users" value
		if("urn:awm:special:all".equals(columnListString)) {
			
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
