package org.ohmage.domain.auth;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * The scope for a given {@link AuthorizationCode}. This includes the schema
 * type (stream or survey), the schema's unique identifier, the schema's
 * version or all versions, and the permissions.
 * </p>
 *
 * @author John Jenkins
 */
public class Scope {
    /**
     * <p>
     * A builder for {@link Scope} objects.
     * </p>
     *
     * @author John Jenkins
     */
    public static class Builder {
        /**
         * The {@link Type} of the schema.
         */
        private final Type type;
        /**
         * The unique identifier for the schema.
         */
        private final String schemaId;
        /**
         * The version for the schema or null if all versions are allowed.
         */
        private final Long schemaVersion;
        /**
         * The {@link Permission} associated with this scope.
         */
        private final Permission permission;

        /**
         * Seeds a builder with the string format for scopes. This is:
         * <tt>/{type}/{id}/&lt;{version} | *&gt;/{permission}</tt>.
         *
         * @param scope
         *        The scope string to be de-constructed.
         *
         * @throws InvalidArgumentException
         *         The string is not of the correct format.
         */
        public Builder(final String scope) throws InvalidArgumentException {
            if(! scope.startsWith("/")) {
                throw
                    new InvalidArgumentException(
                        "The scope must start with '/'");
            }

            String[] parts = scope.split("/");
            if(parts.length != 5) {
                throw
                    new InvalidArgumentException(
                        "The scope must be of the form: " +
                            "/<streams | surveys>" +
                            "/{id}" +
                            "/<{version} | *>" +
                            "/<read | write | delete>");
            }

            type = Type.translate(parts[1]);
            if(type == null) {
                throw
                    new InvalidArgumentException(
                        "The type '" +
                            parts[1] +
                            "' is unknown. It must be one of: " +
                            Type.LOOKUP.keySet());
            }

            schemaId = parts[2];
            schemaVersion =
                (VERSION_WILDCARD.equals(parts[3])) ?
                    null :
                    Long.decode(parts[3]);

            permission = Permission.translate(parts[4]);
            if(permission == null) {
                throw
                    new InvalidArgumentException(
                        "The permission '" +
                            parts[4] +
                            "' is unknown. It must be one of: " +
                            Permission.LOOKUP.keySet());
            }
        }

        /**
         * Builds a new {@link Scope} object based on the state of this
         * builder.
         *
         * @return A new {@link Scope} object based on the state of this
         *         builder.
         *
         * @throws InvalidArgumentException
         *         The state of this builder is not suitable for creating a new
         *         {@link Scope} object.
         */
        public Scope build() {
            return new Scope(type, schemaId, schemaVersion, permission);
        }
    }

    /**
     * <p>
     * The type of the schema associated with this scope.
     * </p>
     *
     * @author John Jenkins
     */
    public static enum Type {
        /**
         * The schema represents a stream.
         */
        STREAM ("streams"),
        /**
         * The schema represents a survey.
         */
        SURVEY ("surveys");

        /**
         * A quick lookup table for de-serializing types.
         */
        private static final Map<String, Type> LOOKUP =
            new HashMap<String, Type>();
        static {
            for(Type type : Type.values()) {
                LOOKUP.put(type.value, type);
            }
        }

        /**
         * The string value that is used to represent this type.
         */
        private final String value;

        /**
         * Creates this type.
         *
         * @param value
         *        The string value that represents this type.
         */
        private Type(final String value) {
            this.value = value;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return value;
        }

        /**
         * Returns the set of allowed values that can be translated into a
         * corresponding Type object.
         *
         * @return The set of allowed values that can be translated into a
         *         corresponding Type object.
         */
        public static Set<String> getAllowedValues() {
            return Collections.unmodifiableSet(LOOKUP.keySet());
        }

        /**
         * Translates a string value into a concrete Type object.
         *
         * @param value
         *        The string value to be translated.
         *
         * @return The corresponding Type object or null if the string value is
         *         unknown.
         */
        public static Type translate(final String value) {
            return LOOKUP.get(value);
        }
    }

    /**
     * <p>
     * Specific permissions that may be granted.
     * </p>
     *
     * @author John Jenkins
     */
    public static enum Permission {
        /**
         * Allows the bearer to read data corresponding to the given schema on
         * behalf of the user.
         */
        READ ("read"),
        /**
         * Allows the bearer to write data into the given schema on behalf of
         * the user.
         */
        WRITE ("write"),
        /**
         * Allows the bearer to delete data corresponding to the given schema
         * on behalf of the user.
         */
        DELETE ("delete");

        /**
         * A quick lookup table for de-serializing permissions.
         */
        public static final Map<String, Permission> LOOKUP =
            new HashMap<String, Permission>();
        static {
            for(Permission permission : Permission.values()) {
                LOOKUP.put(permission.value, permission);
            }
        }

