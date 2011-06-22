package org.ohmage.service;

import java.io.IOException;
import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.util.StringUtils;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Ensures that the parameterized prompt ID exists in the XML for the 
 * parameterized campaign.
 * 
 * @author John Jenkins
 */
public class PromptExistsService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(PromptExistsService.class);
	
	private final String _key;
	private final boolean _required;
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use if the prompt ID doesn't exist.
	 * 
	 * @param dao The DAO to call to get the XML.
	 * 
	 * @param key The key to use to get the prompt ID from the request.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public PromptExistsService(AwRequestAnnotator annotator, Dao dao, String key, boolean required) {
		super(dao, annotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(key)) {
			throw new IllegalArgumentException("The key cannot be null or whitespace only.");
		}
		
		_key = key;
		_required = required;
	}

	/**
	 * Gets the XML, then searches for a prompt with the parameterized prompt 
	 * ID.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the promptId.
		String promptId;
		try {
			promptId = (String) awRequest.getToProcessValue(_key);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				_logger.error("The required key is missing: " + _key);
				throw new ServiceException(e);
			}
			else {
				return;
			}
		}
		
		_logger.info("Validating that the prompt exists in the XML.");
		
		// Get the XML.
		try {
			getDao().execute(awRequest);
		}
		catch(IllegalArgumentException e) {
			throw new ServiceException(e);
		}
		String campaignXml = (String) awRequest.getResultList().get(0);
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(campaignXml));
		} catch (IOException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unable to read XML.", e);
			throw new DataAccessException("XML was unreadable.");
		} catch (ValidityException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Invalid XML.", e);
			throw new DataAccessException("XML was invalid.");
		} catch (ParsingException e) {
			// The XML should already have been validated, so this should
			// never happen.
			_logger.error("Unparcelable XML.", e);
			throw new DataAccessException("XML was unparcelable.");
		}
		
		// Find all the prompt IDs with the parameterized promptId.
		Element root = document.getRootElement();
		Nodes nodes = root.query("/campaign/surveys/survey/contentList/prompt[id='" + promptId + "']");
		if(nodes.size() == 0) {
			getAnnotator().annotate(awRequest, "No such prompt in the given campaign.");
			awRequest.setFailedRequest(true);
		}
	}
}