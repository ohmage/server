/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
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
