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
package org.ohmage.validator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import org.ohmage.util.StringUtils;


public abstract class AbstractAnnotatingRegexpValidator extends AbstractAnnotatingValidator {
	protected Pattern _regexpPattern;
	
	/**
	 * Creates an instance of this class the provided regexp for validation.
	 *
	 * @throws IllegalArgumentException if the passed in regexp is null, empty, or all whitespace
	 * @throws PatternSyntaxException if the passed in regexp is invalid
	 */
	public AbstractAnnotatingRegexpValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(regexp)) {
			throw new IllegalArgumentException("a null, empty, or all-whitespace string is not allowed");
		}
		
		_regexpPattern = Pattern.compile(regexp);
	}
}
