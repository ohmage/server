package edu.ucla.cens.awserver.dao;

import javax.sql.DataSource;

import edu.ucla.cens.awserver.request.AwRequest;

/**
 * Persist prompt responses to the db.
 * 
 * @author selsky
 */
public class SurveyResponsesUploadDao extends AbstractUploadDao {
	
	public SurveyResponsesUploadDao(DataSource dataSource) {
		super(dataSource);
	}
	
	@Override
	public void execute(AwRequest awRequest) {
		// TODO Auto-generated method stub

	}

}
