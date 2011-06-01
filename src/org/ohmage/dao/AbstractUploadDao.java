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
package org.ohmage.dao;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import org.ohmage.request.AwRequest;



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
	 * Logs a duplicate record index to the AwRequest. An upload consists of many records in a JSON Array. A duplicate index is 
	 * an index into that array.
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
