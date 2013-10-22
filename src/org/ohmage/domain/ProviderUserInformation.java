package org.ohmage.domain;

import org.ohmage.domain.exception.OhmageException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The information about a user including the user's ohmage user-name as given
 * by the provider.
 * </p>
 * 
 * @author John Jenkins
 */
public class ProviderUserInformation {
	/**
	 * <p>
	 * A builder for {@link ProviderUserInformation} objects.
	 * </p> 
	 *
	 * @author John Jenkins
	 */
	public static class Builder {
		private String providerId;
		private String id;
		private String email;
		
		/**
		 * Creates an initial Builder with the required parameters.
		 * 
		 * @param providerId
		 *        The provider's unique identifier.
		 * 
		 * @param userId
		 *        The provider-generated, consistent, unique identifier for
		 *        this user.
		 * 
		 * @param email
		 *        The provider-supplied, validated email address for this user.
		 */
		public Builder(
			final String providerId,
			final String id,
			final String email) {
			
			this.providerId = providerId;
			this.id = id;
			this.email = email;
		}

		/**
		 * Returns the provider's internal identifier.
		 * 
		 * @return The provider's internal identifier.
		 */
		public String getProviderId() {
			return providerId;
		}

		/**
		 * Returns the provider-generated, consistent, unique identifier for this
		 * user.
		 * 
		 * @return The provider-generated, consistent, unique identifier for this
		 *         user.
		 */
		public String getId() {
			return id;
		}

		/**
		 * Returns the provider-supplied, validated email address for this user.
		 * 
		 * @return The provider-supplied, validated email address for this user.
		 */
		public String getEmail() {
			return email;
		}
		
		/**
		 * Builds the ProviderUserInformation object.
		 * 
		 * @return The ProviderUserInformation object, based on the state of
		 *         the builder.
		 * 
		 * @throws OhmageException
		 *         The state of the builder contained invalid fields.
		 */
		public ProviderUserInformation build() {
			return new ProviderUserInformation(providerId, id, email);
		}
	}
	
	/**
	 * The JSON key for the provider's internal identifier.
	 */
	public static final String JSON_KEY_PROVIDER_ID = "provider_id";
	/**
	 * The JSON key for the provider-generated, consistent unique identifier
	 * for this user.
	 */
	public static final String JSON_KEY_USER_ID = "user_id";
	/**
	 * The JSON key for the provider-supplied, validated email address for this
	 * user.
	 */
	public static final String JSON_KEY_EMAIL = "email";
	
	/**
	 * The provider's internal identifier.
	 */
	@JsonProperty(JSON_KEY_PROVIDER_ID)
	private final String providerId;

	/**
	 * The provider-generated, consistent unique identifier for this user.
	 */
	@JsonProperty(JSON_KEY_USER_ID)
	private final String userId;
	/**
	 * The provider-supplied, validated email address for this user.
	 */
	@JsonProperty(JSON_KEY_EMAIL)
	private final String email;

	/**
	 * Creates a set of information as given by some provider.
	 * 
	 * @param providerId
	 *        The provider's internal identifier.
	 * 
	 * @param userId
	 *        The provider-generated, consistent, unique identifier for this
	 *        user.
	 * 
	 * @param email
	 *        The provider-supplied, validated email address for this user.
	 * 
	 * @throws IllegalArgumentException
	 *         Any of the required parameters are null or invalid.
	 */
	@JsonCreator
	public ProviderUserInformation(
		@JsonProperty(JSON_KEY_PROVIDER_ID)
			final String providerId,
		@JsonProperty(JSON_KEY_USER_ID)
			final String userId,
		@JsonProperty(JSON_KEY_EMAIL)
			final String email)
		throws IllegalArgumentException {

		if(providerId == null) {
			throw new IllegalArgumentException("The provider ID is null.");
		}
		if(userId == null) {
			throw new IllegalArgumentException("The user's ID is null.");
		}
		if(email == null) {
			throw new IllegalArgumentException("The email address is null.");
		}

		this.providerId = providerId;
		this.userId = userId;
		this.email = email;
	}

	/**
	 * Returns the provider's internal identifier.
	 * 
	 * @return The provider's internal identifier.
	 */
	public String getProviderId() {
		return providerId;
	}

	/**
	 * Returns the provider-generated, consistent, unique identifier for this
	 * user.
	 * 
	 * @return The provider-generated, consistent, unique identifier for this
	 *         user.
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * Returns the provider-supplied, validated email address for this user.
	 * 
	 * @return The provider-supplied, validated email address for this user.
	 */
	public String getEmail() {
		return email;
	}
}