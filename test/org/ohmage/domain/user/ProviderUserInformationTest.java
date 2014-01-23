package org.ohmage.domain.user;

import org.junit.Assert;
import org.junit.Test;

/**
 * <p>
 * Test everything about the {@link ProviderUserInformation} class.
 * </p>
 *
 * @author John Jenkins
 */
public class ProviderUserInformationTest {
    /**
     * A valid provider ID to use for testing.
     */
    public static final String PROVIDER_ID = "Provider.ID";
    /**
     * A valid user ID to use for testing.
     */
    public static final String USER_ID = UserTest.USERNAME;
    /**
     * A valid email address to use for testing.
     */
    public static final String EMAIL = UserTest.EMAIL;

    /**
     * Tests the creation of a {@link ProviderUserInformation} object where the
     * provider ID is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testProviderUserInformationProviderIdNull() {
        new ProviderUserInformation(null, USER_ID, EMAIL);
    }

    /**
     * Tests the creation of a {@link ProviderUserInformation} object where the
     * user ID is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testProviderUserInformationUserIdNull() {
        new ProviderUserInformation(PROVIDER_ID, null, EMAIL);
    }

    /**
     * Tests the creation of a {@link ProviderUserInformation} object where the
     * email is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testProviderUserInformationEmailNull() {
        new ProviderUserInformation(PROVIDER_ID, USER_ID, null);
    }

    /**
     * Tests the valid creation of a {@link ProviderUserInformation} object.
     */
    @Test
    public void testProviderUserInformation() {
        new ProviderUserInformation(PROVIDER_ID, USER_ID, EMAIL);
    }

    /**
     * Test that the provider ID used to create the
     * {@link ProviderUserInformation} is the same as the one that is returned.
     */
    @Test
    public void testGetProviderId() {
        Assert
            .assertEquals(
                PROVIDER_ID,
                (new ProviderUserInformation(PROVIDER_ID, USER_ID, EMAIL))
                    .getProviderId());
    }

    /**
     * Test that the user ID used to create the {@link ProviderUserInformation}
     * is the same as the one that is returned.
     */
    @Test
    public void testGetUserId() {
        Assert
            .assertEquals(
                USER_ID,
                (new ProviderUserInformation(PROVIDER_ID, USER_ID, EMAIL))
                    .getUserId());
    }

    /**
     * Test that the email address used to create the
     * {@link ProviderUserInformation} is the same as the one that is returned.
     */
    @Test
    public void testGetEmail() {
        Assert
            .assertEquals(
                EMAIL,
                (new ProviderUserInformation(PROVIDER_ID, USER_ID, EMAIL))
                    .getEmail());
    }
}