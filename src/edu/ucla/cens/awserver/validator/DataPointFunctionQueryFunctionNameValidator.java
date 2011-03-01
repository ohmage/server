package edu.ucla.cens.awserver.validator;

import java.util.List;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointFunctionQueryAwRequest;

/**
 * Validates the functionId from the AwRequest.
 * 
 * @author selsky
 */
public class DataPointFunctionQueryFunctionNameValidator extends AbstractAnnotatingValidator {
	private List<String> _functionIds;
	
	public DataPointFunctionQueryFunctionNameValidator(AwRequestAnnotator awRequestAnnotator, List<String> functionIds) {
		super(awRequestAnnotator);
		
		if(null == functionIds || functionIds.isEmpty()) {
			throw new IllegalArgumentException("the list of function ids cannot be null or empty");
		}
		
		_functionIds = functionIds;
	}
	
	public boolean validate(AwRequest awRequest) {
		DataPointFunctionQueryAwRequest req = (DataPointFunctionQueryAwRequest) awRequest; 
		
		if(null == req.getFunctionName()) { // logical error!
			throw new ValidatorException("functionName missing from AwRequest");
		}
		
		if(! _functionIds.contains(req.getFunctionName())) {
			getAnnotator().annotate(awRequest, "invalid function name found: " + req.getFunctionName());
			return false;
		}
		
		return true;
	}
}
