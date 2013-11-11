package org.ohmage.domain;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;
import org.ohmage.domain.jackson.MapValuesJsonSerializer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

/**
 * <p>
 * A ohmlet is a group of schemas, which define the data that the members of
 * the ohmlet should be collecting and are sharing with other members of the
 * ohmlet.
 * </p>
 * 
 * @author John Jenkins
 */
public class Ohmlet extends OhmageDomainObject {
	/**
	 * The publicly-facing name of the ohmlet.
	 */
	public static final String COMMUNITY_SKIN = "ohmlet";
	
	/**
	 * <p>
	 * A builder for {@link Ohmlet}s.
	 * </p>
	 * 
	 * @author John Jenkins
	 */
	public static class Builder extends OhmageDomainObject.Builder<Ohmlet> {
		/**
		 * The unique identifier for this ohmlet.
		 */
		private String ohmletId;
		/**
		 * The name of this ohmlet.
		 */
		private String name;
		/**
		 * The description of this ohmlet.
		 */
		private String description;
		/**
		 * The streams for this ohmlet.
		 */
		private List<SchemaReference> streams;
		/**
		 * The schemas for this ohmlet.
		 */
		private List<SchemaReference> surveys;
		/**
		 * The reminders for this ohmlet that define when certain surveys
		 * should be prompted for the user.
		 */
		private List<String> reminders;
		/**
		 * The members that are part of this ohmlet.
		 */
		private Map<String, Member> members;
		/**
		 * The {@link PrivacyState} of the ohmlet.
		 */
		private PrivacyState privacyState;
		/**
		 * The minimum required {@link Role} that a user must have to invite other
		 * users.
		 */
		private Role inviteRole;
		/**
		 * The minimum required {@link Role} that a user must have to see other
		 * users' data.
		 */
		private Role visibilityRole;
		
		/**
		 * Creates a new Ohmlet builder object.
		 * 
		 * @param name
		 *        The name of this ohmlet.
		 * 
		 * @param description
		 *        The description of this ohmlet.
		 * 
		 * @param owner
		 *        The creator and subsequent owner of this ohmlet.
		 * 
		 * @param streams
		 *        The list of streams that compose this ohmlet.
		 * 
		 * @param surveys
		 *        The list of surveys that compose this ohmlet.
		 * 
		 * @param reminders
		 *        The list of default reminders for users that download this
		 *        ohmlet.
		 * 
		 * @param members
		 *        The map of member IDs to their roles of this ohmlet.
		 * 
		 * @param privacyState
		 *        The {@link PrivacyState} of this ohmlet.
		 * 
		 * @param inviteRole
		 *        The minimum required {@link Role} to invite other users to
		 *        this ohmlet.
		 * 
		 * @param visibilityRole
		 *        The minimum required {@link Role} to view data supplied by
		 *        the members of this ohmlet.
		 */
		public Builder(
			@JsonProperty(JSON_KEY_NAME) final String name,
			@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
			@JsonProperty(JSON_KEY_STREAMS)
				final List<SchemaReference> streams,
			@JsonProperty(JSON_KEY_SURVEYS)
				final List<SchemaReference> surveys,
			@JsonProperty(JSON_KEY_REMINDERS) final List<String> reminders,
			@JsonProperty(JSON_KEY_PRIVACY_STATE)
				final PrivacyState privacyState,
			@JsonProperty(JSON_KEY_INVITE_ROLE) final Role inviteRole,
			@JsonProperty(JSON_KEY_VISIBILITY_ROLE)
				final Role visibilityRole) {
			
			super(null);
			
			this.name = name;
			this.description = description;
			this.streams = streams;
			this.surveys = surveys;
			this.reminders = reminders;
			this.privacyState = privacyState;
			this.inviteRole = inviteRole;
			this.visibilityRole = visibilityRole;
		}

		/**
		 * Creates a new builder based on an existing Ohmlet object.
		 * 
		 * @param ohmlet
		 *        The existing Ohmlet object on which this Builder should be
		 *        based.
		 */
		public Builder(final Ohmlet ohmlet) {
			super(ohmlet);
			
			this.ohmletId = ohmlet.ohmletId;
			this.name = ohmlet.name;
			this.description = ohmlet.description;
			this.streams = ohmlet.streams;
			this.surveys = ohmlet.surveys;
			this.reminders = ohmlet.reminders;
			this.members = ohmlet.members;
			this.privacyState = ohmlet.privacyState;
			this.inviteRole = ohmlet.inviteRole;
			this.visibilityRole = ohmlet.visibilityRole;
		}
		
