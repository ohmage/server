package edu.ucla.cens.awserver.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.PreparedStatementCreator;

import edu.ucla.cens.awserver.datatransfer.AwRequest;
import edu.ucla.cens.awserver.domain.DataPacket;
import edu.ucla.cens.awserver.domain.PromptDataPacket;
import edu.ucla.cens.awserver.domain.PromptDataPacket.PromptResponseDataPacket;


/**
 * DAO for handling persistence of uploaded prompt data.
 * 
 * @author selsky
 */
public class PromptUploadDao extends AbstractUploadDao {
	private static Logger _logger = Logger.getLogger(PromptUploadDao.class);
	
	private final String _selectSql = "select id from prompt" +
			                          " where campaign_prompt_group_id = ?" +
			                          " and campaign_prompt_version_id = ?" +
			                          " and prompt_config_id = ?";
	
	private final String _insertSql = "insert into prompt_response" +
	                                  " (prompt_id, user_id, utc_time_stamp, utc_epoch_millis, phone_timezone," +
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
	public void execute(AwRequest request) {
		_logger.info("beginning prompt response persistence");
		
		List<DataPacket> dataPackets = (List<DataPacket>) request.getAttribute("dataPackets");
		if(null == dataPackets) {
			throw new IllegalArgumentException("no DataPackets found in the AwRequest");
		}
		
		int numberOfPackets = dataPackets.size();
		final int userId = request.getUser().getId();
		
		
		for(int i = 0; i < numberOfPackets; i++) {
			
			final PromptDataPacket promptDataPacket = (PromptDataPacket) dataPackets.get(i);
			List<PromptResponseDataPacket> promptResponses = promptDataPacket.getResponses();
			int numberOfResponses = promptResponses.size();
			
			for(int j = 0; j < numberOfResponses; j++) {
				
				final PromptResponseDataPacket response = promptResponses.get(j);
				final int promptId;
				
				try { // get the internal prompt_id -- the device uploading the data has it's own local configuration 
					  // (prompt_config_id) which has to be mapped to the prompt's "real" primary key
				
					 promptId = getJdbcTemplate().queryForInt(
						_selectSql, new Object[]{request.getAttribute("campaignPromptGroupId"), 
								                 request.getAttribute("campaignPromptVersionId"), 
								                 response.getPromptConfigId()}
				    );
					
				} catch(IncorrectResultSizeDataAccessException irsdae) { // the query did not return one row, a bad data problem.
					                                                     // it means that the device uploading data has multiple
					                                                     // prompts mapped to one prompt_config_id-group_id-version_id
					                                                     // combination
					
					throw new DataAccessException(irsdae);
					
				}
				catch(org.springframework.dao.DataAccessException dae) { // serious db problem (connectivity, config, etc)
					 
					throw new DataAccessException(dae);
				}
				 
				try { // Now insert the response -- auto committed by MySQL (the default setting)
					
					int numberOfRowsUpdated = getJdbcTemplate().update( 
						new PreparedStatementCreator() {
							public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
								PreparedStatement ps = connection.prepareStatement(_insertSql);
								ps.setInt(1, promptId);
								ps.setInt(2, userId);
								ps.setTimestamp(3, Timestamp.valueOf(promptDataPacket.getUtcDate()));
								ps.setLong(4, promptDataPacket.getUtcTime());
								ps.setString(5, promptDataPacket.getTimezone());
								ps.setDouble(6, promptDataPacket.getLatitude().equals(Double.NaN) ? null : promptDataPacket.getLatitude());
								ps.setDouble(7, promptDataPacket.getLongitude().equals(Double.NaN) ? null : promptDataPacket.getLongitude());
								ps.setString(8, response.getResponse());
								
								return ps;
							}
						}
				    );
					
					_logger.info("number of rows updated = " + numberOfRowsUpdated);
					
				}
				catch(DataIntegrityViolationException dive) { 
					
					if(isDuplicate(dive)) {
						
						_logger.info("found duplicate prompt response message");
						handleDuplicate(request, i);
						
					} else {
					
						// some other integrity violation occurred - bad!! All of the data to be inserted must be validated
						// before this DAO runs so there is either missing validation or somehow an auto_incremented key
						// has been duplicated
						throw new DataAccessException(dive);
					}
					
				}
				catch(org.springframework.dao.DataAccessException dae) { // some other bad db problem occurred that prevented the
					                                                     // SQL from completing normally 
					 
					throw new DataAccessException(dae);
				}
			}
		}
		
		_logger.info("successully persisted prompt response");
		
	}
}
