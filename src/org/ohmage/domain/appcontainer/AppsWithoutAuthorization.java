package org.ohmage.domain.appcontainer;

import org.ohmage.domain.exception.InvalidArgumentException;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * <p>
 * A container for the different applications, keyed on their platform, that
 * can be referenced by streams and remote activity prompts. This is unique in
 * that it does not allow an authorization URI.
 * </p>
 *
 * @author John Jenkins
 */
public class AppsWithoutAuthorization extends Apps {
    /**
     * Creates a new object with all of the information about the different
     * platform applications.
     *
     * @param ios
     *        The information about the application for the iOS platform.
     *
     * @param android
     *        The information about the application for the Android platform.
     *
     * @throws InvalidArgumentException
     *         An authorization URI was given.
     */
    @JsonCreator
    public AppsWithoutAuthorization(
        @JsonProperty(JSON_KEY_PLATFORM_IOS) final AppInformation ios,
        @JsonProperty(JSON_KEY_PLATFORM_ANDROID)
            final AndroidAppInformation android)
        throws InvalidArgumentException {

        super(ios, android);

        // Check the authorization URI for the iOS platform.
        if(
            (getIosAppInformation() != null) &&
            (getIosAppInformation().getAuthorizationUri() != null)) {

            throw
                new InvalidArgumentException(
                    "The iOS application information has an authorization " +
                        "URI.");
        }

        // Check the authorization URI for the Android platform.
        if(
            (getAndroidAppInformation() != null) &&
            (getAndroidAppInformation().getAuthorizationUri() != null)) {

            throw
                new InvalidArgumentException(
                    "The Android application information has an " +
                        "authorization URI.");
        }
    }
}