package org.ohmage.validator;

import junit.framework.Test;
import junit.framework.TestSuite;

import org.apache.log4j.BasicConfigurator;
import org.ohmage.test.ParameterSets;

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
		ParameterSets.init();
		
		TestSuite suite = new TestSuite(ValidatorTests.class.getName());
		
		suite.addTestSuite(AuditValidatorsTest.class);
		suite.addTestSuite(CampaignClassValidatorsTest.class);
		suite.addTestSuite(CampaignDocumentValidatorsTest.class);
		suite.addTestSuite(CampaignValidatorsTest.class);
		suite.addTestSuite(ClassDocumentValidatorsTest.class);
		suite.addTestSuite(ClassValidatorsTest.class);
		suite.addTestSuite(DateValidatorsTest.class);
		suite.addTestSuite(DocumentValidatorsTest.class);
		suite.addTestSuite(ImageValidatorsTest.class);
		suite.addTestSuite(MobilityValidatorsTest.class);
		suite.addTestSuite(SurveyResponseValidatorsTest.class);
		suite.addTestSuite(UserCampaignValidatorsTest.class);
		suite.addTestSuite(UserClassValidatorsTest.class);
		suite.addTestSuite(UserDocumentValidatorsTest.class);
		suite.addTestSuite(UserValidatorsTest.class);
		suite.addTestSuite(VisualizationValidatorsTest.class);
		
		return suite;
	}
}