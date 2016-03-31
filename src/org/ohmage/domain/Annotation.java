/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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

import java.util.TimeZone;
import java.util.UUID;

import org.apache.log4j.Logger;
import org.joda.time.format.ISODateTimeFormat;
import org.json.JSONException;
import org.json.JSONObject;
import org.ohmage.exception.DomainException;
import org.ohmage.util.StringUtils;

/**
 * An annotation has an id, some text and date-time info. Annotations may be
 * attached to survey responses or prompts responses. Future support is planned
 * for annotating campaigns and annotations.
 * 
 * @author Joshua Selsky
 */
public class Annotation {
	private static final Logger LOGGER = Logger.getLogger(Annotation.class);
	
	private static final String JSON_KEY_TEXT = "text";
	private static final String JSON_KEY_ID = "annotation_id";
	private static final String JSON_KEY_TIME = "time";
	private static final String JSON_KEY_TIMEZONE = "timezone";
	private static final String JSON_KEY_AUTHOR = "author";

	
	private UUID id;
	private String text;
	private Long epochMillis;
	private TimeZone timezone;
	private String author;
	
	/**
	 * Creates an Annotation using the provided values.
	 * 
	 * @param id a UUID
	 * @param text the annotation text
	 * @param epochMillis the UNIX epoch millis
	 * @param timezone the timezone on the annotation
	 * @throws DomainException if any of the input is missing or malformed
	 */
	public Annotation(String id, String text, Long epochMillis, String timezone, String author) throws DomainException {
		UUID tUuid = null;
		
		try {
			tUuid = UUID.fromString(id);
		}
		catch(IllegalArgumentException e) {
			throw new DomainException("The id is not a valid UUID.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(text)) {
			throw new DomainException("Annotation text is required.");
		}
		if(epochMillis == null) {
			throw new DomainException("A epoch milliseconds value is required.");
		}
		if(StringUtils.isEmptyOrWhitespaceOnly(timezone)) {
			throw new DomainException("A timezone is required.");
		}
		
		this.id = tUuid;
		this.text = text;
		this.epochMillis = epochMillis;
		this.timezone = TimeZone.getTimeZone(timezone);
		this.author = author;
	}
	
	public Long getEpochMillis() {
		return epochMillis;
	}
	
	public UUID getId() {
		return id;
	}
	
	public String getText() {
		return text;
	}
	
	public TimeZone getTimezone() {
		return timezone;
	}

	public String getAuthor() {
		return author;
	}
	
	@Override
	public String toString() {
		return "Annotation [id=" + id + ", text=" + text + ", epochMillis="
				+ epochMillis + ", timezone=" + timezone + "]";
	}

	/**
	 * A JSONObject representing the available information from this annotation.
	 * 
	 * @return A JSONObject representing this object. If there is an error 
	 * 		   building this object, null is returned.
	 */
	public JSONObject toJson() {
		try {
			JSONObject result = new JSONObject();
			
			result.put(JSON_KEY_ID, id);
			result.put(JSON_KEY_TEXT, text);
			result.put(JSON_KEY_TIME, epochMillis);
			result.put(JSON_KEY_TIMEZONE, timezone.getID());
			result.put(JSON_KEY_AUTHOR, author);
			
			return result;
		}
		catch(JSONException e) {
			LOGGER.error("Error building the JSONObject.", e);
			return null;
		}
	}
}
