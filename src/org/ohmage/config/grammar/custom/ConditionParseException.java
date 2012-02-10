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
package org.ohmage.config.grammar.custom;

import org.ohmage.config.grammar.parser.ParseException;

/**
 * Wraps the JavaCC ParseException in a more friendly container (RuntimeException instead of the standard Exception).
 * 
 * @author selsky
 */
public class ConditionParseException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	
	public ConditionParseException(String message, ParseException cause) {
		super(message, cause);
	}
	
	public ConditionParseException(String message, Throwable cause) {
		super(message, cause);
	}

}
