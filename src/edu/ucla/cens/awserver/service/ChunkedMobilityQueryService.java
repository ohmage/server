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
			
//			ChunkedMobilityQueryResult currentChunkedResult = new ChunkedMobilityQueryResult();
//			int count = 0, still = 0, walk = 0, run = 0, bike = 0, drive = 0;
//			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
//			sdf.setLenient(false);
//			long currentStartDate = 0L;
//			
//			try {
//				currentStartDate = sdf.parse(awRequest.getStartDate()).getTime();
//			} catch (ParseException pe) { // if this occurs there is a logical error in the parameter validation code
//				_logger.error("found an unparseable timestamp from the query params. expected a timestamp with the format of " +
//					"\"yyyy-MM-dd hh:mm:ss\". found the following value: "+ awRequest.getStartDate());
//				throw new IllegalStateException(pe);
//			}
//			
//			long currentEndDate = currentStartDate += 600000L;
//						
//			for(MobilityQueryResult result : results) { 
//				
//				
//				
//				
//				while(Timestamp.valueOf(result.getTimestamp()).getTime() < currentStartDate || 
//						Timestamp.valueOf(result.getTimestamp()).getTime() > currentEndDate) {
//					
//					ChunkedMobilityQueryResult cmr = new ChunkedMobilityQueryResult();
//					cmr.setDuration(6000000L);
//					cmr.setLocation(null);
//					cmr.setLocationStatus("unvailable");
//					cmr.setTimestamp(new Timestamp(currentStartDate).toString());
//					cmr.setValue(_missingChunkObject);
//					chunkedResults.add(cmr);
//					currentStartDate += 600000L;
//					currentEndDate += 600000L;
//				}
//				
//				if(0 == count) {
//					startTimestamp = result.getTimestamp();
//					currentChunkedResult.setLocation(result.getLocation());
//					currentChunkedResult.setLocationStatus(result.getLocationStatus());
//					currentChunkedResult.setTimestamp(result.getTimestamp());
//					currentChunkedResult.setTimezone(result.getTimezone());
//				}
//				
//				count++;
//				
//				if("still".equals(result.getValue())) {
//					still++;
//				} else if("walk".equals(result.getValue())) {
//					walk++;
//				} else if("run".equals(result.getValue())) {
//					run++;
//				} else if("bike".equals(result.getValue())) {
//					bike++;
//				} else if("drive".equals(result.getValue())) {
//					drive++;
//				} else {
//					_logger.warn("unknown mobility mode found: " + result.getValue());
//				}
//				
//				if(10 == i || (i == (results.size() - 1)) ) {
//					
//					JSONObject o = new JSONObject();
//					
//					try {
//						o.put("still", still);
//						o.put("walk", walk);
//						o.put("run", run);
//						o.put("bike", bike);
//						o.put("drive", drive);
//					} catch (JSONException jsone) { // can never happen unless there is a logical error in the try block
//						_logger.warn(jsone);
//					}
//					
//					currentChunkedResult.setValue(o);
//					currentChunkedResult.setDuration(calcDuration(startTimestamp, currentChunkedResult.getTimestamp()));
//					chunkedResults.add(currentChunkedResult);
//					
//					count = 0; still = 0; walk = 0; run = 0; bike = 0; drive = 0;
//					currentChunkedResult = new ChunkedMobilityQueryResult();
//				}
//			}
//			
//			results.clear();
//			awRequest.setResultList(chunkedResults);
//			
//		} catch (DataAccessException dae) {
//			throw new ServiceException(dae);
//		}
	}
	
//	private long calcDuration(String t1, String t2) {
//		return Timestamp.valueOf(t2).getTime() - Timestamp.valueOf(t1).getTime();
//	}
}
