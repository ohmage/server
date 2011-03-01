package edu.ucla.cens.awserver.service;

import java.util.Map;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.DataPointFunctionQueryAwRequest;

/**
 * @author selsky
 */
public class DataPointFunctionQueryService implements Service {
	private Map<String, Service> _services;
	
	public DataPointFunctionQueryService(Map<String, Service> services) {
		if(null == services || services.isEmpty()) {
			throw new IllegalArgumentException("the map of services cannot be null or empty");
		}
		_services = services;
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		DataPointFunctionQueryAwRequest req	= (DataPointFunctionQueryAwRequest) awRequest;
		String functionName = req.getFunctionName(); 
		Service service = _services.get(functionName);
		if(null == service) { // this is a logical/configuration error
			throw new IllegalStateException("service not found for function " + functionName);
		}
		service.execute(awRequest);
	}
}
