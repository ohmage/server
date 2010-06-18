package edu.ucla.cens.awserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;

import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptDataPacket;
import edu.ucla.cens.awserver.domain.PromptDataPacket.PromptResponseDataPacket;
import edu.ucla.cens.awserver.request.AwRequest;


/**
 * DAO for handling persistence of uploaded prompt data.
 * 
 * @author selsky
 */
public class PromptUploadDao extends AbstractUploadDao {
	private static Logger _logger = Logger.getLogger(PromptUploadDao.class);
	
	private final String _selectSql = "select prompt.id from prompt, campaign_prompt_group" +
			                          " where prompt.campaign_prompt_version_id = ?" +
			                          " and campaign_prompt_group.campaign_prompt_version_id = ?" +
			                          " and campaign_prompt_group.group_id = ?" +
			                          " and campaign_prompt_group.campaign_id = ?" +
			                          " and campaign_prompt_group.id = prompt.campaign_prompt_group_id" +
			                          " and prompt_config_id = ?";
	
	private final String _insertSql = "insert into prompt_response" +
	                                  " (prompt_id, user_id, time_stamp, epoch_millis, phone_timezone," +
	                                  " latitude, longitude, json_data) " +
	                                  " values (?,?,?,?,?,?,?,?)";
	
	public PromptUploadDao(DataSource datasource) {
		super(datasource);
	}
	
