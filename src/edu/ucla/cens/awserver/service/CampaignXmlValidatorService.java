package edu.ucla.cens.awserver.service;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.CampaignValidator;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;
import edu.ucla.cens.awserver.validator.json.FailedJsonRequestAnnotator;

/**
 * Validates the campaign XML.
 * 
 * @author John Jenkins
 */
public class CampaignXmlValidatorService extends AbstractAnnotatingService {
	private static Logger _logger = Logger.getLogger(CampaignXmlValidatorService.class);
	
	private CampaignValidator _validator;
	private String _schemaFileName;
	private boolean _required;
	
	/**
	 * Sets up this campaign validator service.
	 * 
	 * @param annotator The annotator to respond with if the XML is invalid.
	 * 
	 * @param validator The validator to use to do the validation.
	 * 
	 * @param schemaFileName The filename of the schema used for validation.
	 * 
	 * @param required Whether or not the XML file is required.
	 */
	public CampaignXmlValidatorService(AwRequestAnnotator annotator, CampaignValidator validator, String schemaFileName, boolean required) {
		super(annotator);
		
		_validator = validator;
		_schemaFileName = schemaFileName;
		_required = required;
	}

	/**
	 * Validates the XML.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		_logger.info("Validating campaign XML.");

		// Normally, in a service I would only check the toProcess map, but
		// this is an optimization to allow us to validate the XML as a last
		// step as it is the most expensive.
		String campaignXml;
		try {
			campaignXml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException outterException) {
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
		
		try {
			_validator.run(campaignXml, _schemaFileName);
			
			awRequest.addToProcess(InputKeys.XML, campaignXml, true);
		}
		catch(ValidityException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
		} 
		catch(SAXException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
		}
		catch(ParsingException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
		}
		catch(IllegalStateException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
		}
		catch(IllegalArgumentException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
		}
	}
}