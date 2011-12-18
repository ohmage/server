package org.ohmage.test.clazz;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Clazz;
import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;
import org.ohmage.test.Controller;
import org.ohmage.test.ParameterSets;

/**
 * This class is responsible for testing the class API.
 * 
 * @author John Jenkins
 */
public class ClassApiTest {
	private final OhmageApi api;
	
	// This keeps track of the ID of the class we are currently testing.
	private String classId;
	private String className;
	private String classDescription;
	
	/**
	 * Creates a tester for the configuration API.
	 * 
	 * @param api The ohamge API object that should already be setup with a
	 * 			  connection to a server.
	 * 
	 * @throws ApiException There was an unexpected error.
	 * 
	 * @throws IllegalStateException One of the tests failed.
	 */
	public ClassApiTest(final OhmageApi api, final String adminAuthToken) 
			throws ApiException {
		
		this.api = api;
		
		classId = null;
		
		testClassCreation(adminAuthToken);
		testClassDeletion(adminAuthToken);
	}
	
	/**
	 * Tests all parameters that pertain to classes.
	 * 
	 * @param adminAuthToken An authentication token for an administrator.
	 * 
	 * @throws ApiException Thrown if there is a library error which should
	 * 						never happen.
	 */
	public void test(final String adminAuthToken) throws ApiException {
		try {
			classId = "urn:class:testing:id";
			className = "Test Name";
			classDescription = null;
			
			api.createClass(adminAuthToken, Controller.CLIENT, classId, 
					className, classDescription);
			
			testClassReadAndUpdate(adminAuthToken);
		}
		finally {
			api.deleteClass(adminAuthToken, Controller.CLIENT, classId);
		}
	}
	
	/**
	 * Test class creation then creates a class whose ID is stored in the 
	 * class-level variable.
	 * 
	 * @param adminAuthToken An administrator's authentication token.
	 * 
	 * @throws ApiException There was an unexpected error.
	 */
	private void testClassCreation(final String adminAuthToken) 
			throws ApiException {
		
		// No token.
		try {
			api.createClass(null, Controller.CLIENT, "urn:test:class:id", "_test_class_name_", null);
			throw new IllegalStateException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No authentication token: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Invalid token.
		try {
			api.createClass("invalid token", Controller.CLIENT, "urn:test:class:id", "_test_class_name_", null);
			throw new IllegalStateException("Failed: Invalid authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid authentication token: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// No client.
		try {
			api.createClass(adminAuthToken, null, "urn:test:class:id", "_test_class_name_", null);
			throw new IllegalStateException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No client: " + ErrorCode.SERVER_INVALID_CLIENT, e);
			}
		}
		
		// No class ID.
		try {
			api.createClass(adminAuthToken, Controller.CLIENT, null, "_test_class_name_", null);
			throw new IllegalStateException("Failed: No class ID.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No class ID: " + ErrorCode.CLASS_INVALID_ID, e);
			}
		}
		
		// Invalid class ID.
		try {
			api.createClass(adminAuthToken, Controller.CLIENT, "invalid ID", "_test_class_name_", null);
			throw new IllegalStateException("Failed: Invalid class ID.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid class ID: " + ErrorCode.CLASS_INVALID_ID, e);
			}
		}
		
		// No class name.
		try {
			api.createClass(adminAuthToken, Controller.CLIENT, "urn:test:class:id", null, null);
			throw new IllegalStateException("Failed: No class name.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_NAME.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No class name: " + ErrorCode.CLASS_INVALID_NAME, e);
			}
		}
		
		try {
			api.createClass(adminAuthToken, Controller.CLIENT, "urn:test:class:id", "_test_class_name_", null);
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Class creation.", e);
		}
		