	/**
	 * Inserts prompt upload DataPackets into the DB.
	 * 
	 * @throws IllegalArgumentException if the AwRequest does not contain an attribute called dataPackets
	 */
	public void execute(AwRequest awRequest) {
		_logger.info("beginning prompt response persistence");
		
		List<DataPacket> dataPackets = awRequest.getDataPackets();
		if(null == dataPackets) {
			throw new IllegalArgumentException("no DataPackets found in the AwRequest");
		}
		
		int numberOfPackets = dataPackets.size();
		final int userId = awRequest.getUser().getId();
		
		for(int i = 0; i < numberOfPackets; i++) {
			
			final PromptDataPacket promptDataPacket = (PromptDataPacket) dataPackets.get(i);
			List<PromptResponseDataPacket> promptResponses = promptDataPacket.getResponses();
			int numberOfResponses = promptResponses.size();
			
			for(int j = 0; j < numberOfResponses; j++) {
				
				final PromptResponseDataPacket response = promptResponses.get(j);
				final int promptId;
				int currentPromptConfigId = response.getPromptConfigId(); // used for logging duplicates
				
				try { // get the internal prompt_id -- the device uploading the data has it's own local configuration 
					  // (prompt_config_id) which has to be mapped to the prompt's "real" primary key
				
					 promptId = getJdbcTemplate().queryForInt(
						_selectSql, new Object[]{ awRequest.getCampaignPromptVersionId(), 
								                  awRequest.getCampaignPromptVersionId(), 
								                  promptDataPacket.getGroupId(),
								                  awRequest.getUser().getCurrentCampaignId(),
								                  response.getPromptConfigId()}
				     );
					
				} catch(IncorrectResultSizeDataAccessException irse) { // the query did not return one row, a bad data problem.
					                                                   // it means that the device uploading data has multiple
					                                                   // prompts mapped to one prompt_config_id-group_id-version_id
					                                                   // combination
					_logger.error("caught IncorrectResultSizeDataAccessException (one row expected, but " + irse.getActualSize() +
							" returned) when running SQL '" + _selectSql + "' with the following parameters: " + 
							awRequest.getCampaignPromptVersionId() + ", " + 
							awRequest.getCampaignPromptVersionId() + ", " +
							promptDataPacket.getGroupId() + "," + 
							awRequest.getUser().getCurrentCampaignId() +  "," + 
							response.getPromptConfigId()
					); 
					
					throw new DataAccessException(irse);
					
				}
				catch(org.springframework.dao.DataAccessException dae) { // serious db problem (connectivity, config, etc)
					
					_logger.error("caught DataAccessException when running SQL '" + _selectSql + "' with the following " +
						"parameters: " + awRequest.getCampaignPromptGroupId() + ", " + 
						awRequest.getCampaignPromptVersionId() + ", " +
						response.getPromptConfigId()); 
					
					throw new DataAccessException(dae);
				}
				 
				try { // Now insert the response -- auto committed by MySQL (the default setting)
					
					int numberOfRowsUpdated = getJdbcTemplate().update( 
						new PreparedStatementCreator() {
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps = connection.prepareStatement(_insertSql);
								ps.setInt(1, promptId);
								ps.setInt(2, userId);
								ps.setTimestamp(3, Timestamp.valueOf(promptDataPacket.getDate()));
								ps.setLong(4, promptDataPacket.getEpochTime());
								ps.setString(5, promptDataPacket.getTimezone());
								
								// MySQL will not accept Double.NaN so null is used in its place
								
								if(promptDataPacket.getLatitude().isNaN()) {
									ps.setNull(6, Types.DOUBLE);
								} else {
									ps.setDouble(6, promptDataPacket.getLatitude());
								}
								
								if(promptDataPacket.getLongitude().isNaN()) {
									ps.setNull(7, Types.DOUBLE);
								} else {
									ps.setDouble(7, promptDataPacket.getLongitude());
								}
								
								ps.setString(8, response.getResponse());
								
								return ps;
							}
						}
				    );
					
					if(_logger.isDebugEnabled()) {
						_logger.debug("number of rows updated = " + numberOfRowsUpdated);
					}
					
				}
				catch(DataIntegrityViolationException dive) { 
					
					if(isDuplicate(dive)) {
						
						if(_logger.isDebugEnabled()) {
							_logger.debug("found duplicate prompt response message");
						}
						handleDuplicate(awRequest, i, currentPromptConfigId);
						
					} else {
					
						// some other integrity violation occurred - bad!! All of the data to be inserted must be validated
						// before this DAO runs so there is either missing validation or somehow an auto_incremented key
						// has been duplicated
						
						_logger.error("caught DataIntegrityViolationException when running SQL '" + _insertSql + "' with the " +
							"following parameters: " + promptId + ", " + userId + ", " + promptDataPacket.getDate() + ", " + 
							promptDataPacket.getEpochTime() + ", " + promptDataPacket.getTimezone() + ", " +
							(promptDataPacket.getLatitude().equals(Double.NaN) ? "null" : promptDataPacket.getLatitude()) +  ", " +
							(promptDataPacket.getLongitude().equals(Double.NaN) ? "null" : promptDataPacket.getLongitude()) + ", " +
							response.getResponse());
						
						throw new DataAccessException(dive);
					}
					
				}
				catch(org.springframework.dao.DataAccessException dae) { // some other bad db problem occurred that prevented the
					                                                     // SQL from completing normally 
					 
					_logger.error("caught DataAccessException when running SQL '" + _insertSql + "' with the " +
							"following parameters: " + promptId + ", " + userId + ", " + promptDataPacket.getDate() + ", " + 
							promptDataPacket.getEpochTime() + ", " + promptDataPacket.getTimezone() + ", " +
							(promptDataPacket.getLatitude().equals(Double.NaN) ? "null" : promptDataPacket.getLatitude()) +  ", " +
							(promptDataPacket.getLongitude().equals(Double.NaN) ? "null" : promptDataPacket.getLongitude()) + ", " +
							response.getResponse());
						
					throw new DataAccessException(dae);
				}
			}
		}
		
		_logger.info("successully persisted prompt response");
		
	}
	
	private void handleDuplicate(AwRequest awRequest, int duplicateIndex, int duplicatePromptConfigId) {
		handleDuplicate(awRequest, duplicateIndex);
		
		Map<Integer, List<Integer>> duplicatePromptResponseMap = awRequest.getDuplicatePromptResponseMap();
		List<Integer> promptConfigIdList = null;
		
		if(null == duplicatePromptResponseMap) {
			duplicatePromptResponseMap = new HashMap<Integer, List<Integer>>();
			awRequest.setDuplicatePromptResponseMap(duplicatePromptResponseMap);
		} else {
			promptConfigIdList = duplicatePromptResponseMap.get(duplicateIndex);
		}
		
		if(null == promptConfigIdList) {
			promptConfigIdList = new ArrayList<Integer>();
			duplicatePromptResponseMap.put(duplicateIndex, promptConfigIdList);
		}
		
		promptConfigIdList.add(duplicatePromptConfigId);
	}
}
