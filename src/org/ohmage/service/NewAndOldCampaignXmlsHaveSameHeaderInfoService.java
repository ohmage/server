package org.ohmage.service;

import java.io.IOException;
import java.io.StringReader;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.ohmage.dao.Dao;
import org.ohmage.dao.DataAccessException;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;
import org.ohmage.validator.AwRequestAnnotator;

/**
 * Checks that the currently stored XML and the new XML for a single campaign
 * contains the same URN and name.
 * 
 * @author John Jenkins
 */
public class NewAndOldCampaignXmlsHaveSameHeaderInfoService extends AbstractAnnotatingDaoService {
	private static final Logger _logger = Logger.getLogger(NewAndOldCampaignXmlsHaveSameHeaderInfoService.class);
	
	private final boolean _required;
	
	/**
	 * Builds this service.
	 * 
	 * @param annotator The annotator to use if there is a missmatch.
	 * 
	 * @param dao The DAO to use to get the original XML.
	 * 
	 * @param required Whether or not this validation is required.
	 */
	public NewAndOldCampaignXmlsHaveSameHeaderInfoService(AwRequestAnnotator annotator, Dao dao, boolean required) {
		super(dao, annotator);
		
		_required = required;
	}

	/**
	 * Ensures that the new XML and the old XML have the same campaign URN and
	 * the same campaign names.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		// Get the new campaign XML if it exists.
		String newXml;
		try {
			newXml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException e) {
			if(_required) {
				_logger.error("Missing required key: " + InputKeys.XML);
				throw new ServiceException(e);
			}
			else {
				return;
			}
		}
		
		// Run the DAO to load the original campaign XML.
		try {
			getDao().execute(awRequest);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
		
		// Pull out the original campaign XML.
		String originalXml;
		try {
			originalXml = (String) awRequest.getResultList().get(0);
		}
		catch(NullPointerException e) {
			_logger.error("The DAO didn't throw an error, but the result list was never set.");
			throw new ServiceException(e);
		}
		catch(IndexOutOfBoundsException e) {
			_logger.error("The DAO didn't throw an error and returned a result list, but the list was empty.");
			throw new ServiceException(e);
		}
		catch(ClassCastException e) {
			_logger.error("The DAO didn't throw an error and returned a non-empty result list, but the item in teh result list isn't of expected type String.");
			throw new ServiceException(e);
		}
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document originalDocument;
		Document newDocument;
		try {
			originalDocument = builder.build(new StringReader(originalXml));
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
		try {
			newDocument = builder.build(new StringReader(newXml));
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
		
		Element originalDocumentRoot = originalDocument.getRootElement();
		Element newDocumentRoot = newDocument.getRootElement();
		
		// Check that the URNs match.
		final String campaignUrnPath = "/campaign/campaignUrn";
		if(! originalDocumentRoot.query(campaignUrnPath).get(0).getValue().equals(newDocumentRoot.query(campaignUrnPath).get(0).getValue())) {
			getAnnotator().annotate(awRequest, "The document's campaign URNs don't match.");
			awRequest.setFailedRequest(true);
		}
		
		// Check that the names match.
		final String campaignNamePath = "/campaign/campaignName";
		if(! originalDocumentRoot.query(campaignNamePath).get(0).getValue().equals(newDocumentRoot.query(campaignNamePath).get(0).getValue())) {
			getAnnotator().annotate(awRequest, "The document's campaign names don't match.");
			awRequest.setFailedRequest(true);
		}
	}
}