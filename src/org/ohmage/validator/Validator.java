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

import org.ohmage.request.AwRequest;

/**
 * Implementations of this interface are intended to validate some portion of an AwRequest instance.
 * 
 * @author selsky
 */
public interface Validator {
	
	/**
	 * Runs validation against attributes of the provided AwRequest.
	 */
	public boolean validate(AwRequest awRequest);
	
}
