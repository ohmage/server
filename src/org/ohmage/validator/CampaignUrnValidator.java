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

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;


/**
 * Checks that the URN is a valid URN.
 * 
 * @author John Jenkins
 */
public class CampaignUrnValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignUrnValidator.class);
	
	/**
	 * Basic constructor that takes an annotator to respond with if something
	 * fails.
	 * 
	 * @param annotator Annotates the error if one should arise.
	 */
	public CampaignUrnValidator(AwRequestAnnotator annotator) {
		super(annotator);
	}
	
	/**
	 * Checks that the URN in the request exists and is a valid URN.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating the campaign URN.");
		
		String urn = awRequest.getCampaignUrn();
		if(! StringUtils.isValidUrn(urn)) {
			awRequest.setFailedRequest(true);
			getAnnotator().annotate(awRequest, "Invalid URN.");
			return false;
		}

		return true;
	}
}
