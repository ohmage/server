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
package org.ohmage.service;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.CampaignValidator;
import org.apache.log4j.Logger;
import org.ohmage.domain.ErrorResponse;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;
import org.ohmage.validator.json.FailedJsonRequestAnnotator;
import org.xml.sax.SAXException;

/**
 * Validates the campaign XML.
 * 
 * @author John Jenkins
 */
public class CampaignXmlValidatorService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignXmlValidatorService.class);
	
	private String _schemaFileName;
	private boolean _required;
	
	/**
	 * Sets up this campaign validator service.
	 * 
	 * @param annotator The annotator to respond with if the XML is invalid.
	 * 
	 * @param schemaFileName The filename of the schema used for validation.
	 * 
	 * @param required Whether or not the XML file is required.
	 */
	public CampaignXmlValidatorService(AwRequestAnnotator annotator, String schemaFileName, boolean required) {
		super(annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(schemaFileName)) {
			throw new IllegalArgumentException("The schema file's location cannot be null or whitespace only.");
		}
		
		_schemaFileName = schemaFileName;
		_required = required;
	}

	/**
	 * Validates the XML.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Normally, in a service I would only check the toProcess map, but
		// this is an optimization to allow us to validate the XML as a last
		// step as it is the most expensive.
		String campaignXml;
		try {
			campaignXml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException outerException) {
			try {
				campaignXml = (String) awRequest.getToValidateValue(InputKeys.XML);
			}
			catch(IllegalArgumentException innerException) {
				if(_required) {
					_logger.error("Request reached XML validation but is missing the required XML parameter.");
					throw new ServiceException("Missing XML in request.");
				}
				else {
					return;
				}
			}
		}
		
		_logger.info("Validating campaign XML.");
		
		ErrorResponse errorResponse = new ErrorResponse();
		errorResponse.setCode("0804");
		
		FailedJsonRequestAnnotator annotator = new FailedJsonRequestAnnotator(errorResponse);
		
		try {
			(new CampaignValidator()).run(campaignXml, _schemaFileName);
			
			awRequest.addToProcess(InputKeys.XML, campaignXml, true);
		}
		catch(ValidityException e) {
			awRequest.setFailedRequest(true);
			errorResponse.setText(e.getMessage());
			annotator.annotate(awRequest, e.getMessage());
		} 
		catch(SAXException e) {
			awRequest.setFailedRequest(true);
			errorResponse.setText(e.getMessage());
			annotator.annotate(awRequest, e.getMessage());
		}
		catch(ParsingException e) {
			awRequest.setFailedRequest(true);
			errorResponse.setText(e.getMessage());
			annotator.annotate(awRequest, e.getMessage());
		}
		catch(IllegalStateException e) {
			awRequest.setFailedRequest(true);
			errorResponse.setText(e.getMessage());
			annotator.annotate(awRequest, e.getMessage());
		}
		catch(IllegalArgumentException e) {
			awRequest.setFailedRequest(true);
			errorResponse.setText(e.getMessage());
			annotator.annotate(awRequest, e.getMessage());
		}
	}
}