		/**
		 * Takes the non-null contents of the parameterized builder and
		 * overwrites the corresponding value in this builder.
		 * 
		 * @param builder
		 *        The builder whose values should be merged into this builder.
		 * 
		 * @return This builder to facilitate chaining.
		 */
		public Builder merge(final Builder builder) {
			if(builder == null) {
				return this;
			}

			if(builder.name != null) {
				this.name = builder.name;
			}
			
			if(builder.description != null) {
				this.description = builder.description;
			}
			
			if(builder.streams != null) {
				this.streams = builder.streams;
			}
			
			if(builder.surveys != null) {
				this.surveys = builder.surveys;
			}
			
			if(builder.reminders != null) {
				this.reminders = builder.reminders;
			}
			
			if(builder.privacyState != null) {
				this.privacyState = builder.privacyState;
			}
			
			if(builder.inviteRole != null) {
				this.inviteRole = builder.inviteRole;
			}
			
			if(builder.visibilityRole != null) {
				this.visibilityRole = builder.visibilityRole;
			}
			
			return this;
		}
		
		/**
		 * Sets the name of this ohmlet.
		 * 
		 * @param name
		 *        The new name for this ohmlet.
		 * 
		 * @return This builder to facilitate chaining.
		 */
		public Builder setName(final String name) {
			this.name = name;
			
			return this;
		}
		
		/**
		 * Adds a member to this ohmlet.
		 * 
		 * @param username
		 *        The user's user-name.
		 * 
		 * @param role
		 *        The member's role in the ohmlet.
		 * 
		 * @return This builder to facilitate chaining.
		 */
		public Builder addMember(final String username, final Role role) {
			if(this.members == null) {
				this.members = new HashMap<String, Member>();
			}
			
			this.members.put(username, new Member(username, role));
			
			return this;
		}
		
		/**
		 * Removes a user from a ohmlet.
		 * 
		 * @param username The user-name of the user to remove.
		 * 
		 * @return This builder to facilitate chaining.
		 */
		public Builder removeMember(final String username) {
			if(this.members == null) {
				return this;
			}
			
			this.members.remove(username);
			
			return this;
		}
		
		/**
		 * Creates a new Ohmlet object from the state of this builder.
		 * 
		 * @return A new Ohmlet object from the state of this builder.
		 * 
		 * @throws OhmageException
		 *         The state of the builder contained invalid fields.
		 */
		public Ohmlet build() {
			return
				new Ohmlet(
					(ohmletId == null) ? getRandomId() : ohmletId,
					name, 
					description,
					streams,
					surveys, 
					reminders,
					(members == null) ? null : members.values(),
					privacyState,
					inviteRole,
					visibilityRole,
					internalReadVersion,
					internalWriteVersion);
		}
	}
	
	/**
	 * <p>
	 * A reference to a specific schema. Optionally, it may also specify a
	 * specific version of that schema. If not, the latest version will always
	 * be used.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class SchemaReference {
		/**
		 * The JSON key for the schema ID.
		 */
		public static final String JSON_KEY_SCHEMA_ID = "schema_id";
		/**
		 * The JSON key for the schema version.
		 */
		public static final String JSON_KEY_VERSION = "version";
		
		/**
		 * The schema ID.
		 */
		@JsonProperty(JSON_KEY_SCHEMA_ID)
		private final String schemaId;
		/**
		 * The schema version.
		 */
		@JsonProperty(JSON_KEY_VERSION)
		private final Long version;
		
		/**
		 * Creates or recreates a reference to a schema. The version may be
		 * null, indicating that the latest version should always be used.
		 * 
		 * @param schemaId
		 *        The schema's unique identifier.
		 * 
		 * @param version
		 *        The specific version that is being referenced or null if no
		 *        version is being referenced.
		 * 
		 * @throws IllegalArgumentException
		 *         The schema ID is null.
		 */
		@JsonCreator
		public SchemaReference(
			@JsonProperty(JSON_KEY_SCHEMA_ID) final String schemaId,
			@JsonProperty(JSON_KEY_VERSION) final Long version)
			throws IllegalArgumentException {
			
			if(schemaId == null) {
				throw new IllegalArgumentException("The schema ID is null.");
			}
			
			this.schemaId = schemaId;
			this.version = version;
		}
		
