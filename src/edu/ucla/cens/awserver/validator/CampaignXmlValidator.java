package edu.ucla.cens.awserver.validator;

import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.andwellness.config.xml.CampaignValidator;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import edu.ucla.cens.awserver.request.AwRequest;
import edu.ucla.cens.awserver.request.InputKeys;
import edu.ucla.cens.awserver.validator.json.FailedJsonRequestAnnotator;

public class CampaignXmlValidator extends AbstractAnnotatingValidator {
	private static Logger _logger = Logger.getLogger(CampaignXmlValidator.class);
	
	private CampaignValidator _validator;
	private String _schemaFileName;
	private boolean _required;
	
	/**
	 * Creates a validator for the XML file that defines a campaign.
	 * 
	 * @param annotator The annotator for the message if this should fail.
	 * 
	 * @param valiator The validator for the incomming XML file.
	 */
	public CampaignXmlValidator(AwRequestAnnotator annotator, CampaignValidator validator, String schemaFileName, boolean required) {
		super(annotator);
		
		_validator = validator;
		_schemaFileName = schemaFileName;
		_required = required;
	}
	
	/**
	 * Validates the XML and annotates if there is an error.
	 */
	@Override
	public boolean validate(AwRequest awRequest) {
		_logger.info("Validating campaign XML.");

		String campaignXml = (String) awRequest.getToValidate().get(InputKeys.XML);
		if(campaignXml == null) {
			if(_required) {
				_logger.error("Request reached XML validation but is missing the required XML parameter.");
				throw new ValidatorException("Missing XML in request.");
			}
			else {
				return true;
			}
		}
		
		try {
			_validator.run(campaignXml, _schemaFileName);
		}
		catch(ValidityException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
			return false;
		} 
		catch(SAXException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
			return false;
		}
		catch(ParsingException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
			return false;
		}
		catch(IllegalStateException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
			return false;
		}
		catch(IllegalArgumentException e) {
			awRequest.setFailedRequest(true);
			((FailedJsonRequestAnnotator) getAnnotator()).appendErrorText(" " + e.getMessage());
			getAnnotator().annotate(awRequest, e.getMessage());
			return false;
		}
		
		awRequest.addToProcess(InputKeys.XML, campaignXml, true);
		return true;
	}
}
