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
package org.ohmage.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.domain.ChunkedMobilityQueryResult;
import org.ohmage.domain.MobilityQueryResult;
import org.ohmage.request.AwRequest;


/**
 * Service for placing mobility query results into 10 minute buckets.
 * 
 * @author Joshua Selsky
 */
public class ChunkedMobilityQueryService extends AbstractDaoService {
	private static Logger _logger = Logger.getLogger(ChunkedMobilityQueryService.class);
	private JSONObject _missingChunkObject;
	
	public ChunkedMobilityQueryService(Dao dao) {
		super(dao);
		_missingChunkObject = new JSONObject();
		try {
			_missingChunkObject.put("unknown", 10);
		} catch (JSONException jsone) {
			throw new IllegalStateException("cannot create JSON Object for 'empty chunk'");
		}
	}
	
	/**
	 * Dispatches to a DAO to query for mobility data and then collates the mobility modes into 10-minute chunks.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		try {
			getDao().execute(awRequest);
		} catch (DataAccessException dae) {
			throw new ServiceException(dae);
		}
		
		@SuppressWarnings("unchecked")
		List<MobilityQueryResult> results = (List<MobilityQueryResult>) awRequest.getResultList();
		List<ChunkedMobilityQueryResult> chunkedResults = new ArrayList<ChunkedMobilityQueryResult>();
		

		// TODO - make this class timezone-aware in its date calculations/comparisons - currently it assumes all data is in 
		// the same timezone as the running JVM and it also assumes that the data is all in the same timezone
		
		// First create all of the 10-minute buckets
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		long currentStartDate = 0L;
		long currentEndDate = 0L;
		long originalEndDate = 0L;
		
		try {
			currentStartDate = sdf.parse(awRequest.getStartDate()).getTime();
			currentEndDate = sdf.parse(awRequest.getEndDate()).getTime();
			originalEndDate = currentEndDate;
		} catch(ParseException pe) { // if this occurs there is a logical error in the parameter validation code
			_logger.error("cannot parse start or end date from query params, expected a date of the form 'yyyy-MM-dd'" +
					" start date: " + awRequest.getStartDate() + " end date: " + awRequest.getEndDate());
			throw new ServiceException(pe);
		}
		
		while(currentStartDate < originalEndDate) {
			ChunkedMobilityQueryResult cr = new ChunkedMobilityQueryResult();
			cr.setDuration(600000L); // 10 minute chunks
			cr.setTimestamp(new Timestamp(currentStartDate).toString());
			cr.setLocation(null);
			cr.setLocationStatus("unavailable");
			cr.setTimezone(TimeZone.getDefault().getID());
			cr.setUtcTimestamp(null);
			cr.setValue(_missingChunkObject);
			chunkedResults.add(cr);
			currentStartDate += 600000L;
		}
		
		// Now find the bucket for each result
		// Assume both lists (the new chunked list and the data returned from the db) are sorted by date
		
		int currentStartIndex = 0;
		
		for(ChunkedMobilityQueryResult cr : chunkedResults) {
			for(int i = currentStartIndex; i < results.size(); i++, currentStartIndex++) {
				if(Timestamp.valueOf(results.get(i).getTimestamp()).getTime() > (Timestamp.valueOf(cr.getTimestamp()).getTime() + 600000L)) {
					cr.calcModes();
					break;
				} else {
					cr.addItem(results.get(i));
				}
			}
		}
		
		results.clear();
		awRequest.setResultList(chunkedResults);
	}
}
