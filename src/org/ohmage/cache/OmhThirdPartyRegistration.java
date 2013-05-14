package org.ohmage.cache;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ohmage.domain.PayloadId;
import org.ohmage.exception.ValidationException;

/**
 * <p>
 * The Open mHealth registry of third-part applications for whom we are acting
 * as a shim layer.
 * </p>
 * 
 * @author John Jenkins
 */
public final class OmhThirdPartyRegistration {
	/**
	 * The registry of domain IDs to their {@link PayloadId} class that can
	 * handle them.
	 */
	private static final Map<String, Class<? extends PayloadId>> REGISTRY =
		new ConcurrentHashMap<String, Class<? extends PayloadId>>();
	
	/**
	 * Default constructor.
	 */
	private OmhThirdPartyRegistration() {
		// Do nothing.
	}

	/**
	 * Registers a new domain and its corresponding {@link PayloadId} class.
	 * 
	 * @param domain
	 *        The string representing the domain.
	 * 
	 * @param clazz
	 *        The {@link PayloadId} class that can handle this domain.
	 * 
	 * @throws IllegalArgumentException
	 *         A parameter was invalid.
	 */
	public static void registerDomain(
		final String domain,
		final Class<? extends PayloadId> clazz)
		throws IllegalArgumentException {
		
		// Null check.
		if(domain == null) {
			throw new IllegalArgumentException("The domain is null.");
		}
		else if(clazz == null) {
			throw new IllegalArgumentException("The PayloadId class is null.");
		}
		
		// Check if the class is abstract.
		if(Modifier.isAbstract(clazz.getModifiers())) {
			throw
				new IllegalArgumentException(
					"The PayloadId class is abstract.");
		}
		
		// Ensure that the class has a constructor that takes exaclty a string
		// array.
		try {
			clazz.getConstructor(String[].class);
		}
		catch(NoSuchMethodException e) {
			throw
				new IllegalArgumentException(
					"The PayloadId class doesn't have a constructor that " +
						"takes exactly an array of strings: " +
						domain,
					e);
		}
		catch(SecurityException e) {
			throw
				new IllegalArgumentException(
					"The security manager is preventing the access of the " +
						"constructor that takes exactly an array of " +
						"strings: " +
						domain,
					e);
		}
		
		// Make sure the domain hasn't already been registered.
		if(REGISTRY.containsKey(domain)) {
			throw
				new IllegalArgumentException(
					"The domain has already been registered: " + domain);
		}
		
		// Add the domain to the registry.
		REGISTRY.put(domain, clazz);
	}
	
	/**
	 * Creates a new {@link PayloadId} from the already split parts of a
	 * payload ID.
	 * 
	 * @param payloadIdParts
	 *        The parts of the payload that must contain at least two parts,
	 *        where the second part is the domain.
	 * 
	 * @return A {@link PayloadId} based on the payload ID parts.
	 * 
	 * @throws ValidationException
	 * 		   The exception thrown by the underlying constructor.
	 * 
	 * @throws IllegalArgumentException
	 *         The payload ID parts are invalid.
	 * 
	 * @throws IllegalStateException
	 *         An internal error occurred.
	 */
	public static PayloadId getPayloadId(
		final String[] payloadIdParts)
		throws
			ValidationException,
			IllegalArgumentException,
			IllegalStateException {
		
		// Validate the input.
		if(payloadIdParts == null) {
			throw
				new IllegalArgumentException("The payload ID parts is null.");
		}
		else if(payloadIdParts.length < 2) {
			throw
				new IllegalArgumentException(
					"The payload ID parts is too short.");
		}
		
		// Get the domain's PayloadId class.
		Class<? extends PayloadId> clazz = REGISTRY.get(payloadIdParts[1]);
		if(clazz == null) {
			throw
				new IllegalArgumentException(
					"The domain is unknown: " + payloadIdParts[1]);
		}
		
		try {
			return
				clazz
					.getConstructor(String[].class)
					.newInstance((Object) payloadIdParts);
		}
		catch(NoSuchMethodException e) {
			throw
				new IllegalStateException(
					"An entry was added to the registry that did not have " +
						"the appropriate constructor.",
					e);
		}
		catch(SecurityException e) {
			throw
				new IllegalStateException(
					"The security manager is preventing the access of the " +
						"constructor that takes exactly an array of strings.",
					e);
		}
		catch(IllegalArgumentException e) {
			throw
				new IllegalStateException(
					"The constructor signature does not match the parameter " +
						"we are giving it despite doing a check first.",
					e);
		}
		catch(IllegalAccessException e) {
			throw
				new IllegalStateException(
					"The Java access control is preventing the access of " +
						"the constructor that takes exactly an array of " +
						"strings.",
					e);
		}
		catch(InstantiationException e) {
			throw new IllegalStateException("The class is abstract.", e);
		}
		catch(ExceptionInInitializerError e) {
			throw new IllegalStateException("Java's initializer failed.", e);
		}
		// The underlying constructor threw an exception. We need to see if it
		// is a DomainException, in which case, we can just echo it.
		catch(InvocationTargetException e) {
			Throwable cause = e.getCause();
			if(cause instanceof ValidationException) {
				throw (ValidationException) cause;
			}
			else {
				throw
					new IllegalStateException(
						"The underlying constructor threw an exception.",
					e);
			}
		}
	}
}