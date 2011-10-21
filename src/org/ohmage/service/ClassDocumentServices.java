package org.ohmage.service;

import java.util.Collection;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Document;
import org.ohmage.exception.DataAccessException;
import org.ohmage.exception.ServiceException;
import org.ohmage.query.ClassDocumentQueries;

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
	public static Document.Role getDocumentRoleForClass(final String classId, 
			final String documentId) throws ServiceException {
		
		try {
			return ClassDocumentQueries.getClassDocumentRole(classId, documentId);
		}
		catch(DataAccessException e) {
			throw new ServiceException(e);
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and a class based on the class' role with the document.
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
	public static void ensureRoleHighEnoughToDisassociateDocumentFromClass(
			final Document.Role role, final String classId, 
			final String documentId) throws ServiceException {
		
		Document.Role classDocumentRole = getDocumentRoleForClass(classId, documentId);
		
		if(role.compare(classDocumentRole) < 0) {
			throw new ServiceException(
					ErrorCode.DOCUMENT_INSUFFICIENT_PERMISSIONS, 
					"Insufficient permissions to disassociate the document '" +
						documentId + 
						"' with the class '" + 
						classId + 
						"' as the class has a higher role.");
		}
	}
	
	/**
	 * Verifies that a given role has enough permissions to disassociate a
	 * document and each of the classes in a collection based on the 
	 * classes' individual roles with the document.
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
	public static void ensureRoleHighEnoughToDisassociateDocumentFromClasses(
			final Document.Role role, final Collection<String> classIds, 
			final String documentId) throws ServiceException {
		
		for(String classId : classIds) {
			ensureRoleHighEnoughToDisassociateDocumentFromClass(role, classId, documentId);
		}
	}
}