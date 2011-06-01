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
 * Handler for annotating an AwRequest when post-processing validation. 
 * 
 * @author selsky
 */
public interface AwRequestAnnotator {
	
	/**
	 * Annotates the AwRequest with the provided message. Implementations of this interface are free to annotate the 
	 * request in ways specific to a feature or usage scenario (e.g., failed validation, creation of custom error messages, etc). 
	 */
	public void annotate(AwRequest awRequest, String message);
	
}
