package org.ohmage.auth.provider;

import java.util.HashMap;
import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;

/**
 * <p>
 * A centralized collection of {@link Provider}s.
 * </p>
 * 
 * <p>
 * When the application starts, a series of calls to
 * {@link #register(Provider)} should be made, one for each unique provider.
 * Then, {@link #get(String)} should be used to retrieve the desired instances
 * of those providers.
 * </p>
 * 
 * @author John Jenkins
 */
public class ProviderRegistry {
	/**
	 * <p>
	 * An exception specifically for when the registry does not know about a
	 * provider.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class UnknownProviderException extends OhmageException {
		/**
		 * An ID for this version of this class for serialization purposes.
		 */
		private static final long serialVersionUID = 1L;

		/**
		 * Creates a new exception with a reason.
		 * 
		 * @param reason
		 *        The reason this exception was thrown.
		 */
		public UnknownProviderException(final String reason) {
			super(reason);
		}
	}
	
	/**
	 * The map of provider IDs to their Provider objects.
	 */
	private static final Map<String, Provider> REGISTRY =
		new HashMap<String, Provider>();
	
	/**
	 * Registers a provider object that can now be retrieved via
	 * {@link #get(String)}.
	 * 
	 * @param registree
	 *        The provider that is being registered.
	 * 
	 * @throws IllegalArgumentException
	 *         The registree was null or a provider has already registered with
	 *         that ID.
	 */
	public static void register(
		final Provider registree)
		throws IllegalArgumentException {
		
		if(registree == null) {
			throw new IllegalArgumentException("The registree is null.");
		}
		
		String registreeId = registree.getId();
		
		if(REGISTRY.containsKey(registreeId)) {
			throw
				new IllegalArgumentException(
					"A provider with this key has already registered: " +
						registreeId);
		}
		
		REGISTRY.put(registreeId, registree);
	}
	
	/**
	 * Returns the provider associated with the given provider ID. Even if such
	 * a provider exists, they must first be {@link #register(Provider)}ed.
	 * 
	 * @param providerId
	 *        The unique identifier of the desired provider.
	 * 
	 * @return The Provider object associated with the provider ID.
	 * 
	 * @throws InvalidArgumentException
	 *         The provider ID is null.
	 * 
	 * @throws UnknownProviderException
	 *         The provider is unknown.
	 */
	public static Provider get(
		final String providerId)
		throws InvalidArgumentException {
		
		if(providerId == null) {
			throw new InvalidArgumentException("The provider ID is null.");
		}
		
		Provider result = REGISTRY.get(providerId);
		if(result == null) {
			throw new UnknownProviderException("The provider is unknown.");
		}
		
		return result;
	}
}