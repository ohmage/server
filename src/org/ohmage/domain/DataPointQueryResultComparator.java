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
package org.ohmage.domain;

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