		/**
		 * Returns the unique identifier of the schema.
		 * 
		 * @return The unique identifier of the schema.
		 */
		public String getSchemaId() {
			return schemaId;
		}
		
		/**
		 * Returns the specific version of the schema or null if no version was
		 * specified.
		 * 
		 * @return The specific version of the schema or null if no version was
		 *         specified.
		 */
		public Long getVersion() {
			return version;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#hashCode()
		 */
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result =
				prime *
					result +
					((schemaId == null) ? 0 : schemaId.hashCode());
			result =
				prime * result + ((version == null) ? 0 : version.hashCode());
			return result;
		}

		/* (non-Javadoc)
		 * @see java.lang.Object#equals(java.lang.Object)
		 */
		@Override
		public boolean equals(final Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj == null) {
				return false;
			}
			if(!(obj instanceof SchemaReference)) {
				return false;
			}
			SchemaReference other = (SchemaReference) obj;
			if(schemaId == null) {
				if(other.schemaId != null) {
					return false;
				}
			}
			else if(!schemaId.equals(other.schemaId)) {
				return false;
			}
			if(version == null) {
				if(other.version != null) {
					return false;
				}
			}
			else if(!version.equals(other.version)) {
				return false;
			}
			return true;
		}
	}
	
	/**
	 * <p>
	 * The roles a user may have within a ohmlet.
	 * <p>
	 *
	 * @author John Jenkins
	 */
	public static enum Role {
		/**
		 * A user with this role has requested to become part of the ohmlet.
		 * A user only needs to request access when the {@link PrivacyState} is
		 * set to {@link PrivacyState#INVITE_ONLY}. A user with suitable
		 * permission may either invite the user by escalating their
		 * permissions to either {@link Role#INVITED} or {@link Role#MEMBER}. 
		 */
		REQUESTED,
		/**
		 * A user with this role has been invited to join the ohmlet. This
		 * can happen at any time, and it is now up to the user to decide to
		 * escalate their own permission level to {@link Role#MEMBER}.
		 */
		INVITED,
		/**
		 * <p>
		 * The typical role for a user, this role indicates that the user's
		 * data is visible to some members of the ohmlet. However, the
		 * {@link Ohmlet#visibilityRole} indicates the minimum role a user
		 * must have to view other users' data.
		 * </p>
		 * 
		 * <p>
		 * For example, if the ohmlet's visibility role is set to
		 * {@link Role#MODERATOR}, a user with the role {@link Role#MODERATOR}
		 * will be able to see the data that users have shared with this
		 * ohmlet, but users with this role will not. They will only be
		 * supplying data.
		 * </p>
		 */
		MEMBER,
		/**
		 * An elevated privilege, this role allows a user to make modifications
		 * to the state of the ohmlet but not delete it. This role is mainly
		 * for owners that would like to allow other users to modify the
		 * ohmlet without actually having ownership.
		 */
		MODERATOR,
		/**
		 * The highest privilege for a user, this role allows a user to do
		 * anything with the ohmlet including delete it.
		 */
		OWNER;
		
		/**
		 * Returns whether or not this role is the same as or greater than the
		 * given role.
		 * 
		 * @param role
		 *        The role to compare with this role.
		 * 
		 * @return True only if this role is just as privileged or more
		 *         privileged than the given role.
		 */
		public boolean encompases(final Role role) {
			return this.ordinal() >= role.ordinal();
		}
		
		/**
		 * Returns whether or not this role is greater than the given role.
		 * 
		 * @param role
		 *        The role to compare with this role.
		 * 
		 * @return True only if this role is more privileged than the given
		 *         role.
		 */
		public boolean supersedes(final Role role) {
			return this.ordinal() > role.ordinal();
		}
		
		/**
		 * Returns this role as a user-friendly string.
		 * 
		 * @return This role as a user-friendly string.
		 */
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	/**
	 * The absolute minimum allowed role for the {@link Ohmlet#inviteRole}.
	 */
	public static final Role MINIMUM_INVITE_ROLE = Role.MEMBER;
	/**
	 * The absolute minimum allowed role for the
	 * {@link Ohmlet#visibilityRole}.
	 */
	public static final Role MINIMUM_VISIBILITY_ROLE = Role.MEMBER;
	
	/**
	 * <p>
	 * A member of a ohmlet.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static class Member {
		/**
		 * The JSON key for the member's unique identifier.
		 */
		public static final String JSON_KEY_MEMBER_ID = "member_id";
		/**
		 * The JSON key for the member's role.
		 */
		public static final String JSON_KEY_ROLE = "role";
		
		/**
		 * The member's system-wide unique identifier.
		 */
		@JsonProperty(JSON_KEY_MEMBER_ID)
		private final String memberId;
		/**
		 * The member's ohmlet role.
		 */
		@JsonProperty(JSON_KEY_ROLE)
		private final Role role;
		
		/**
		 * Creates a new Member object.
		 * 
		 * @param memberId
		 *        The member's system-wide unique identifier.
		 * 
		 * @param role
		 *        The member's role in the ohmlet.
		 * 
		 * @throws InvalidArgumentException
		 *         The member's identifier or role were null.
		 */
		@JsonCreator
		public Member(
			@JsonProperty(JSON_KEY_MEMBER_ID) final String memberId,
			@JsonProperty(JSON_KEY_ROLE) final Role role)
			throws InvalidArgumentException {
			
			if(memberId == null) {
				throw new InvalidArgumentException("The member ID is null.");
			}
			if(role == null) {
				throw
					new InvalidArgumentException("The member's role is null.");
			}
			
			this.memberId = memberId;
			this.role = role;
		}
		
		/**
		 * Returns the member's system-wide unique identifier.
		 * 
		 * @return The member's system-wide unique identifier.
		 */
		public String getMemberId() {
			return memberId;
		}
		
		/**
		 * Returns the member's role in the ohmlet.
		 * 
		 * @return The member's role in the ohmlet.
		 */
		public Role getRole() {
			return role;
		}
	}
	
	/**
	 * <p>
	 * The allowed privacy states of the ohmlet.
	 * </p>
	 *
	 * @author John Jenkins
	 */
	public static enum PrivacyState {
		/**
		 * This privacy state indicates that the ohmlet will not be listed
		 * or searchable except by those that are members. Only members may
		 * invite other members.
		 */
		PRIVATE,
		/**
		 * This privacy state indicates that the ohmlet is listed and
		 * searchable, however, in order to join the ohmlet, you must be
		 * invited. This can be done by blind invitations by existing members
		 * or by having the user first request an invitation.
		 */
		INVITE_ONLY,
		/**
		 * This privacy state indicates that the ohmlet is listed and 
		 * searchable and that any user may join at any time.
		 */
		PUBLIC;
		
		/**
		 * Returns this role as a user-friendly string.
		 * 
		 * @return This role as a user-friendly string.
		 */
		@Override
		public String toString() {
			return this.name().toLowerCase();
		}
	}
	
	/**
	 * The JSON key for the ID.
	 */
	public static final String JSON_KEY_ID = "ohmlet_id";
	/**
	 * The JSON key for the name.
	 */
	public static final String JSON_KEY_NAME = "name";
	/**
	 * The JSON key for the description.
	 */
	public static final String JSON_KEY_DESCRIPTION = "description";
	/**
	 * The JSON key for the streams.
	 */
	public static final String JSON_KEY_STREAMS = "streams";
	/**
	 * The JSON key for the surveys.
	 */
	public static final String JSON_KEY_SURVEYS = "surveys";
	/**
	 * The JSON key for the reminders.
	 */
	public static final String JSON_KEY_REMINDERS = "reminders";
	/**
	 * The JSON key for the members.
	 */
	public static final String JSON_KEY_MEMBERS = "people";
	/**
	 * The JSON key for the privacy state.
	 */
	public static final String JSON_KEY_PRIVACY_STATE = "privacy_state";
	/**
	 * The JSON key for the invite role.
	 */
	public static final String JSON_KEY_INVITE_ROLE = "invite_role";
	/**
	 * The JSON key for the visibility role.
	 */
	public static final String JSON_KEY_VISIBILITY_ROLE = "visibility_role";

	/**
	 * The unique identifier for this ohmlet.
	 */
	@JsonProperty(JSON_KEY_ID)
	private final String ohmletId;
	/**
	 * The name of this ohmlet.
	 */
	@JsonProperty(JSON_KEY_NAME)
	private final String name;
	/**
	 * The description of this ohmlet.
	 */
	@JsonProperty(JSON_KEY_DESCRIPTION)
	private final String description;
	/**
	 * The streams for this ohmlet.
	 */
	@JsonProperty(JSON_KEY_STREAMS)
	private final List<SchemaReference> streams;
	/**
	 * The surveys for this ohmlet.
	 */
	@JsonProperty(JSON_KEY_SURVEYS)
	private final List<SchemaReference> surveys;
	/**
	 * The reminders for this ohmlet that define when certain surveys should
	 * be prompted for the user.
	 */
	@JsonProperty(JSON_KEY_REMINDERS)
	private final List<String> reminders;
	/**
	 * The members that are part of this ohmlet.
	 */
	@JsonProperty(JSON_KEY_MEMBERS)
	@JsonSerialize(using = MapValuesJsonSerializer.class)
	private final Map<String, Member> members;
	/**
	 * The {@link PrivacyState} of the ohmlet.
	 */
	@JsonProperty(JSON_KEY_PRIVACY_STATE)
	private final PrivacyState privacyState;
	/**
	 * The minimum required {@link Role} that a user must have to invite other
	 * users.
	 */
	@JsonProperty(JSON_KEY_INVITE_ROLE)
	private final Role inviteRole;
	/**
	 * The minimum required {@link Role} that a user must have to see other
	 * users' data.
	 */
	@JsonProperty(JSON_KEY_VISIBILITY_ROLE)
	private final Role visibilityRole;
	
	/**
	 * Creates a new ohmlet.
	 * 
	 * @param owner
	 *        The username of the user that is creating this ohmlet.
	 * 
	 * @param name
	 *        The name of this ohmlet.
	 * 
	 * @param description
	 *        The description of this ohmlet.
	 * 
	 * @param streams
	 *        The list of streams that compose this ohmlet.
	 * 
	 * @param surveys
	 *        The list of surveys that compose this ohmlet.
	 * 
	 * @param reminders
	 *        The list of default reminders for users that download this
	 *        ohmlet.
	 * 
	 * @param members
	 *        The members of this ohmlet.
	 * 
	 * @param privacyState
	 *        The {@link PrivacyState} of this ohmlet.
	 * 
	 * @param inviteRole
	 *        The minimum required {@link Role} to invite other users to this
	 *        ohmlet.
	 * 
	 * @param visibilityRole
	 *        The minimum required {@link Role} to view data supplied by the
	 *        members of this ohmlet.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	public Ohmlet(
		final String name,
		final String description,
		final List<SchemaReference> streams,
		final List<SchemaReference> surveys,
		final List<String> reminders,
		final List<Member> members,
		final PrivacyState privacyState,
		final Role inviteRole,
		final Role visibilityRole)
		throws IllegalArgumentException {
		
		// Pass through to the builder constructor.
		this(
			getRandomId(),
			name,
			description,
			streams,
			surveys,
			reminders,
			members,
			privacyState,
			inviteRole,
			visibilityRole,
			null);
	}

	/**
	 * Recreates an existing ohmlet.
	 * 
	 * @param id
	 *        The ID for the ohmlet or null for a randomly generated one.
	 * 
	 * @param name
	 *        The name of this ohmlet.
	 * 
	 * @param description
	 *        The description of this ohmlet.
	 * 
	 * @param streams
	 *        The list of streams that compose this ohmlet.
	 * 
	 * @param surveys
	 *        The list of surveys that compose this ohmlet.
	 * 
	 * @param reminders
	 *        The reminders for this ohmlet.
	 * 
	 * @param members
	 *        The members of this ohmlet.
	 * 
	 * @param privacyState
	 *        The {@link PrivacyState} of this ohmlet.
	 * 
	 * @param inviteRole
	 *        The minimum required {@link Role} to invite other users to this
	 *        ohmlet.
	 * 
	 * @param visibilityRole
	 *        The minimum required {@link Role} to view data supplied by the
	 *        members of this ohmlet.
	 * 
	 * @param internalVersion
	 *        The internal version of this ohmlet.
	 * 
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	@JsonCreator
	protected Ohmlet(
		@JsonProperty(JSON_KEY_ID) final String id,
		@JsonProperty(JSON_KEY_NAME) final String name,
		@JsonProperty(JSON_KEY_DESCRIPTION) final String description,
		@JsonProperty(JSON_KEY_STREAMS) final List<SchemaReference> streams,
		@JsonProperty(JSON_KEY_SURVEYS) final List<SchemaReference> surveys,
		@JsonProperty(JSON_KEY_REMINDERS) final List<String> reminders,
		@JsonProperty(JSON_KEY_MEMBERS) final List<Member> members,
		@JsonProperty(JSON_KEY_PRIVACY_STATE) final PrivacyState privacyState,
		@JsonProperty(JSON_KEY_INVITE_ROLE) final Role inviteRole,
		@JsonProperty(JSON_KEY_VISIBILITY_ROLE) final Role visibilityRole,
		@JsonProperty(JSON_KEY_INTERNAL_VERSION) final Long internalVersion)
		throws IllegalArgumentException, InvalidArgumentException {
		
		// Pass through to the builder constructor.
		this(
			id, 
			name, 
			description,
			streams, 
			surveys, 
			reminders,
			members,
			privacyState,
			inviteRole,
			visibilityRole,
			internalVersion, 
			null);
	}
	
	/**
	 * Builds the Ohmlet object.
	 * 
	 * @param id
	 *        The ID for the ohmlet or null for a randomly generated one.
	 * 
	 * @param name
	 *        The name of this ohmlet.
	 * 
	 * @param description
	 *        The description of this ohmlet.
	 * 
	 * @param streams
	 *        The list of streams that compose this ohmlet.
	 * 
	 * @param surveys
	 *        The list of surveys that compose this ohmlet.
	 * 
	 * @param reminders
	 *        The list of default reminders for users that download this
	 *        ohmlet.
	 * 
	 * @param members
	 *        The members of this ohmlet.
	 * 
	 * @param privacyState
	 *        The {@link PrivacyState} of this ohmlet.
	 * 
	 * @param inviteRole
	 *        The minimum required {@link Role} to invite other users to this
	 *        ohmlet.
	 * 
	 * @param visibilityRole
	 *        The minimum required {@link Role} to view data supplied by the
	 *        members of this ohmlet.
	 * 
	 * @param internalReadVersion
	 *        The internal version of this ohmlet when it was read from the
	 *        database.
	 * 
	 * @param internalWriteVersion
	 *        The new internal version of this ohmlet when it will be
	 *        written back to the database.
	 * 
	 * @throws IllegalArgumentException
	 *         The ID is invalid.
	 * 
	 * @throws InvalidArgumentException
	 *         A parameter is invalid.
	 */
	private Ohmlet(
		final String id,
		final String name,
		final String description,
		final List<SchemaReference> streams,
		final List<SchemaReference> surveys,
		final List<String> reminders,
		final Collection<Member> members,
		final PrivacyState privacyState,
		final Role inviteRole,
		final Role visibilityRole,
		final Long internalReadVersion,
		final Long internalWriteVersion)
		throws IllegalArgumentException, InvalidArgumentException {
		
		// Initialize the parent.
		super(internalReadVersion, internalWriteVersion);

		// Validate the parameters.
		if(id == null) {
			throw new IllegalArgumentException("The ID is null.");
		}
		if(name == null) {
			throw new InvalidArgumentException("The name is null.");
		}
		if(description == null) {
			throw new InvalidArgumentException("The description is null.");
		}
		if(members == null) {
			throw new InvalidArgumentException("The list of members is null.");
		}
		if(privacyState == null) {
			throw
				new InvalidArgumentException(
					"The privacy state is null or unknown.");
		}
		if(inviteRole == null) {
			throw
				new InvalidArgumentException(
					"The minimum role to invite other users is null or " +
						"unknown.");
		}
		if(! inviteRole.encompases(MINIMUM_INVITE_ROLE)) {
			throw
				new InvalidArgumentException(
					"The minimum role to invite other users is less than " +
						"the minimum allowed role of '" +
						MINIMUM_INVITE_ROLE.toString().toLowerCase() +
						"'.");
		}
		if(visibilityRole == null) {
			throw
				new InvalidArgumentException(
					"The minimum role to view users' data is null or " +
						"unknown.");
		}
		if(! visibilityRole.encompases(MINIMUM_VISIBILITY_ROLE)) {
			throw
				new InvalidArgumentException(
					"The minimum role to view other user's data is less " +
						"than the minimum allowed role of '" +
						MINIMUM_VISIBILITY_ROLE.toString().toLowerCase() +
						"'.");
		}

		// Save the state.
		this.ohmletId = id;
		this.name = name;
		this.description = description;
		this.streams =
			((streams == null) ?
				Collections.<SchemaReference>emptyList() :
				Collections.unmodifiableList(streams));
		this.surveys =
			((surveys == null) ?
				Collections.<SchemaReference>emptyList() :
				Collections.unmodifiableList(surveys));
		this.reminders =
			((reminders == null) ?
				Collections.<String>emptyList() :
				Collections.unmodifiableList(reminders));
		this.privacyState = privacyState;
		this.inviteRole = inviteRole;
		this.visibilityRole = visibilityRole;
		
		this.members = new HashMap<String, Member>();
		for(Member member : members) {
			this.members.put(member.getMemberId(), member);
		}
	}
	
	/**
	 * Returns the unique identifier for this ohmlet.
	 * 
	 * @return The unique identifier for this ohmlet.
	 */
	public String getId() {
		return ohmletId;
	}
	
	/**
	 * Returns an unmodifiable list of the streams.
	 * 
	 * @return An unmodifiable list of the streams.
	 */
	public List<SchemaReference> getStreams() {
		return streams;
	}
	
	/**
	 * Returns an unmodifiable list of the surveys.
	 * 
	 * @return An unmodifiable list of the surveys.
	 */
	public List<SchemaReference> getSurveys() {
		return surveys;
	}
	
	/**
	 * Returns the privacy state for this ohmlet.
	 * 
	 * @return The privacy state for this ohmlet.
	 */
	public PrivacyState getPrivacyState() {
		return privacyState;
	}
	
	/**
	 * Returns the minimum required role to invite users to this ohmlet.
	 * 
	 * @return The minimum required role to invite users to this ohmlet.
	 */
	public Role getInviteRole() {
		return inviteRole;
	}
	
	/**
	 * Returns whether or not a user has any role within the ohmlet.
	 * 
	 * @param username
	 *        The user's user-name.
	 * 
	 * @return Whether or not a user has any role within the ohmlet.
	 */
	public boolean hasRole(final String username) {
		return members.containsKey(username);
	}
	
	/**
	 * Returns whether or not a user has a role in the ohmlet that is equal
	 * to or greater than some specific role.
	 * 
	 * @param username
	 *        The user's user-name.
	 * 
	 * @param role
	 *        The specific role.
	 * 
	 * @return True if the user has a role greater than or equal to some
	 *         specific role.
	 */
	public boolean hasRole(final String username, final Role role) {
		Member member = members.get(username);
		
		if(username == null) {
			return false;
		}
		
		return member.getRole().encompases(role);
	}
	
	/**
	 * Returns the role of the user.
	 * 
	 * @param username
	 *        The user's user-name.
	 * 
	 * @return The user's role in the ohmlet or null if the user has no role
	 *         in the ohmlet.
	 */
	public Role getRole(final String username) {
		Member member = members.get(username);
		
		if(username == null) {
			return null;
		}
		
		return member.getRole();
	}
	
	/**
	 * Returns whether or not a user is allowed to modify a ohmlet. This
	 * includes everything except the the people and their roles within the
	 * ohmlet.
	 * 
	 * @param username The user's user-name.
	 * 
	 * @return True if the user is allowed to modify the ohmlet.
	 */
	public boolean canModifyOhmlet(final String username) {
		return hasRole(username, Role.MODERATOR);
	}
}