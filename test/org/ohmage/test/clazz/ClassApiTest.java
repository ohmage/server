package org.ohmage.test.clazz;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.lib.OhmageApi;
import org.ohmage.lib.exception.ApiException;
import org.ohmage.lib.exception.RequestErrorException;
import org.ohmage.test.Controller;

/**
 * This class is responsible for testing the class API.
 * 
 * @author John Jenkins
 */
public class ClassApiTest {
	private final OhmageApi api;
	
	private String classId;
	
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