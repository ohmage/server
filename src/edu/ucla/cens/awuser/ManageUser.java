package edu.ucla.cens.awuser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import jbcrypt.BCrypt;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * Procedural utility class for adding or removing a user within the AW database. The lazy registration feature. Eventually
 * this should be refactored into a real registration feature. 
 * 
 * @author selsky
 */
public class ManageUser {
	private static Logger _logger = Logger.getLogger(ManageUser.class);
		
	/**
	 *  Adds or removes a user from the AW database depending on the command line options provided. Run with "help" as the first
	 *  argument for an explanation and an example.
	 *  
	 *  @throws IllegalArgumentException if any required values are missing from the command line or the input file
	 *  @throws IllegalArgumentException if the userName is malformed (too short or contains illegal characters)
	 *  @throws IOException if the input file does not exist or if there are any other IO problems
	 *  @throws JSONException if the json property in the input file contains malformed JSON
	 *  @throws DataAccessException for database access problems (see individual method calls below)
	 *  @throws IncorrectResultSizeDataAccessException for logical database problems (see individual method calls below)
	 */
	public static void main(String args[]) throws IOException, JSONException {
		// Configure log4j. (pointing to System.out)
		// All Spring messages are at the DEBUG level. They are extremely informative (and verbose).
		BasicConfigurator.configure();
		
		if(args.length > 0  && args[0].equals("help")) {
			_logger.info(_helpText);
			System.exit(0);
		}
		
		if(args.length < 2 || args.length > 4) {
			_logger.info("Incorrect number of arguments");
			_logger.info(_helpText);
			System.exit(0);
		}
		
		String fileName = null;
		String function = null;
		String salt = null;
		boolean quiet = false;
		
		if(args.length == 2) {
			if(! "remove".equals(args[0])) {
				throw new IllegalArgumentException("remove is required as the first argument. You provided " + args[0]);
			}
			function = args[0];
			fileName = args[1];
		}
		
		if(args.length == 3) {
			if(! "add".equals(args[0])) {
				throw new IllegalArgumentException("add is required as the first argument. You provided " + args[0]);
			}
			function = args[0];
			fileName = args[1];
			salt = args[2];
			
		}
		
		if(args.length == 4) {
			if(! ("quiet".equals(args[0]))) {
				throw new IllegalArgumentException("quiet is required as the first argument. You provided " + args[0]);
			} else {
				quiet = true;
			}
			if(! "add".equals(args[1])) {
				throw new IllegalArgumentException("add is required as the second argument. You provided " + args[1]);
			}
			
			function = args[1];
			fileName = args[2];
			salt = args[3];
		}
		
		if(quiet) {
			Logger.getLogger("org.springframework").setLevel(Level.INFO);
		}
		
		BufferedReader in = new BufferedReader(new FileReader(fileName)); 
		Properties props = new Properties();
		props.load(in);
		in.close();

		checkProperty(props, "userName");
		checkProperty(props, "password");
		checkProperty(props, "subdomain");
		checkProperty(props, "emailAddress");
		checkProperty(props, "json");
		checkProperty(props, "dbUserName");
		checkProperty(props, "dbPassword");
		checkProperty(props, "dbDriver");
		checkProperty(props, "dbJdbcUrl");
		
		// perform detailed check on userName and password
		String userName = props.getProperty("userName");
		String password = props.getProperty("password");
		
		
//		if(userName.length() < 9) {
//			throw new IllegalArgumentException("userName must be longer than eight characters");
//		}
		
		Pattern pattern = Pattern.compile("[a-z\\.]{9,15}");
		if(! pattern.matcher(userName).matches()) {
			throw new IllegalArgumentException("user names can only be between 9 and 15 characters in length and can only contain" +
			" letters and the dot character");
		}
		
		if(! pattern.matcher(password).matches()) {
			throw new IllegalArgumentException("passwords can only be between 9 and 15 characters in length and can only contain" +
			" letters and the dot character");
		}
		
//		String validChars = "abcdefghijklmnopqrstuvwxyz.";
//		
//		for(int i = 0; i < userName.length(); i++) {
//			CharSequence subSequence = userName.subSequence(i, i + 1);
//			
//			if(! validChars.contains(subSequence)) {
//				throw new IllegalArgumentException("userName contains an illegal character: " + subSequence);
//			}
//		}
		
		JSONObject json = new JSONObject(props.getProperty("json"));
		// these calls are analogous to checkProperty() above except JSONObject will throw a JSONException if the key 
		// does not exist
		json.getString("first_name");
		json.getString("last_name");
		
		// ok, now some actual work can be done
		if("add".equals(function)) {
			
			addUser(props, salt);
			
		} else { 
			
			removeUser(props);			
		}
		
		_logger.info("completed successfully");
	}
	
	
	/**
	 * Adds a user to the database using the provided Properties.
	 * 
	 * @throws DataAccessException if any errors occur while interacting with the database
	 */
	private static void addUser(final Properties props, final String salt) {
		_logger.info("Adding user with the following properties: " + props);
		
		BasicDataSource dataSource = getDataSource(props);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		try { 
			
			final int campaignId = findCampaignForSubdomain(jdbcTemplate, props);
			
			// Execute the rest of the queries inside a transaction
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("Transaction wrapping an insert into user, user_personal, user_user_personal, and user_role_campaign");
			
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
			TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
			
			try {
				//
				// 1. Insert user with default password.
				//
				// 
				// TODO For the first round of development, new_account is set to false. Once we implement password 
				// creation from the phone, new_account must be set to true.
				
					
				KeyHolder userIdKeyHolder = new GeneratedKeyHolder(); 
				jdbcTemplate.update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(
								"insert into user (login_id, enabled, password, new_account) values(?,?,?,?)", new String[] {"id"}
							);
							ps.setString(1, props.getProperty("userName"));
							ps.setBoolean(2, true);
							ps.setString(3, BCrypt.hashpw(props.getProperty("password"), salt));
							ps.setBoolean(4, false);
							return ps;
						}
					},
					userIdKeyHolder
				);
				
