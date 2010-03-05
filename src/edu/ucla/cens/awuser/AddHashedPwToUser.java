package edu.ucla.cens.awuser;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Properties;
import java.util.regex.Pattern;

import jbcrypt.BCrypt;

import org.apache.commons.dbcp.BasicDataSource;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * Adds or updates a password on an AW user.
 * 
 * @author selsky
 */
public class AddHashedPwToUser {
	private static Logger _logger = Logger.getLogger(AddHashedPwToUser.class);
	
	/**
	 * Validates incoming parameters and dispatches to run() to perform the db update. 
	 */
	public static void main(String args[]) throws IOException {
		// Configure log4j. (pointing to System.out)
		// All Spring messages are at the DEBUG level. They are extremely informative (and verbose).
		BasicConfigurator.configure();
		
		if(args.length > 0  && args[0].equals("help")) {
			_logger.info(_helpText);
			System.exit(0);
		}
		
		if(args.length != 2) {
			_logger.info("Incorrect number of arguments");
			_logger.info(_helpText);
			System.exit(0);
		}
		
		String fileName = args[0];
		String salt = args[1];
		
		BufferedReader in = new BufferedReader(new FileReader(fileName)); 
		Properties props = new Properties();
		props.load(in);
		in.close();

		checkProperty(props, "userName");
		checkProperty(props, "password");
		checkProperty(props, "dbUserName");
		checkProperty(props, "dbPassword");
		checkProperty(props, "dbDriver");
		checkProperty(props, "dbJdbcUrl");
		
		// perform detailed check on userName and password
		String userName = props.getProperty("userName");
		String password = props.getProperty("password");
		
		Pattern pattern = Pattern.compile("[a-z\\.]{9,15}");
		
		if(! pattern.matcher(userName).matches()) {
			throw new IllegalArgumentException("user names can only be between 9 and 15 characters in length and can only contain" +
			" letters and the dot character");
		}
		
		if(! pattern.matcher(password).matches()) {
			throw new IllegalArgumentException("passwords can only be between 9 and 15 characters in length and can only contain" +
			" letters and the dot character");
		}

		run(props, salt);
		
		_logger.info("completed successfully");
	}
	
	/**
	 * Performs an update on the user table. 
	 */
	private static void run(Properties props, String salt) {
		_logger.info("Adding a password to a user with the following properties: " + props);
		
		String hashedPw = BCrypt.hashpw(props.getProperty("password"), salt); // if the salt is not in a format that BCrypt 
		                                                                      // understands (i.e., if the salt was not created
		                                                                      // using BCrypt), it will complain loudly and exit
		                                                                      // with a RuntimeException
		
		String updateSql = "UPDATE user" +
				           " SET password = '" + hashedPw + "', " +
				           " new_account = b'0'" +   // setting new_account to false for now. it will need to be set to true for 
				                                     // creation of passwords from phone
				           
		                   " WHERE login_id = '" + props.getProperty("userName") + "'";
		
		BasicDataSource dataSource = getDataSource(props);
		JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);
		
		try {
			
			int numberOfRowsAffected = jdbcTemplate.update(updateSql);
			
			if(numberOfRowsAffected < 1) {
				_logger.info("no user found");
			}
			
			// The condition of greater than one row updated is not checked because login_ids are unique in the user table
			
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
	 * Returns a BasicDataSource configured using the provided Properties. 
	 */
	private static BasicDataSource getDataSource(Properties props) {

		BasicDataSource dataSource = new BasicDataSource();
		
		dataSource.setDriverClassName(props.getProperty("dbDriver"));
		dataSource.setUrl(props.getProperty("dbJdbcUrl"));
		dataSource.setUsername(props.getProperty("dbUserName"));
		dataSource.setPassword(props.getProperty("dbPassword"));
		dataSource.setInitialSize(3);
		dataSource.setDefaultAutoCommit(true);
		
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
	
	
	private static String _helpText = "\nUsage: java -classpath LIB_DIR:CLASS_DIR edu.ucla.cens.awuser.AddHashedPwToUser [help] fileName salt\n\n" +
			                          "The following jars are required to be in the classpath: spring-2.5.6-SEC01.jar,\n" +
			                          "mysql-connector-java-5.1.10-bin.jar, json-dot-org-2010-01-05.jar,\n" +
			                          "commons-dbcp-1.2.2.jar, commons-pool-1.5.4.jar, commons-logging-1.1.1.jar,\n" +
			                          "log4j-1.2.15.jar, jbcrypt-0.3.jar.\n" +
			                          "The file represented by fileName must be a java Properties file with the following keys: \n" +
			                          "userName, password, dbUserName, dbPassword, dbDriver, dbJdbcUrl";
	
}
