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
package org.ohmage.dao;

import java.io.IOException;
import java.io.StringReader;

import javax.sql.DataSource;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.Logger;
import org.ohmage.request.AwRequest;
import org.ohmage.request.InputKeys;


/**
 * Validates the contents of the XML with regards to what is in the database 
 * to ensure that not collisions will take place.
 * 
 * @author John Jenkins
 */
public class CampaignAlreadyExistsValidationDao extends AbstractDao {
	private static Logger _logger = Logger.getLogger(CampaignAlreadyExistsValidationDao.class);
	
	private static final String SQL = "SELECT count(*) " +
									  "FROM campaign " +
									  "WHERE urn = ?";

	/**
	 * Sets up the data source for this DAO.
	 * 
	 * @param dataSource The data source that will be used to query the
	 * 					 database for information.
	 */
	public CampaignAlreadyExistsValidationDao(DataSource dataSource) {
		super(dataSource);
	}
	
	/**
	 * Parses the XML for the campaign information, checks it against the
	 * database, and makes sure that no collisions will take place.
	 */
	@Override
	public void execute(AwRequest awRequest) {
		String campaignXml;
		try {
			campaignXml = (String) awRequest.getToProcessValue(InputKeys.XML);
		}
		catch(IllegalArgumentException e) {
			_logger.info("No campaign XML in the toProcess map, so skipping service validation.");
			return;
		}
		
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
		
		Element root = document.getRootElement();
		String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue();
		
		try {
			int others = getJdbcTemplate().queryForInt(SQL, new Object[] { campaignUrn });
			
			if(others != 0) {
				awRequest.setFailedRequest(true);
			}
		}
		catch(org.springframework.dao.DataAccessException e) {
			_logger.error("An error occurred while running the following SQL '" + SQL + "' with the parameter: " + campaignUrn);
			throw new DataAccessException(e);
		}
	}
}
