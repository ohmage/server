package org.ohmage.service;

import java.util.Collection;

import org.ohmage.annotator.ErrorCodes;
import org.ohmage.cache.DocumentRoleCache;
import org.ohmage.dao.ClassDocumentDaos;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.Request;

/**
 * This class is responsible for all operations that pertain to class-document
 * relationships.
 * 
 * @author John Jenkins
 */
public class ClassDocumentServices {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ClassDocumentServices() {}
	
	/**
	 * Returns the document role for a given document with a given class.
	 * 
	 * @param request The Request performing this service.
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @return The class' document role or null if it is not associated 
	 * 		   with the document.
	 * 
	 * @throws ServiceException Thrown if there is an error.
	 */
	public static DocumentRoleCache.Role getDocumentRoleForClass(Request request, String classId, String documentId) throws ServiceException {
		try {
			return ClassDocumentDaos.getClassDocumentRole(classId, documentId);
		}
		catch(DataAccessException e) {
			request.setFailed();
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and a class based on the class' role with the document.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param role The maximum role of the user that is attempting to 
	 * 			   disassociate the class and document. 
	 * 
	 * @param classId The class' unique identifier.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the role is not high enough to
	 * 							disassociate a class and document.
	 */
	public static void ensureRoleHighEnoughToDisassociateDocumentFromClass(Request request, DocumentRoleCache.Role role, String classId, String documentId) throws ServiceException {
		DocumentRoleCache.Role classDocumentRole = getDocumentRoleForClass(request, classId, documentId);
		
		if(role.compare(classDocumentRole) < 0) {
			request.setFailed(ErrorCodes.DOCUMENT_INSUFFICIENT_PERMISSIONS, "Insufficient permissions to disassociate the document '" + documentId + "' with the class '" + classId + "' as the class has a higher role.");
			throw new ServiceException("Insufficient permissions to disassociate the document '" + documentId + "' with the class '" + classId + "' as the class has a higher role.");
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and each of the classes in a collection based on the 
	 * classes' individual roles with the document.
	 * 
	 * @param request The Request that is performing this service.
	 * 
	 * @param role The maximum role of the user that is attempting to 
	 * 			   disassociate the classes and document. 
	 * 
	 * @param classIds The classes' unique identifiers.
	 * 
	 * @param documentId The document's unique identifier.
	 * 
	 * @throws ServiceException Thrown if the role is not high enough to
	 * 							disassociate the class and document.
	 */
	public static void ensureRoleHighEnoughToDisassociateDocumentFromClasses(Request request, DocumentRoleCache.Role role, Collection<String> classIds, String documentId) throws ServiceException {
		for(String classId : classIds) {
			ensureRoleHighEnoughToDisassociateDocumentFromClass(request, role, classId, documentId);
		}
	}
}