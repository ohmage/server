package org.ohmage.domain.appcontainer;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;


/**
 * <p>
 * A container for the different applications, keyed on their platform, that
 * can be referenced by streams and remote activity prompts.
 * </p>
 *
 * @author John Jenkins
 */
public class Apps {
    /**
     * The JSON key for an application for the Android platform.
     */
    public static final String JSON_KEY_PLATFORM_ANDROID = "android";

    /**
     * The JSON key for an application for the iOS platform.
     */
    public static final String JSON_KEY_PLATFORM_IOS = "ios";

    /**
     * The application data for the Android platform.
     */
    @JsonProperty(JSON_KEY_PLATFORM_ANDROID)
    private final AndroidAppInformation android;

    /**
     * The application data for the iOS platform.
     */
    @JsonProperty(JSON_KEY_PLATFORM_IOS)
    private final AppInformation ios;

    /**
     * Creates a new object with all of the information about the different
     * platform applications.
     *
     * @param ios
     *        The information about the application for the iOS platform.
     *
     * @param android
     *        The information about the application for the Android platform.
     */
    @JsonCreator
    public Apps(
        @JsonProperty(JSON_KEY_PLATFORM_IOS) final AppInformation ios,
        @JsonProperty(JSON_KEY_PLATFORM_ANDROID)
            final AndroidAppInformation android) {

        this.ios = ios;
        this.android = android;
    }

    /**
     * Returns the application information for the iOS platform. This may be
     * null.
     *
     * @return The application information for the iOS platform or null if no
     *         such information was given.
     */
    @JsonIgnore
    public AppInformation getIosAppInformation() {
        return ios;
    }

    /**
     * Returns the application information for the Android platform. This may
     * be null.
     *
     * @return The application information for the Android platform or null if
     *         no such information was given.
     */
    @JsonIgnore
    public AndroidAppInformation getAndroidAppInformation() {
        return android;
    }
}