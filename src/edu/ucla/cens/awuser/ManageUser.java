package edu.ucla.cens.awuser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.dao.DataAccessException;
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
		
	/**
	 *  Adds or removes a user from the AW database depending on the command line options provided. Run with "help" as the first
	 *  argument for an explanation and an example.
	 *  
	 *  @throws IllegalArgumentException if any required values are missing from the command line or the input file
	 *  @throws IllegalArgumentException if the userName is malformed (too short or contains illegal characters)
	 *  @throws IOException if the input file does not exist or if there are any other IO problems
	 *  @throws JSONException if the json property in the input file contains malformed JSON
	 *  @throws DataAccessException if no campaign exists for the subdomain found in the properties file
	 */
	public static void main(String args[]) throws IOException, JSONException {
		// Configure log4j for Spring. (pointing to System.out)
		BasicConfigurator.configure();
		
		// TODO - need a verbose switch to turn this on and off
		// Logger.getLogger("rootLogger").setLevel(Level.INFO);
		
		if(args.length < 2 || (args.length == 1 && args[0].contains("help"))) {
			System.out.println(helpText);
			System.exit(0);
		}
		
		if(! ("add".equals(args[0]) || "remove".equals(args[0]))) {
			throw new IllegalArgumentException("add or remove is required as the first argument. You provided " + args[0]);
		}
		
		BufferedReader in = new BufferedReader(new FileReader(args[1])); 
		Properties props = new Properties();
		props.load(in);
		in.close();

		// user supplied props
		String function = args[0];
		
		checkProperty(props, "userName");
		checkProperty(props, "subdomain");
		checkProperty(props, "emailAddress");
		checkProperty(props, "json");
		checkProperty(props, "dbUserName");
		checkProperty(props, "dbPassword");
		checkProperty(props, "dbDriver");
		checkProperty(props, "dbJdbcUrl");
		
		// perform detailed check on userName
		String userName = props.getProperty("userName");
		
		if(userName.length() < 9) {
			throw new IllegalArgumentException("userName must be longer than eight characters");
		}
		
		String validChars = "abcdefghijklmnopqrstuvwxyz.";
		
		for(int i = 0; i < userName.length(); i++) {
			CharSequence subSequence = userName.subSequence(i, i + 1);
			
			if(! validChars.contains(subSequence)) {
				throw new IllegalArgumentException("userName contains an illegal character: " + subSequence);
			}
		}
		
		JSONObject json = new JSONObject(props.getProperty("json"));
		// these calls are analogous to checkProperty() above except JSONObject will throw a JSONException if the key 
		// does not exist
		json.getString("firstName");
		json.getString("lastName");
		
		// ok, now some actual work can be done
		if("add".equals(function)) {
			
			addUser(props);
			
		} else { 
			
			removeUser(props);			
		}
		
		System.out.println("Done");
	}
	
	
	private static void addUser(Properties props) {
		System.out.println("Adding user with the following properties ... ");
		System.out.println(props);
		
		BasicDataSource dataSource = getDataSource(props);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		try { 
			
			// first, make sure the subdomain exists for a campaign
			// a DataAccessException will be thrown if no rows are returned
			final int campaignId = jdbcTemplate.queryForInt(
				"select id from campaign where subdomain = ?",  
                new Object[]{props.getProperty("subdomain")}
		    );
		
			System.out.println("found campaign id " + campaignId + " for subdomain " + props.getProperty("subdomain"));

			// Now execute the rest of the insertions using a transaction
			
			// Now execute the rest of the queries inside a transaction so they can be committed or 
			// rolled back together
			
			DefaultTransactionDefinition def = new DefaultTransactionDefinition();
			def.setName("User Insert"); // TODO actually define the isolation level, etc here
			
			PlatformTransactionManager transactionManager = new DataSourceTransactionManager(dataSource);
			TransactionStatus status = transactionManager.getTransaction(def); // this effectively begins a transaction
			
			try {
				// insert user
				
				// these vars are final in order to allow them to be used in the anonymous inner class
				// it's weird, but the compiler won't allow it any other way
				// TODO make the incoming props final
				final String insertUserSql = "insert into user (login_id) values(?)";
				final String userName = props.getProperty("userName");
					
				KeyHolder userIdKeyHolder = new GeneratedKeyHolder(); 
				jdbcTemplate.update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(insertUserSql, new String[] {"id"});
							ps.setString(1, userName);
							return ps;
						}
					},
					userIdKeyHolder
				);
				
				System.out.println("successful insert into user table, returned id: " + userIdKeyHolder.getKey());
				
				// insert user_personal
				
				final String insertUserPersonalSql = "insert into user_personal (email_address, json_data) values (?,?)";
				final String emailAddress = props.getProperty("emailAddress");
				final String json = props.getProperty("json");
				
				KeyHolder userPersonalIdKeyHolder = new GeneratedKeyHolder();
				
				jdbcTemplate.update(
					new PreparedStatementCreator() {
						public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
							PreparedStatement ps = connection.prepareStatement(insertUserPersonalSql, new String[] {"id"});
							ps.setString(1, emailAddress);
							ps.setString(2, json);
							return ps;
						}
					},
					userPersonalIdKeyHolder
				);
				
				System.out.println("successful insert into user table, returned id: " + userPersonalIdKeyHolder.getKey());
				
				// insert user_user_personal
				
				final String insertUserUserPersonalSql = "insert into user_user_personal (user_id, user_personal_id) values (?,?)";
				final int userId = userIdKeyHolder.getKey().intValue();
				final int userPersonalId = userPersonalIdKeyHolder.getKey().intValue();
				
				jdbcTemplate.update(
				    new PreparedStatementCreator() {
				    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				    		PreparedStatement ps = connection.prepareStatement(insertUserUserPersonalSql);
				    		ps.setInt(1, userId);
				    		ps.setInt(2, userPersonalId);
				    		return ps;
				    	}
				    }
				);
				
				System.out.println("successful insert into user_user_personal");
				
				final int roleId = jdbcTemplate.queryForInt("select id from user_role where label = \"participant\"");
				
				System.out.println("in user_role found role_id: " + roleId + " for role \"participant\"");
				
				final String insertUserRoleCampaignSql = "insert into user_role_campaign (user_id, campaign_id, user_role_id) values (?,?,?)";
				
				jdbcTemplate.update(
				    new PreparedStatementCreator() {
				    	public PreparedStatement createPreparedStatement(Connection connection) throws SQLException {
				    		PreparedStatement ps = connection.prepareStatement(insertUserRoleCampaignSql);
				    		ps.setInt(1, userId);
				    		ps.setInt(2, campaignId);
				    		ps.setInt(3, roleId);
				    		return ps;
				    	}
				    }
			    );
				
				System.out.println("successful insert into user_role_campaign table");
			
			} catch (DataAccessException dae) {
			
				transactionManager.rollback(status);
				throw dae;
				
			}
			
			transactionManager.commit(status); // this effectively ends the transaction
			System.out.println("transaction committed");
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
				sqle.printStackTrace();
				
			}
		}
	}
	
	private static void removeUser(Properties props) {
		
	}
		
	private static BasicDataSource getDataSource(Properties props) {

		BasicDataSource dataSource = new BasicDataSource();
		
		dataSource.setDriverClassName(props.getProperty("dbDriver"));
		dataSource.setUrl(props.getProperty("dbJdbcUrl"));
		dataSource.setUsername(props.getProperty("dbUserName"));
		dataSource.setPassword(props.getProperty("dbPassword"));
		dataSource.setInitialSize(3);
		dataSource.setDefaultAutoCommit(false);
		// use the default MySQL isolation level: REPEATABLE READ
		// dataSource.setDefaultTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
		
		return dataSource;
	}
	
	private static void checkProperty(Properties props, String propName) {
		if((! props.containsKey(propName)) || isEmptyOrWhitespaceOnly(props.getProperty(propName))) {
			throw new IllegalArgumentException("Missing required " + propName + " property in input file.");
		}
	}
	
	private static boolean isEmptyOrWhitespaceOnly(String string) {
		return null == string || "".equals(string.trim());
	}
	
	private static String helpText = "This is a program for adding or removing users from the AndWellness database.\n\n" +
	                                 "Please use edu.ucla.cens.awuser.CreateUserName to create user names.\n\n" +
	                                 "**** Be careful when removing users. The idea behind remove is that it should _only_ serve " +
	                                 "as an undo feature when a new user is accidentally added by this same program. ****\n\n" + 
			                         "Usage:\n" +
			                         "   java edu.ucla.cens.awuser.ManageUser [add|remove] file\n\n" +
			                         "The file must contain data in java.util.Properties format i.e., newline separated " +
			                         "key=value pairs. Please see awuser/adduser-template.properties for an example. All values " +
			                         "defined in the template file are required.\n\n" +
			                         "The following jars must be in the classpath: spring-2.5.6-SEC01.jar, " +
			                         "mysql-connector-java-5.1.10-bin.jar, json-dot-org-2010-01-05.jar, commons-dbcp-1.2.2.jar, " +
			                         "commons-pool-1.5.4.jar, commons-logging-1.1.1.jar, log4j-1.2.15.jar.";
}