				_logger.info("successful insert into user, returned id: " + userIdKeyHolder.getKey());
				
				//
				// 2. Insert user_personal
				//
				
				KeyHolder userPersonalIdKeyHolder = new GeneratedKeyHolder();
				
				jdbcTemplate.update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(
								"insert into user_personal (email_address, json_data) values (?,?)", new String[] {"id"}
						    );
							ps.setString(1, props.getProperty("emailAddress"));
							ps.setString(2, props.getProperty("json"));
							return ps;
						}
					},
					userPersonalIdKeyHolder
				);
				
				_logger.info("successful insert into user_personal, returned id: " + userPersonalIdKeyHolder.getKey());
				
				//
				// 3. Insert user_user_personal
				//
				
				// these vars are final in order to allow them to be used in the anonymous inner class
				// it's weird, but the compiler won't allow it any other way
				final int userId = userIdKeyHolder.getKey().intValue();
				final int userPersonalId = userPersonalIdKeyHolder.getKey().intValue();
				
				jdbcTemplate.update(
				    new PreparedStatementCreator() {
				    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				    		PreparedStatement ps = connection.prepareStatement(
				    			"insert into user_user_personal (user_id, user_personal_id) values (?,?)"
				    		);
				    		ps.setInt(1, userId);
				    		ps.setInt(2, userPersonalId);
				    		return ps;
				    	}
				    }
				);
				
				_logger.info("successful insert into user_user_personal");
				
				//
				// 4. Find user_role.id for the role type of "participant"
				//
				
				final int roleId = jdbcTemplate.queryForInt("select id from user_role where label = \"participant\"");
				
				_logger.info("in user_role found role_id: " + roleId + " for role \"participant\"");
				
				//
				// 5. Insert user_role_campaign
				//
				
				jdbcTemplate.update(
				    new PreparedStatementCreator() {
				    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				    		PreparedStatement ps = connection.prepareStatement(
				    			"insert into user_role_campaign (user_id, campaign_id, user_role_id) values (?,?,?)"
				    		);
				    		ps.setInt(1, userId);
				    		ps.setInt(2, campaignId);
				    		ps.setInt(3, roleId);
				    		return ps;
				    	}
				    }
			    );
				
				_logger.info("successful insert into user_role_campaign");
			
			} catch (DataAccessException dae) {
				
				_logger.error("Rolling back transaction!", dae);
				transactionManager.rollback(status);
				throw dae;
				
			}
			
			transactionManager.commit(status); // end transaction
			_logger.info("transaction committed");
		}
		
		
		finally { // clean up
			try {
				
				if(dataSource != null) {
					dataSource.close();
					dataSource = null;
				}
			} 
			catch(SQLException sqle) {
				// not much that can be done so just log the error
				_logger.error(sqle);
				
			}
		}
	}
	
	/**
	 * Removes a user from the database using the provided Properties.
	 * 
	 * @throws IncorrectResultSizeDataAccessException if a campaign cannot be found for the subdomain property  
	 * @throws IncorrectResultSizeDataAccessException if a user cannot be found for the userName property
	 * @throws IncorrectResultSizeDataAccessException if none or more than one row exists in user_personal for the user   
	 * @throws IncorrectResultSizeDataAccessException if no rows exist in user_role_campaign for the user
	 * @throws IncorrectResultSizeDataAccessException if no rows exist in user_user_personal for the user
	 * @throws DataAccessException if there are any problems accessing the db
	 */
	private static void removeUser(final Properties props) {
		_logger.info("Removing user with the following properties: " + props);
		int totalNumberOfRowsRemoved = 0;
		
		BasicDataSource dataSource = getDataSource(props);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		//
		// Check incoming data to make sure that the removal can be performed
		//

		findCampaignForSubdomain(jdbcTemplate, props); // throws an IncorrectResultSizeDataAccessException if the campaign cannot be
		                                               // found 
		
		int userId = jdbcTemplate.queryForInt( // throws an IncorrectResultSizeDataAccessException if the user cannot be found
			"select id from user where login_id = \"" + props.getProperty("userName") + "\""
		); 
		_logger.info("found user.id " + userId + " for user " + props.getProperty("userName"));
		
		// throws an IncorrectResultSizeDataAccessException if a row cannot be found or more than one row is found.
		// Our DB schema allows for multiple rows per user in user_personal (due to IRB standards the data must be kept separate
		// from login credentials), but the business rules disallow it e.g., why would a user have two sets of first name and last 
		// name? 
		int userPersonalId = jdbcTemplate.queryForInt("select id from user_personal where id = (select user_personal_id from " +
			"user_user_personal where user_id = " + userId + ")");
		_logger.info("found user_personal.id " + userPersonalId + " for user " + props.getProperty("userName"));
		
		//
		// Perform the removal within a transaction
		//

		DefaultTransactionDefinition def = new DefaultTransactionDefinition();
		def.setName("Transaction wrapping removal of a user from user, user_personal, user_user_personal, and user_role_campaign");
		
		PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
		TransactionStatus status = transactionManager.getTransaction(def); // begin transaction
		
		try {
			//
			// Delete from user_role_campaign (for all campaigns a user may belong to)
			//
			int numberOfRows = jdbcTemplate.update("delete from user_role_campaign where user_id = " + userId );
			
			if(0 == numberOfRows) {
				// violates business rule, there must be at least one row here
				throw new IncorrectResultSizeDataAccessException("no rows found to delete in user_role_campaign for user id: " + userId, 1, 0);
			}
		    _logger.info("deleted " + numberOfRows + " row from user_role_campaign for user " + props.getProperty("userName"));
		    totalNumberOfRowsRemoved += numberOfRows;
		    
		    //
			// Delete from user_user_personal.
			//			
		    numberOfRows = jdbcTemplate.update("delete from user_user_personal where user_id = " + userId);
		    if(0 == numberOfRows) {
		    	// violates business rule, there can be only one row here
		    	throw new IncorrectResultSizeDataAccessException("no rows found to delete in user_user_personal for user id: " + userId, 1, 0);
			}
		    _logger.info("deleted " + numberOfRows + " row from user_user_personal for user " + props.getProperty("userName"));
		    totalNumberOfRowsRemoved += numberOfRows;
		    
		    //
			// Delete from user_personal. From the check of the user_personal table above, there is only one row here.
			//			
		    numberOfRows = jdbcTemplate.update("delete from user_personal where id = "  + userPersonalId);
		    _logger.info("deleted " + numberOfRows + " row from user_personal for user " + props.getProperty("userName"));
		    totalNumberOfRowsRemoved += numberOfRows;
		    
		    //
			// Delete from user. From the check of the user table above, there is only one row here. 
			//
		    numberOfRows = jdbcTemplate.update("delete from user where id = " + userId);
		    _logger.info("deleted " + numberOfRows + " row from user for user " + props.getProperty("userName"));
		    totalNumberOfRowsRemoved += numberOfRows;
		    
		    _logger.info(totalNumberOfRowsRemoved + " deleted rows for user: " + props.getProperty("userName"));
		    transactionManager.commit(status); // end transaction
 		    
		} catch (DataAccessException dae) { // note: also catches any IncorrectResultSizeDataAccessExceptions from the try block
			
			_logger.error("Rolling back transaction!", dae);
			transactionManager.rollback(status);
			throw dae;
			
		}
		finally { // clean up
			try {
				
				if(dataSource != null) {
					dataSource.close();
					dataSource = null;
				}
			} 
			catch(SQLException sqle) {
				// not much that can be done so just log the error
				_logger.error(sqle);
				
			}
		}   		
	}
	
	/**
	 * @throws IncorrectResultSizeDataAccessException if no campaign could be found for the subdomain 
	 */
	private static int findCampaignForSubdomain(JdbcTemplate jdbcTemplate, Properties props) {
		int campaignId = jdbcTemplate.queryForInt(
			"select id from campaign where subdomain = \"" + props.getProperty("subdomain") + "\""
		);
		_logger.info("found campaign id " + campaignId + " for subdomain " + props.getProperty("subdomain"));
		return campaignId;
	}
	
	/**
	 * Returns a BasicDataSource configured using the provided Properties. 
	 */
	private static BasicDataSource getDataSource(Properties props) {

		BasicDataSource dataSource = new BasicDataSource();
		
		dataSource.setDriverClassName(props.getProperty("dbDriver"));
		dataSource.setUrl(props.getProperty("dbJdbcUrl"));
		dataSource.setUsername(props.getProperty("dbUserName"));
		dataSource.setPassword(props.getProperty("dbPassword"));
		dataSource.setInitialSize(3);
		// commits are performed manually
		dataSource.setDefaultAutoCommit(false);
		// use the default MySQL isolation level: REPEATABLE READ
		
		return dataSource;
	}
	
	/**
	 * Validates incoming property values. 
	 */
	private static void checkProperty(Properties props, String propName) {
		if((! props.containsKey(propName)) || isEmptyOrWhitespaceOnly(props.getProperty(propName))) {
			throw new IllegalArgumentException("Missing required " + propName + " property in input file.");
		}
	}
	
	/**
	 * Validates whether Strings are empty or null. 
	 */
	private static boolean isEmptyOrWhitespaceOnly(String string) {
		return null == string || "".equals(string.trim());
	}
	
	private static String _helpText = "\nThis is a program for adding or removing users from the AndWellness database.\n\n" +
	                                  "Please use edu.ucla.cens.awuser.CreateUserName to create user names.\n\n" +
	                                  "****\n Be careful when removing users. The idea behind remove is that it should\n" +
	                                  " _only_ serve as an undo feature when a new user is accidentally added by this\n" +
	                                  "same program. " +
	                                  "\n The remove feature removes the user entirely even if the user belongs to\n" +
	                                  "multiple campaigns. (i.e., if the user is represented by multiple rows in\n" +
	                                  "user_role_campaign, they are all deleted.) However, the remove feature is also\n" +
	                                  "strict in that it will flag logical data violations and exit. An AW user should\n" +
	                                  "not have more than one row in each of the following tables: user, user_personal,\n" +
	                                  "user_user_personal, and user_role_campaign. Mulitple rows in user_role_campaign are\n" +
	                                  "tolerated for internal CENS use only, although in the future it's conceivable that\n" +
	                                  "we will have live users who belong to mulitple campaigns." +
	                                  "\n****\n\n" + 
			                          "Usage:\n" +
			                          "   java -classpath LIB_DIR:CLASS_DIR edu.ucla.cens.awuser.ManageUser [help] [quiet] <add>|<remove> file salt\n" +
			                          "E.g. java -classpath lib/*:classes edu.ucla.cens.awuser.ManageUser add data/add-remove-user.properties saltstring\n\n" + 
			                          "The file must contain data in java.util.Properties format i.e., newline\n" +
			                          "separated key=value pairs. Please see data/add-remove-user.properties for an\n" +
			                          "example. All values defined in the template file are required.\n\n" +
			                          "The following jars must be in the classpath: spring-2.5.6-SEC01.jar,\n" +
			                          "mysql-connector-java-5.1.10-bin.jar, json-dot-org-2010-01-05.jar,\n" +
			                          "commons-dbcp-1.2.2.jar, commons-pool-1.5.4.jar, commons-logging-1.1.1.jar,\n" +
			                          "log4j-1.2.15.jar, jbcrypt-0.3.jar.";
}
