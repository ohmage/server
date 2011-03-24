package edu.ucla.cens.awserver.service;

import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.dao.DataAccessException;
import edu.ucla.cens.awserver.domain.ChunkedMobilityQueryResult;
import edu.ucla.cens.awserver.domain.MobilityQueryResult;
import edu.ucla.cens.awserver.request.AwRequest;

/**
 * @author selsky
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
		
		// TODO - make this more efficient and less brute force
		// TODO - make this class timezone-aware in its date calculations/comparisons - currently it assumes all data is in 
		// the same timezone as the running JVM and it also assumes that the data is all in the same timezone
		
		// First create all of the 10-minute buckets
		
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		sdf.setLenient(false);
		long currentStartDate = 0L;
		long currentEndDate = 0L;
		long originalStartDate = 0L;
		long originalEndDate = 0L;
		
		try {
			currentStartDate = sdf.parse(awRequest.getStartDate()).getTime();
			originalStartDate = currentStartDate;
			currentEndDate = sdf.parse(awRequest.getEndDate()).getTime();
			originalEndDate = currentEndDate;
		} catch(ParseException pe) { // if this occurs there is a logical error in the parameter validation code
			_logger.error("cannot parse start or end date from query params, expected a date of the form 'yyyy-MM-dd'" +
					" start date: " + awRequest.getStartDate() + " end date: " + awRequest.getEndDate());
			throw new ServiceException(pe);
		}
		
		while(currentStartDate <= originalEndDate) {
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