		classId = "urn:test:class:id";
	}
	
	/**
	 * Tests class update and read.
	 * 
	 * @param adminAuthToken An authentication token for an administrator.
	 */
	private void testClassReadAndUpdate(final String adminAuthToken) 
			throws ApiException {

		Collection<String> classIds = new LinkedList<String>();
		classIds.add(classId);
		
		try {
			api.getClasses(null, Controller.CLIENT, classIds);
			throw new IllegalArgumentException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No authentication token.", e);
			}
		}
		
		try {
			api.getClasses("invalid token", Controller.CLIENT, classIds);
			throw new IllegalArgumentException("Failed: Invalid token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid token.", e);
			}
		}
		
		try {
			api.getClasses(adminAuthToken, null, classIds);
			throw new IllegalArgumentException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No client.", e);
			}
		}
		
		try {
			api.getClasses(adminAuthToken, Controller.CLIENT, null);
			throw new IllegalArgumentException("Failed: No class ID.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No class ID.", e);
			}
		}
		
		for(String invalidUrn : ParameterSets.getInvalidUrns()) {
			try {
				api.getClasses(adminAuthToken, Controller.CLIENT, null);
				throw new IllegalArgumentException("Failed: Invalid class ID: " + invalidUrn);
			}
			catch(RequestErrorException e) {
				if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
					throw new IllegalArgumentException("Failed: Invalid class ID: " + invalidUrn, e);
				}
			}
		}
		
		// Now, attempt a legitimate query and check the results.
		Map<String, Clazz> classInformationMap;
		try {
			classInformationMap = 
					api.getClasses(adminAuthToken, Controller.CLIENT, classIds);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Class read.", e);
		}
		
		if(classInformationMap.size() > 1) {
			throw new IllegalArgumentException("Information was returned about other classes: " + classInformationMap.keySet());
		}
		
		if(! classInformationMap.containsKey(classId)) {
			throw new IllegalArgumentException("Information about the class was not returned.");
		}
		
		Clazz classInformation = classInformationMap.get(classId);

		if(! classId.equals(classInformation.getId())) {
			throw new IllegalArgumentException("The class ID doesn't match (" + classId + "): " + classInformation.getId());
		}

		if(! className.equals(classInformation.getName())) {
			throw new IllegalArgumentException("The class name doesn't match(" + className + "): " + classInformation.getName());
		}

		if(! (classDescription == null) && 
				(classInformation.getDescription() == null)) {
			throw new IllegalArgumentException("The class description doesn't match(" + classDescription + "):" + classInformation.getId());
		}
		
		// So far, so good. Now, start with an update that changes nothing.
		try {
			api.updateClass(null, Controller.CLIENT, classId, null, null, null, null);
			throw new IllegalArgumentException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No authentication token.", e);
			}
		}
		
		try {
			api.updateClass("invalid token", Controller.CLIENT, classId, null, null, null, null);
			throw new IllegalArgumentException("Failed: Invalid token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: Invalid token.", e);
			}
		}
		
		try {
			api.updateClass(adminAuthToken, null, classId, null, null, null, null);
			throw new IllegalArgumentException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No client.", e);
			}
		}
		
		try {
			api.updateClass(adminAuthToken, Controller.CLIENT, null, null, null, null, null);
			throw new IllegalArgumentException("Failed: No class ID.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
				throw new IllegalArgumentException("Failed: No class ID.", e);
			}
		}
		
		for(String invalidUrn : ParameterSets.getInvalidUrns()) {
			try {
				api.updateClass(adminAuthToken, Controller.CLIENT, invalidUrn, null, null, null, null);
				throw new IllegalArgumentException("Failed: Invalid class ID: " + invalidUrn);
			}
			catch(RequestErrorException e) {
				if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
					throw new IllegalArgumentException("Failed: Invalid class ID: " + invalidUrn, e);
				}
			}
		}
		
		try {
			api.updateClass(adminAuthToken, Controller.CLIENT, classId, null, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Valid class update.", e);
		}
		
		// Now, check that nothing changed.
		try {
			classInformationMap = 
					api.getClasses(adminAuthToken, Controller.CLIENT, classIds);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Class read.", e);
		}
		
		if(classInformationMap.size() > 1) {
			throw new IllegalArgumentException("Information was returned about other classes: " + classInformationMap.keySet());
		}
		
		if(! classInformationMap.containsKey(classId)) {
			throw new IllegalArgumentException("Information about the class was not returned.");
		}
		
		classInformation = classInformationMap.get(classId);

		if(! classId.equals(classInformation.getId())) {
			throw new IllegalArgumentException("The class ID doesn't match (" + classId + "): " + classInformation.getId());
		}

		if(! className.equals(classInformation.getName())) {
			throw new IllegalArgumentException("The class name doesn't match(" + className + "): " + classInformation.getName());
		}

		if(! (classDescription == null) && 
				(classInformation.getDescription() == null)) {
			throw new IllegalArgumentException("The class description doesn't match(" + classDescription + "):" + classInformation.getId());
		}
		
		// Alright. Now, begin changing the information one at a time and then
		// checking the results.
		// Begin with the name.
		try {
			className = "New Name";
			api.updateClass(adminAuthToken, Controller.CLIENT, classId, className, null, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Valid class update.", e);
		}
		
		try {
			classInformationMap = 
					api.getClasses(adminAuthToken, Controller.CLIENT, classIds);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Class read.", e);
		}
		
		if(classInformationMap.size() > 1) {
			throw new IllegalArgumentException("Information was returned about other classes: " + classInformationMap.keySet());
		}
		
		if(! classInformationMap.containsKey(classId)) {
			throw new IllegalArgumentException("Information about the class was not returned.");
		}
		
		classInformation = classInformationMap.get(classId);

		if(! classId.equals(classInformation.getId())) {
			throw new IllegalArgumentException("The class ID doesn't match (" + classId + "): " + classInformation.getId());
		}

		if(! className.equals(classInformation.getName())) {
			throw new IllegalArgumentException("The class name doesn't match(" + className + "): " + classInformation.getName());
		}

		if(! (classDescription == null) && 
				(classInformation.getDescription() == null)) {
			throw new IllegalArgumentException("The class description doesn't match(" + classDescription + "):" + classInformation.getId());
		}
		
		// Now, update the description.
		try {
			classDescription = "Now, it has a description.";
			api.updateClass(adminAuthToken, Controller.CLIENT, classId, null, classDescription, null, null);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Valid class update.", e);
		}
		
		try {
			classInformationMap = 
					api.getClasses(adminAuthToken, Controller.CLIENT, classIds);
		}
		catch(RequestErrorException e) {
			throw new IllegalArgumentException("Failed: Class read.", e);
		}
		
		if(classInformationMap.size() > 1) {
			throw new IllegalArgumentException("Information was returned about other classes: " + classInformationMap.keySet());
		}
		
		if(! classInformationMap.containsKey(classId)) {
			throw new IllegalArgumentException("Information about the class was not returned.");
		}
		
		classInformation = classInformationMap.get(classId);

		if(! classId.equals(classInformation.getId())) {
			throw new IllegalArgumentException("The class ID doesn't match (" + classId + "): " + classInformation.getId());
		}

		if(! className.equals(classInformation.getName())) {
			throw new IllegalArgumentException("The class name doesn't match(" + className + "): " + classInformation.getName());
		}

		if(! (classDescription == null) && 
				(classInformation.getDescription() == null)) {
			throw new IllegalArgumentException("The class description doesn't match(" + classDescription + "):" + classInformation.getId());
		}
		
		// FIXME: Incomplete. We still need to add the user/role add/remove
		// tests.
		Map<String, Clazz.Role> usernamesAndTheirRole = 
				new HashMap<String, Clazz.Role>();
		try {
			// Create the users to test with.
			String privilegedUsername = "test.privileged";
			api.createUser(
					adminAuthToken, 
					Controller.CLIENT, 
					privilegedUsername, 
					"Test.password1", 
					false, 
					true, 
					false, 
					false);
			usernamesAndTheirRole.put(
					privilegedUsername, 
					Clazz.Role.PRIVILEGED);
			
			String restrictedUsername = "test.restricted";
			api.createUser(
					adminAuthToken, 
					Controller.CLIENT, 
					restrictedUsername, 
					"Test.password1", 
					false, 
					true, 
					false, 
					false);
			usernamesAndTheirRole.put(
					restrictedUsername, 
					Clazz.Role.RESTRICTED);
			
			/*
			try {
				api.updateClass(adminAuthToken, Controller.CLIENT, classId, null, null, usernamesAndTheirRole, null);
			}
			catch(RequestErrorException e) {
				throw new IllegalArgumentException("Failed: Valid class update.", e);
			}
			*/
			
		}
		finally {
			// Delete the users that were using for testing.
			api.deleteUser(
					adminAuthToken, 
					Controller.CLIENT, 
					usernamesAndTheirRole.keySet());
		}
	}
	
	/**
	 * Test class deletion then deletes the class defined by the class-level
	 * class ID and nulls its value.
	 * 
	 * @param adminAuthToken An administrator's authentication token.
	 * 
	 * @throws ApiException There was an unexpected error.
	 */
	private void testClassDeletion(final String adminAuthToken) 
			throws ApiException {
		
		// No token.
		try {
			api.deleteClass(null, Controller.CLIENT, classId);
			throw new IllegalStateException("Failed: No authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No authentication token: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// Invalid token.
		try {
			api.deleteClass("invalid token", Controller.CLIENT, classId);
			throw new IllegalStateException("Failed: Invalid authentication token.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.AUTHENTICATION_FAILED.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid authentication token: " + ErrorCode.AUTHENTICATION_FAILED, e);
			}
		}
		
		// No client.
		try {
			api.deleteClass(adminAuthToken, null, classId);
			throw new IllegalStateException("Failed: No client.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.SERVER_INVALID_CLIENT.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No client: " + ErrorCode.SERVER_INVALID_CLIENT, e);
			}
		}
		
		// No class ID.
		try {
			api.deleteClass(adminAuthToken, Controller.CLIENT, null);
			throw new IllegalStateException("Failed: No class ID.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: No class ID: " + ErrorCode.CLASS_INVALID_ID, e);
			}
		}
		
		// Invalid class ID.
		try {
			api.deleteClass(adminAuthToken, Controller.CLIENT, "invalid  ID");
			throw new IllegalStateException("Failed: Invalid class ID.");
		}
		catch(RequestErrorException e) {
			if(! ErrorCode.CLASS_INVALID_ID.equals(e.getErrorCode())) {
				throw new IllegalStateException("Failed: Invalid class ID: " + ErrorCode.CLASS_INVALID_ID, e);
			}
		}
		
		try {
			api.deleteClass(adminAuthToken, Controller.CLIENT, classId);
		}
		catch(RequestErrorException e) {
			throw new IllegalStateException("Failed: Class deletion.", e);
		}
		
		classId = null;
	}
}