package edu.ucla.cens.awserver.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import edu.ucla.cens.awserver.request.AwRequest;


/**
 * Abstract base class for DAOs that handle the upload feature. Handling duplicates is common across uploads so it is shared here.
 * 
 * @author selsky
 */
public abstract class AbstractUploadDao extends AbstractDao {
	
	public AbstractUploadDao(DataSource datasource) {
		super(datasource);
	}
	
	/**
	 * @return true if the Throwable represents an SQLException and the error code in the SQLException is 1062 (the MySQL error 
	 * code for dupes).
	 * @return false otherwise 
	 */
	protected boolean isDuplicate(Throwable t) {
		 return (t.getCause() instanceof SQLException) && (((SQLException) t.getCause()).getErrorCode() == 1062);
	}
	
	/**
	 * Logs a duplicate record index to the AwRequest. 
	 */
	protected void handleDuplicate(AwRequest awRequest, int duplicateIndex) {
		List<Integer> duplicateIndexList = awRequest.getDuplicateIndexList();
		if(null == duplicateIndexList) {
			duplicateIndexList = new ArrayList<Integer>();
			awRequest.setDuplicateIndexList(duplicateIndexList);
		}
		duplicateIndexList.add(duplicateIndex);
	}
}