        /**
         * The string value that is used to represent this permission.
         */
        private final String value;

        /**
         * Creates this permission.
         *
         * @param value
         *        The string value that represents this permission.
         */
        private Permission(final String value) {
            this.value = value;
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return value;
        }

        /**
         * Returns the set of allowed values that can be translated into a
         * corresponding Permission object.
         *
         * @return The set of allowed values that can be translated into a
         *         corresponding Permission object.
         */
        public static Set<String> getAllowedValues() {
            return Collections.unmodifiableSet(LOOKUP.keySet());
        }

        /**
         * Translates a string value into a concrete Permission object.
         *
         * @param value
         *        The string value to be translated.
         *
         * @return The corresponding Permission object or null if the string
         *         value is unknown.
         */
        public static Permission translate(final String value) {
            return LOOKUP.get(value);
        }
    }

    /**
     * The JSON key for the type.
     */
    public static final String JSON_KEY_TYPE = "type";
    /**
     * The JSON key for the schema's unique identifier.
     */
    public static final String JSON_KEY_SCHEMA_ID = "schema_id";
    /**
     * The JSON key for the schema's version.
     */
    public static final String JSON_KEY_SCHEMA_VERSION = "schema_version";
    /**
     * The JSON key for the permission.
     */
    public static final String JSON_KEY_PERMISSION = "permission";

    /**
     * The value for the version wildcard indicating that all versions of the
     * schema are being granted this permission.
     */
    public static final String VERSION_WILDCARD = "*";

    /**
     * The type of the schema.
     */
    @JsonProperty(JSON_KEY_TYPE)
    private final Type type;
    /**
     * The schema's unique identifier.
     */
    @JsonProperty(JSON_KEY_SCHEMA_ID)
    private final String schemaId;
    /**
     * The version of the schema or null indicating all versions.
     */
    @JsonProperty(JSON_KEY_SCHEMA_VERSION)
    private final Long schemaVersion;
    /**
     * The set of permissions.
     */
    @JsonProperty(JSON_KEY_PERMISSION)
    private final Permission permission;

    /**
     * Creates a new, or reconstructs an existing, scope.
     *
     * @param type
     *        The schema's type.
     *
     * @param schemaId
     *        The schema's unique identifier.
     *
     * @param schemaVersion
     *        The schema's version or null indicating all versions.
     *
     * @param permission
     *        The {@link Permission} associated with this scope.
     *
     * @throws InvalidArgumentException
     *         A parameter was invalid.
     */
    @JsonCreator
    public Scope(
        @JsonProperty(JSON_KEY_TYPE) final Type type,
        @JsonProperty(JSON_KEY_SCHEMA_ID) final String schemaId,
        @JsonProperty(JSON_KEY_SCHEMA_VERSION) final Long schemaVersion,
        @JsonProperty(JSON_KEY_PERMISSION) final Permission permission)
        throws InvalidArgumentException {

        if(type == null) {
            throw new InvalidArgumentException("The type is null.");
        }
        if(schemaId == null) {
            throw new InvalidArgumentException("The schema ID is null.");
        }
        if(permission == null) {
            throw new InvalidArgumentException("The permissions is null.");
        }

        this.type = type;
        this.schemaId = schemaId;
        this.schemaVersion = schemaVersion;
        this.permission = permission;
    }

    /**
     * Returns the type of the schema.
     *
     * @return The type of the schema.
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the schema's unique identifier.
     *
     * @return The schema's unique identifier.
     */
    public String getSchemaId() {
        return schemaId;
    }

    /**
     * Returns the schema's version or null if all versions are allowed.
     *
     * @return The schema's version or null if all versions are allowed.
     */
    public Long getSchemaVersion() {
        return schemaVersion;
    }

    /**
     * Returns the permission granted by this scope.
     *
     * @return The permission granted by this scope.
     */
    public Permission getPermission() {
        return permission;
    }

    /**
     * Determines if this scope covers the given scope.
     *
     * @param other
     *        The scope that must be covered by this scope.
     *
     * @return True if this scope covers the given scope; otherwise, false.
     */
    public boolean covers(final Scope other) {
        return
            type.equals(other.getType()) &&
            schemaId.equals(other.getSchemaId()) &&
            (
                (schemaVersion == null) ||
                schemaVersion.equals(other.getSchemaVersion())
            ) &&
            permission.equals(other.getPermission());
    }

    /**
     * Prints the scope as the specified string:
     * <tt>/{type}/{id}/&lt;{version} | *&gt;/{permission}</tt>.
     */
    @Override
    public String toString() {
        return
            (
                new StringBuilder("/").append(type.toString())
                    .append('/').append(schemaId)
                    .append('/')
                        .append(
                            (schemaVersion == null) ?
                                VERSION_WILDCARD :
                                schemaVersion)
                    .append('/').append(permission.toString()))
                .toString();
    }
}