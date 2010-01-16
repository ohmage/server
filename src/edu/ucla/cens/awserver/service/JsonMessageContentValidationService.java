package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.datatransfer.AwRequest;

/**
 * TODO - see comment above execute()
 * 
 * @author selsky
 */
public class JsonMessageContentValidationService extends AbstractDaoService {
	private String _errorMessage;
	
	public JsonMessageContentValidationService(Dao dao, String errorMessage) {
		super(dao);
		if(null == errorMessage) {
			throw new IllegalArgumentException("a non-null error message is required");
		}
		_errorMessage = errorMessage;
	}
	
	/**
	 * TODO depending on outcome of future meetings, place packet-level validation in this method
	 * 
	 * - validation for elements which must occur in all packets
	 * -- handle Double.NaN for latitude and longitude (MySQL will insert 0 so make it null instead) 
	 * - validation for mobility-specific messages (mode_only & mode_features)
	 * - validation for prompt-specific messages
	 * -- each prompt type has a data type in the db (therefore an inferrable validation rule)
	 */
	public void execute(AwRequest awRequest) {
	

	}

}
