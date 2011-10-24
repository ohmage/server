package org.ohmage.validator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;

/**
 * This class contains the test suite that tests all of the classes in the 
 * validation package.
 * 
 * @author John Jenkins
 */
public class ValidatorTests {

	/**
	 * Sets up the system and then runs each of the tests.
	 * 
	 * @return A TestSuite that contains all of the sub-TestSuites.
	 */
	public static Test suite() {
		BasicConfigurator.configure();
		
		TestSuite suite = new TestSuite(ValidatorTests.class.getName());
		
		suite.addTestSuite(AuditValidatorsTest.class);
		suite.addTestSuite(CampaignClassValidatorsTest.class);
		suite.addTestSuite(CampaignDocumentValidatorsTest.class);
		
		return suite;
	}
}