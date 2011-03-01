package edu.ucla.cens.awserver.domain;

import java.util.Comparator;

/**
 * Comparator to allow lists of DataPointQueryResults to be sorted by surveyId, UTC timestamp, and displayType. This 
 * comparator is inconsistent with typical implementations of equals() where the equality is determined by all of a class's 
 * instance variables.
 * 
 * @author selsky
 */
public class DataPointQueryResultComparator implements Comparator<DataPointQueryResult> {

	@Override
	public int compare(DataPointQueryResult a, DataPointQueryResult b) {
		if(null == a || null == b) {
			throw new NullPointerException("cannot compare null");
		}
		if(null == a.getSurveyId() || null == b.getSurveyId()) {
			throw new NullPointerException("cannot compare null survey ids");
		}
		if(null == a.getDisplayType() || null == b.getDisplayType()) {
			throw new NullPointerException("cannot compare null display types");
		}
		
		int x = a.getSurveyId().compareTo(b.getSurveyId());
		
		if(0 == x) {
			
			int y = a.getUtcTimestamp().compareTo(b.getUtcTimestamp()); // ok to lexicographically compare ISO timestamps
			
			if(0 == y) {
				
				// metadata items are greater than any display type and all other display types are treated equally
				if("metadata".equals(a.getDisplayType())) {
					return 1;
				} else if("metadata".equals(b.getDisplayType())) {
					return -1;
				} else {
					return 0;
				}
				
			} else {
				
				return y;
			}
		}
		
		return x;
	}
}
