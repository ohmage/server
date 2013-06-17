package org.ohmage.cache;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ohmage.domain.PayloadId;
import org.ohmage.domain.PayloadIdBuilder;
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
	private static final Map<String, PayloadIdBuilder> REGISTRY =
		new ConcurrentHashMap<String, PayloadIdBuilder>();
	
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
		final PayloadIdBuilder builder)
		throws IllegalArgumentException {
		
		// Null check.
		if(domain == null) {
			throw new IllegalArgumentException("The domain is null.");
		}
		else if(builder == null) {
			throw
				new IllegalArgumentException("The PayloadId builder is null.");
		}
		
		// Make sure the domain hasn't already been registered.
		if(REGISTRY.containsKey(domain)) {
			throw
				new IllegalArgumentException(
					"The domain has already been registered: " + domain);
		}
		
		// Add the domain to the registry.
		REGISTRY.put(domain, builder);
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
	 */
	public static PayloadId getPayloadId(
		final String[] payloadIdParts)
		throws ValidationException, IllegalArgumentException {
		
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
		PayloadIdBuilder builder = REGISTRY.get(payloadIdParts[1]);
		if(builder == null) {
			throw
				new IllegalArgumentException(
					"The domain is unknown: " + payloadIdParts[1]);
		}
		
		// Build the PayloadId and return it.
		return builder.build(payloadIdParts);
	}
}