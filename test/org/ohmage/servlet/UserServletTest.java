package org.ohmage.servlet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.easymock.EasyMock;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohmage.auth.provider.Provider;
import org.ohmage.auth.provider.ProviderRegistry;
import org.ohmage.auth.provider.ProviderRegistry.UnknownProviderException;
import org.ohmage.bin.OhmletBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.bin.SurveyBin;
import org.ohmage.bin.UserBin;
import org.ohmage.domain.AuthorizationToken;
import org.ohmage.domain.Ohmlet;
import org.ohmage.domain.Ohmlet.SchemaReference;
import org.ohmage.domain.exception.AuthenticationException;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.domain.survey.Survey;
import org.ohmage.domain.user.OhmletReference;
import org.ohmage.domain.user.ProviderUserInformation;
import org.ohmage.domain.user.Registration;
import org.ohmage.domain.user.User;
import org.ohmage.servlet.listener.ConfigurationFileImport;
import org.powermock.api.easymock.PowerMock;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

/**
 * <p>
 * Test everything about the {@link UserServlet} class.
 * </p>
 *
 * @author John Jenkins
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(
    {
        OhmletBin.class,
        ProviderRegistry.class,
        StreamBin.class,
        SurveyBin.class,
        User.class,
        UserBin.class
    })
public class UserServletTest {
    private static final String ROOT_URL = "http://localhost:8080/ohmage";

    private static final String USER_ID = "User.ID";
    private static final String PASSWORD = "password";
    private static final String EMAIL = "email@example.com";

    private static final String PROVIDER_ID = "Provider.ID";
    private static final String PROVIDER_ACCESS_TOKEN = "Provider.AccessToken";

    /**
     * The configuration file import object, which is needed by the
     * registration.
     */
    private static ConfigurationFileImport config;

    /**
     * Initialize the tests by initializing the configuration file importer.
     */
    @BeforeClass
    public static void init() {
        // Create a mock servlet context.
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/")).andReturn("web");
        EasyMock.replay(context);

        // Create a mock startup event.
        ServletContextEvent event =
            EasyMock.createMock(ServletContextEvent.class);
        EasyMock
            .expect(event.getServletContext())
            .andReturn(context)
            .anyTimes();
        EasyMock.replay(event);

        // Create the internal preferences.
        config = new ConfigurationFileImport();
        config.contextInitialized(event);
    }

    /**
     * Runs after all of the tests have finished.
     */
    @AfterClass
    public static void destroy() {
        // Create a mock servlet context.
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/")).andReturn("web");
        EasyMock.replay(context);

        // Create a mock shutdown event.
        ServletContextEvent event =
            EasyMock.createMock(ServletContextEvent.class);
        EasyMock
            .expect(event.getServletContext())
            .andReturn(context)
            .anyTimes();
        EasyMock.replay(event);

        // Destroy the internal preferences.
        config.contextDestroyed(event);
        config = null;
    }

    /**
     * A user to use for the tests.
     */
    private User user;
    /**
     * A user builder to use for the tests.
     */
    private User.Builder userBuilder;

    /**
     * The user bin to use for determining what gets sent to and/or returned
     * from the database.
     */
    private UserBin userBin;

    /**
     * Sets up the per-test components.
     */
    @Before
    public void setup() {
        user = EasyMock.createMock(User.class);
        userBuilder = EasyMock.createMock(User.Builder.class);

        PowerMock.mockStatic(UserBin.class);
        userBin = EasyMock.createMock(UserBin.class);
        EasyMock.expect(UserBin.getInstance()).andReturn(userBin).anyTimes();
        PowerMock.replay(UserBin.class);
    }

    /**
     * Tears down the per-test components.
     */
    @After
    public void tearDown() {
        user = null;
        userBuilder = null;
        userBin = null;
    }

    /**
     * Test user creation when the URL is null.
     */
    @Test(expected = IllegalStateException.class)
    public void testCreateOhmageUserUrlNull() {
        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createOhmageUser(null, PASSWORD, userBuilder);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation when the password is null.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateOhmageUserPasswordNull() {
        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createOhmageUser(ROOT_URL, null, userBuilder);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation when user information was not given.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateOhmageUserUserBuilderNull() {
        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createOhmageUser(ROOT_URL, PASSWORD, null);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation with a user-name that already exists..
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateOhmageUserDuplicateUsername() {
        EasyMock.expect(userBuilder.getId()).andReturn(USER_ID);
        EasyMock
            .expect(userBuilder.setPassword(PASSWORD, true))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.getEmail()).andReturn(EMAIL);
        EasyMock
            .expect(
                userBuilder
                    .setRegistration((Registration) EasyMock.anyObject()))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.build()).andReturn(user);

        userBin.addUser((User) EasyMock.anyObject());
        EasyMock
            .expectLastCall()
            .andThrow(new InvalidArgumentException("Duplicate user."));

        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createOhmageUser(ROOT_URL, PASSWORD, userBuilder);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateOhmageUserEmailExists() {
        EasyMock.expect(userBuilder.getId()).andReturn(USER_ID);
        EasyMock
            .expect(userBuilder.setPassword(PASSWORD, true))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.getEmail()).andReturn(EMAIL);
        EasyMock
            .expect(
                userBuilder
                    .setRegistration((Registration) EasyMock.anyObject()))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.build()).andReturn(user);

        Registration registration = EasyMock.createMock(Registration.class);
        registration.sendUserRegistrationEmail(EasyMock.anyString());
        EasyMock
            .expectLastCall()
            .andThrow(new IllegalArgumentException("Email failure."));
        EasyMock.expect(user.getRegistration()).andReturn(registration);

        userBin.addUser((User) EasyMock.anyObject());
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBuilder, userBin, registration);

        User result =
            UserServlet.createOhmageUser(ROOT_URL, PASSWORD, userBuilder);

        EasyMock.verify(user, userBuilder, userBin, registration);

        Assert
            .assertNotNull(
                "The created user does not have an email address.",
                result.getEmail());
    }

    /**
     * Test user creation.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testCreateOhmageUserActivationEmailFailure() {
        EasyMock.expect(userBuilder.getId()).andReturn(USER_ID);
        EasyMock
            .expect(userBuilder.setPassword(PASSWORD, true))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.getEmail()).andReturn(EMAIL);
        EasyMock
            .expect(
                userBuilder
                    .setRegistration((Registration) EasyMock.anyObject()))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.build()).andReturn(user);

        Registration registration = EasyMock.createMock(Registration.class);
        registration.sendUserRegistrationEmail(EasyMock.anyString());
        EasyMock
            .expectLastCall()
            .andThrow(new IllegalArgumentException("Email failure."));
        EasyMock.expect(user.getRegistration()).andReturn(registration);

        userBin.addUser((User) EasyMock.anyObject());
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBuilder, userBin, registration);

        UserServlet.createOhmageUser(ROOT_URL, PASSWORD, userBuilder);

        EasyMock.verify(user, userBuilder, userBin, registration);
    }

    /**
     * Test user creation.
     */
    @Test
    public void testCreateOhmageUser() {
        EasyMock.expect(userBuilder.getId()).andReturn(USER_ID);
        EasyMock
            .expect(userBuilder.setPassword(PASSWORD, true))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.getEmail()).andReturn(EMAIL);
        EasyMock
            .expect(
                userBuilder
                    .setRegistration((Registration) EasyMock.anyObject()))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.build()).andReturn(user);

        Registration registration = EasyMock.createMock(Registration.class);
        registration.sendUserRegistrationEmail(EasyMock.anyString());
        EasyMock.expectLastCall();
        EasyMock.expect(user.getRegistration()).andReturn(registration);

        userBin.addUser((User) EasyMock.anyObject());
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBuilder, userBin, registration);

        User result =
            UserServlet.createOhmageUser(ROOT_URL, PASSWORD, userBuilder);

        EasyMock.verify(user, userBuilder, userBin, registration);

        Assert
            .assertEquals(
                "The returned user object is not the same as the one created.",
                user,
                result);
    }

    /**
     * Test user creation from a provider.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateUserProviderNull() {
        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createUser(null, PROVIDER_ACCESS_TOKEN, userBuilder);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation from a provider.
     */
    @Test(expected = UnknownProviderException.class)
    public void testCreateUserProviderUnknown() {
        EasyMock.expect(userBuilder.getId()).andReturn(USER_ID);

        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createUser("Unknown", PROVIDER_ACCESS_TOKEN, userBuilder);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation from a provider.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateUserTokenNull() {
        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createUser(PROVIDER_ID, null, userBuilder);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation from a provider.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateUserTokenUnknown() {
        EasyMock.expect(userBuilder.getId()).andReturn(USER_ID);

        PowerMock.mockStatic(ProviderRegistry.class);
        Provider provider = EasyMock.createMock(Provider.class);
        EasyMock.expect(ProviderRegistry.get(PROVIDER_ID)).andReturn(provider);
        PowerMock.replay(ProviderRegistry.class);

        EasyMock
            .expect(provider.getUserInformation(EasyMock.anyString()))
            .andThrow(new InvalidArgumentException("The token is unknown."));

        EasyMock
            .replay(
                user,
                userBuilder,
                userBin,
                provider);

        UserServlet.createUser(PROVIDER_ID, "Unknown", userBuilder);

        EasyMock
            .verify(
                user,
                userBuilder,
                userBin,
                provider);
    }

    /**
     * Test user creation from a provider.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testCreateUserUserBuilderNull() {
        EasyMock.replay(user, userBuilder, userBin);

        UserServlet.createUser(PROVIDER_ID, PROVIDER_ACCESS_TOKEN, null);

        EasyMock.verify(user, userBuilder, userBin);
    }

    /**
     * Test user creation from a provider.
     */
    @Test
    public void testCreateUser() {
        PowerMock.mockStatic(ProviderRegistry.class);
        Provider provider = EasyMock.createMock(Provider.class);
        EasyMock.expect(ProviderRegistry.get(PROVIDER_ID)).andReturn(provider);
        PowerMock.replay(ProviderRegistry.class);

        ProviderUserInformation providerUserInformation =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(provider.getUserInformation(PROVIDER_ACCESS_TOKEN))
            .andReturn(providerUserInformation);
        EasyMock
            .expect(providerUserInformation.getProviderId())
            .andReturn(PROVIDER_ID);
        EasyMock.expect(providerUserInformation.getEmail()).andReturn(EMAIL);

        EasyMock
            .expect(
                userBuilder.addProvider(PROVIDER_ID, providerUserInformation))
            .andReturn(userBuilder);
        EasyMock.expect(userBuilder.setEmail(EMAIL)).andReturn(userBuilder);
        EasyMock.expect(userBuilder.build()).andReturn(user);

        userBin.addUser((User) EasyMock.anyObject());
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBuilder,
                userBin,
                provider,
                providerUserInformation);

        User result =
            UserServlet
                .createUser(PROVIDER_ID, PROVIDER_ACCESS_TOKEN, userBuilder);

        EasyMock
            .verify(
                user,
                userBuilder,
                userBin,
                provider,
                providerUserInformation);

        Assert
            .assertEquals(
                "The returned user object is not the same as the one created.",
                user,
                result);
    }

    /**
     * Test the listing of visible users.
     */
    @Test(expected = AuthenticationException.class)
    public void testGetVisibleUsersTokenNull() {
        UserServlet.getVisibleUsers(null);
    }

    /**
     * Test the listing of visible users.
     */
    @Test
    public void testGetVisibleUsers() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, userBin, token);

        Set<String> usernames = UserServlet.getVisibleUsers(token);

        EasyMock.verify(user, userBin, token);

        Assert
            .assertTrue(
                "Exactly one username should have been returned.",
                usernames.size() == 1);
        Assert
            .assertEquals(
                "The returned username was not the same as the token " +
                    "owner's username.",
                USER_ID,
                usernames.iterator().next());
    }

    /**
     * Test retrieving the user's personal information.
     */
    @Test(expected = AuthenticationException.class)
    public void testGetUserInformationTokenNull() {
        UserServlet.getUserInformation(null, USER_ID);
    }

    /**
     * Test retrieving the user's personal information.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testGetUserInformationUsernameNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.getUserInformation(token, null);

        EasyMock.verify(token);
    }

    /**
     * Test retrieving the user's personal information.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testGetUserInformationUsernameAnother() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, userBin, token);

        UserServlet.getUserInformation(token, "Other.Username");

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Test retrieving the user's personal information.
     */
    @Test
    public void testGetUserInformation() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, userBin, token);

        User user = UserServlet.getUserInformation(token, USER_ID);

        EasyMock.verify(user, userBin, token);

        Assert
            .assertEquals(
                "The internal user and the returned user are not the same.",
                this.user,
                user);
    }

    /**
     * Test updating the user's password.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUpdateUserPasswordUsernameNull() {
        String newPassword = "New.Password";

        UserServlet.updateUserPassword(null, PASSWORD, newPassword);
    }

    /**
     * Test updating the user's password.
     */
    @Test(expected = UnknownEntityException.class)
    public void testUpdateUserPasswordUsernameUnknown() {
        String newPassword = "New.Password";

        EasyMock.expect(userBin.getUser(EasyMock.anyString())).andReturn(null);

        EasyMock.replay(user, userBin);

        UserServlet.updateUserPassword("Unknown", PASSWORD, newPassword);

        EasyMock.verify(user, userBin);
    }

    /**
     * Test updating the user's password.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUpdateUserPasswordOldPasswordNull() {
        String newPassword = "New.Password";

        UserServlet.updateUserPassword(USER_ID, null, newPassword);
    }

    /**
     * Test updating the user's password.
     */
    @Test(expected = AuthenticationException.class)
    public void testUpdateUserPasswordOldPasswordWrong() {
        String newPassword = "New.Password";

        EasyMock.expect(userBin.getUser(USER_ID)).andReturn(user);
        EasyMock
            .expect(user.verifyPassword(EasyMock.anyString()))
            .andReturn(false);

        EasyMock.replay(user, userBin);

        UserServlet.updateUserPassword(USER_ID, "Wrong", newPassword);

        EasyMock.verify(user, userBin);
    }

    /**
     * Test updating the user's password.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUpdateUserPasswordNewPasswordNull() {
        UserServlet.updateUserPassword(USER_ID, PASSWORD, null);
    }

    /**
     * Test updating the user's password.
     */
    @Test
    public void testUpdateUserPassword() {
        String newPassword = "New.Password";
        String newPasswordHashed = "New.Password.Hashed";

        PowerMock.mockStatic(User.class);
        EasyMock
            .expect(User.hashPassword(newPassword))
            .andReturn(newPasswordHashed);
        PowerMock.replay(User.class);

        User newUser = EasyMock.createMock(User.class);

        EasyMock.expect(user.verifyPassword(PASSWORD)).andReturn(true);
        EasyMock
            .expect(user.updatePassword(newPasswordHashed))
            .andReturn(newUser);

        EasyMock.expect(userBin.getUser(USER_ID)).andReturn(user);
        userBin.updateUser(newUser);
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBin);

        UserServlet.updateUserPassword(USER_ID, PASSWORD, newPassword);

        EasyMock.verify(user, userBin);
    }

    /**
     * Test reading the user's followed ohmlets
     */
    @Test(expected = AuthenticationException.class)
    public void testGetFollowedCommunitiesTokenNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.getFollowedOhmlets(null, USER_ID);

        EasyMock.verify(token);
    }

    /**
     * Test reading the user's followed ohmlets
     */
    @Test(expected = InvalidArgumentException.class)
    public void testGetFollowedCommunitiesUsernameNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        EasyMock.replay(token);

        UserServlet.getFollowedOhmlets(token, null);

        EasyMock.verify(token);
    }

    /**
     * Test reading the user's followed ohmlets
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testGetFollowedCommunitiesUsernameAnother() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, token);

        UserServlet.getFollowedOhmlets(token, "Other");

        EasyMock.verify(user, token);
    }

    /**
     * Test reading the user's followed ohmlets
     */
    @Test
    public void testGetFollowedCommunities() {
        OhmletReference reference = EasyMock.createMock(OhmletReference.class);
        List<OhmletReference> expected = new ArrayList<OhmletReference>(1);
        expected.add(reference);

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);
        EasyMock.expect(user.getOhmlets()).andReturn(expected);

        EasyMock.replay(user, userBin, token);

        Collection<OhmletReference> actual =
            UserServlet.getFollowedOhmlets(token, USER_ID);

        EasyMock.verify(user, userBin, token);

        Assert
            .assertEquals(
                "The user did not return the expected ohmlets.",
                expected,
                actual);
    }

    /**
     * Test reading a specific ohmlet reference for a user.
     */
    @Test(expected = AuthenticationException.class)
    public void testGetFollowedOhmletTokenNull() {
        String ohmletId = "Ohmlet.ID";

        UserServlet.getFollowedOhmlet(null, USER_ID, ohmletId);
    }

    /**
     * Test reading a specific ohmlet reference for a user.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testGetFollowedOhmletUsernameNull() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.getFollowedOhmlet(token, null, ohmletId);

        EasyMock.verify(token);
    }

    /**
     * Test reading a specific ohmlet reference for a user.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testGetFollowedOhmletUsernameDifferent() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, token);

        UserServlet.getFollowedOhmlet(token, "Other", ohmletId);

        EasyMock.verify(user, token);
    }

    /**
     * Test reading a specific ohmlet reference for a user.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testGetFollowedOhmletIdNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.getFollowedOhmlet(token, USER_ID, null);

        EasyMock.verify(token);
    }

    /**
     * Test reading a specific ohmlet reference for a user.
     */
    @Test(expected = UnknownEntityException.class)
    public void testGetFollowedOhmletIdUnknown() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);
        EasyMock.expect(user.getOhmlet(EasyMock.anyString())).andReturn(null);

        EasyMock.replay(user, userBin, token);

        UserServlet.getFollowedOhmlet(token, USER_ID, "Unknown");

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Test reading a specific ohmlet reference for a user.
     */
    @Test
    public void testGetFollowedOhmlet() {
        String ohmletId = "Ohmlet.ID";

        OhmletReference reference = EasyMock.createMock(OhmletReference.class);

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);
        EasyMock.expect(user.getOhmlet(ohmletId)).andReturn(reference);

        EasyMock.replay(user, userBin, token);

        OhmletReference actual =
            UserServlet.getFollowedOhmlet(token, USER_ID, ohmletId);

        EasyMock.verify(user, userBin, token);

        Assert
            .assertEquals(
                "The user did not return the expected ohmlet reference.",
                reference,
                actual);
    }

    /**
     * Test stop following an ohmlet.
     */
    @Test(expected = AuthenticationException.class)
    public void testLeaveOhmletTokenNull() {
        String ohmletId = "Ohmlet.ID";

        UserServlet.leaveOhmlet(null, USER_ID, ohmletId);
    }

    /**
     * Test stop following an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testLeaveOhmletUsernameNull() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.leaveOhmlet(token, null, ohmletId);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Test stop following an ohmlet.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testLeaveOhmletUsernameDifferent() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, userBin, token);

        UserServlet.leaveOhmlet(token, "Other", ohmletId);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Test stop following an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testLeaveOhmletIdNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, userBin, token);

        UserServlet.leaveOhmlet(token, USER_ID, null);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Test stop following an ohmlet.
     */
    @Test
    public void testLeaveOhmletIdUnknown() {
        PowerMock.mockStatic(OhmletBin.class);
        OhmletBin ohmletBin = EasyMock.createMock(OhmletBin.class);
        EasyMock
            .expect(OhmletBin.getInstance())
            .andReturn(ohmletBin)
            .anyTimes();
        PowerMock.replay(OhmletBin.class);

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);
        EasyMock
            .expect(ohmletBin.getOhmlet(EasyMock.anyString()))
            .andReturn(null);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.leaveOhmlet(EasyMock.anyString()))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBin, token, ohmletBin);

        UserServlet.leaveOhmlet(token, USER_ID, "Unknown");

        EasyMock.verify(user, userBin, token, ohmletBin);
    }

    /**
     * Test stop following an ohmlet.
     */
    @Test
    public void testLeaveOhmlet() {
        String ohmletId = "Ohmlet.ID";

        PowerMock.mockStatic(OhmletBin.class);
        OhmletBin ohmletBin = EasyMock.createMock(OhmletBin.class);
        EasyMock
            .expect(OhmletBin.getInstance())
            .andReturn(ohmletBin)
            .anyTimes();
        PowerMock.replay(OhmletBin.class);

        Ohmlet ohmlet = EasyMock.createMock(Ohmlet.class);
        EasyMock.expect(ohmletBin.getOhmlet(ohmletId)).andReturn(ohmlet);

        Ohmlet updatedOhmlet = EasyMock.createMock(Ohmlet.class);
        EasyMock
            .expect(ohmlet.removeMember(USER_ID))
            .andReturn(updatedOhmlet);
        ohmletBin.updateOhmlet(updatedOhmlet);
        EasyMock.expectLastCall();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.leaveOhmlet(EasyMock.anyString()))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(user, userBin, token, ohmletBin, ohmlet, updatedOhmlet);

        UserServlet.leaveOhmlet(token, USER_ID, ohmletId);

        EasyMock
            .verify(user, userBin, token, ohmletBin, ohmlet, updatedOhmlet);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test(expected = AuthenticationException.class)
    public void testIgnoreOhmletStreamTokenNull() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .ignoreOhmletStream(null, USER_ID, ohmletId, streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletStreamUsernameNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .ignoreOhmletStream(token, null, ohmletId, streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testIgnoreOhmletStreamUsernameDifferent() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .ignoreOhmletStream(token, "Unknown", ohmletId, streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletStreamOhmletIdNull() {
        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet.ignoreOhmletStream(token, USER_ID, null, streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletStreamOhmletIdUnknown() {
        String unknownOhmletId = "Unknown";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.expect(user.getOhmlet(unknownOhmletId)).andReturn(null);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .ignoreOhmletStream(
                token,
                USER_ID,
                unknownOhmletId,
                streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletStreamStreamReferenceNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        EasyMock.replay(user, userBin, token);

        UserServlet.ignoreOhmletStream(token, USER_ID, ohmletId, null);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Tests ignoring a specific stream in an ohmlet.
     */
    @Test
    public void testIgnoreOhmletStream() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        OhmletReference originalOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        OhmletReference newOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        EasyMock
            .expect(user.getOhmlet(ohmletId))
            .andReturn(originalOhmletReference);
        EasyMock
            .expect(originalOhmletReference.ignoreStream(streamReference))
            .andReturn(newOhmletReference);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.upsertOhmlet(newOhmletReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                streamReference,
                updatedUser);

        UserServlet
            .ignoreOhmletStream(token, USER_ID, ohmletId, streamReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                streamReference,
                updatedUser);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test(expected = AuthenticationException.class)
    public void testStopIgnoringOhmletStreamTokenNull() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .stopIgnoringOhmletStream(
                null,
                USER_ID,
                ohmletId,
                streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletStreamUsernameNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .stopIgnoringOhmletStream(token, null, ohmletId, streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testStopIgnoringOhmletStreamUsernameDifferent() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .stopIgnoringOhmletStream(
                token,
                "Unknown",
                ohmletId,
                streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletStreamOhmletIdNull() {
        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .stopIgnoringOhmletStream(token, USER_ID, null, streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletStreamOhmletIdUnknown() {
        String unknownOhmletId = "Unknown";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.expect(user.getOhmlet(unknownOhmletId)).andReturn(null);

        EasyMock.replay(user, userBin, token, streamReference);

        UserServlet
            .stopIgnoringOhmletStream(
                token,
                USER_ID,
                unknownOhmletId,
                streamReference);

        EasyMock.verify(user, userBin, token, streamReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletStreamStreamReferenceNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        EasyMock.replay(user, userBin, token);

        UserServlet.stopIgnoringOhmletStream(token, USER_ID, ohmletId, null);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Tests stopping ignoring an ohmlet's stream.
     */
    @Test
    public void testStopIgnoringOhmletStream() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        OhmletReference originalOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        OhmletReference newOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        EasyMock
            .expect(user.getOhmlet(ohmletId))
            .andReturn(originalOhmletReference);
        EasyMock
            .expect(
                originalOhmletReference.stopIgnoringStream(streamReference))
            .andReturn(newOhmletReference);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.upsertOhmlet(newOhmletReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                streamReference,
                updatedUser);

        UserServlet
            .stopIgnoringOhmletStream(
                token,
                USER_ID,
                ohmletId,
                streamReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                streamReference,
                updatedUser);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test(expected = AuthenticationException.class)
    public void testIgnoreOhmletSurveyTokenNull() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .ignoreOhmletSurvey(null, USER_ID, ohmletId, surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletSurveyUsernameNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .ignoreOhmletSurvey(token, null, ohmletId, surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testIgnoreOhmletSurveyUsernameDifferent() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .ignoreOhmletSurvey(token, "Unknown", ohmletId, surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletSurveyOhmletIdNull() {
        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet.ignoreOhmletSurvey(token, USER_ID, null, surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletSurveyOhmletIdUnknown() {
        String unknownOhmletId = "Unknown";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.expect(user.getOhmlet(unknownOhmletId)).andReturn(null);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .ignoreOhmletSurvey(
                token,
                USER_ID,
                unknownOhmletId,
                surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testIgnoreOhmletSurveySurveyReferenceNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        EasyMock.replay(user, userBin, token);

        UserServlet.ignoreOhmletSurvey(token, USER_ID, ohmletId, null);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Tests ignoring a specific survey in an ohmlet.
     */
    @Test
    public void testIgnoreOhmletSurvey() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        OhmletReference originalOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        OhmletReference newOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        EasyMock
            .expect(user.getOhmlet(ohmletId))
            .andReturn(originalOhmletReference);
        EasyMock
            .expect(originalOhmletReference.ignoreSurvey(surveyReference))
            .andReturn(newOhmletReference);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.upsertOhmlet(newOhmletReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                surveyReference,
                updatedUser);

        UserServlet
            .ignoreOhmletSurvey(token, USER_ID, ohmletId, surveyReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                surveyReference,
                updatedUser);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test(expected = AuthenticationException.class)
    public void testStopIgnoringOhmletSurveyTokenNull() {
        String ohmletId = "Ohmlet.ID";

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .stopIgnoringOhmletSurvey(
                null,
                USER_ID,
                ohmletId,
                surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletSurveyUsernameNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .stopIgnoringOhmletSurvey(token, null, ohmletId, surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testStopIgnoringOhmletSurveyUsernameDifferent() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .stopIgnoringOhmletSurvey(
                token,
                "Unknown",
                ohmletId,
                surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletSurveyOhmletIdNull() {
        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .stopIgnoringOhmletSurvey(token, USER_ID, null, surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletSurveyOhmletIdUnknown() {
        String unknownOhmletId = "Unknown";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.expect(user.getOhmlet(unknownOhmletId)).andReturn(null);

        EasyMock.replay(user, userBin, token, surveyReference);

        UserServlet
            .stopIgnoringOhmletSurvey(
                token,
                USER_ID,
                unknownOhmletId,
                surveyReference);

        EasyMock.verify(user, userBin, token, surveyReference);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testStopIgnoringOhmletSurveySurveyReferenceNull() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        EasyMock.replay(user, userBin, token);

        UserServlet.stopIgnoringOhmletSurvey(token, USER_ID, ohmletId, null);

        EasyMock.verify(user, userBin, token);
    }

    /**
     * Tests stopping ignoring an ohmlet's survey.
     */
    @Test
    public void testStopIgnoringOhmletSurvey() {
        String ohmletId = "Ohmlet.ID";

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        OhmletReference originalOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        OhmletReference newOhmletReference =
            EasyMock.createMock(OhmletReference.class);
        EasyMock
            .expect(user.getOhmlet(ohmletId))
            .andReturn(originalOhmletReference);
        EasyMock
            .expect(
                originalOhmletReference.stopIgnoringSurvey(surveyReference))
            .andReturn(newOhmletReference);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.upsertOhmlet(newOhmletReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                surveyReference,
                updatedUser);

        UserServlet
            .stopIgnoringOhmletSurvey(
                token,
                USER_ID,
                ohmletId,
                surveyReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                originalOhmletReference,
                newOhmletReference,
                surveyReference,
                updatedUser);
    }

    /**
     * Tests following a stream using the ID and no version.
     */
    @Test(expected = AuthenticationException.class)
    public void testFollowStreamTokenNull() {
        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, streamReference);

        UserServlet.followStream(null, USER_ID, streamReference);

        EasyMock.verify(user, userBin, streamReference);
    }

    /**
     * Tests following a stream using the ID and no version.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testFollowStreamUsernamesNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, token, streamReference);

        UserServlet.followStream(token, null, streamReference);

        EasyMock.verify(user, token, streamReference);
    }

    /**
     * Tests following a stream using the ID and no version.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testFollowStreamUsernameUnknown() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, token, streamReference);

        UserServlet.followStream(token, "Unknown", streamReference);

        EasyMock.verify(user, token, streamReference);
    }

    /**
     * Tests following a stream using the ID and no version.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testFollowStreamReferenceNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.followStream(token, USER_ID, null);

        EasyMock.verify(token);
    }

    /**
     * Tests following a stream using the ID and no version.
     */
    @Test
    public void testFollowStreamNoVersion() {
        String streamId = "Stream.ID";

        PowerMock.mockStatic(StreamBin.class);
        StreamBin streamBin = EasyMock.createMock(StreamBin.class);
        EasyMock
            .expect(StreamBin.getInstance())
            .andReturn(streamBin)
            .anyTimes();
        PowerMock.replay(StreamBin.class);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);
        EasyMock.expect(streamReference.getSchemaId()).andReturn(streamId);
        EasyMock.expect(streamReference.getVersion()).andReturn(null);

        Stream stream = EasyMock.createMock(Stream.class);
        EasyMock.expect(streamBin.getLatestStream(streamId)).andReturn(stream);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.followStream(streamReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                streamBin,
                streamReference,
                stream,
                updatedUser);

        UserServlet.followStream(token, USER_ID, streamReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                streamBin,
                streamReference,
                stream,
                updatedUser);
    }

    /**
     * Tests following a stream using the ID and version.
     */
    @Test
    public void testFollowStreamWithVersion() {
        String streamId = "Stream.ID";
        Long streamVersion = 1L;

        PowerMock.mockStatic(StreamBin.class);
        StreamBin streamBin = EasyMock.createMock(StreamBin.class);
        EasyMock
            .expect(StreamBin.getInstance())
            .andReturn(streamBin)
            .anyTimes();
        PowerMock.replay(StreamBin.class);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);
        EasyMock.expect(streamReference.getSchemaId()).andReturn(streamId);
        EasyMock
            .expect(streamReference.getVersion())
            .andReturn(streamVersion)
            .times(2);

        Stream stream = EasyMock.createMock(Stream.class);
        EasyMock
            .expect(streamBin.getStream(streamId, streamVersion))
            .andReturn(stream);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.followStream(streamReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                streamBin,
                streamReference,
                stream,
                updatedUser);

        UserServlet.followStream(token, USER_ID, streamReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                streamBin,
                streamReference,
                stream,
                updatedUser);
    }

    /**
     * Tests that the streams a user is following are returned.
     */
    @Test(expected = AuthenticationException.class)
    public void testGetFollowedStreamsTokenNull() {
        EasyMock.replay(user);

        UserServlet.getFollowedStreams(null, USER_ID);
    }

    /**
     * Tests that the streams a user is following are returned.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testGetFollowedStreamsUsernameNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, token);

        UserServlet.getFollowedStreams(token, null);
    }

    /**
     * Tests that the streams a user is following are returned.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testGetFollowedStreamsUsernameUnknown() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, token);

        UserServlet.getFollowedStreams(token, "Unknown");
    }

    /**
     * Tests that the streams a user is following are returned.
     */
    @Test
    public void testGetFollowedStreams() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        Set<SchemaReference> expected = new HashSet<SchemaReference>();
        expected.add(EasyMock.createMock(SchemaReference.class));
        EasyMock.expect(user.getStreams()).andReturn(expected);

        EasyMock.replay(user, token);

        Set<SchemaReference> actual =
            UserServlet.getFollowedStreams(token, USER_ID);

        EasyMock.verify(user, token);

        Assert
            .assertEquals(
                "The given stream reference and the returned stream " +
                    "references were not the same.",
                expected,
                actual);
    }

    /**
     * Tests when a user decides to no longer follow a stream.
     */
    @Test
    public void testForgetStream() {
        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference streamReference =
            EasyMock.createMock(SchemaReference.class);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.ignoreStream(streamReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                streamReference,
                updatedUser);

        UserServlet.forgetStream(token, USER_ID, streamReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                streamReference,
                updatedUser);
    }

    /**
     * Tests following a survey using the ID and no version.
     */
    @Test(expected = AuthenticationException.class)
    public void testFollowSurveyTokenNull() {
        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, userBin, surveyReference);

        UserServlet.followSurvey(null, USER_ID, surveyReference);

        EasyMock.verify(user, userBin, surveyReference);
    }

    /**
     * Tests following a survey using the ID and no version.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testFollowSurveyUsernamesNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, token, surveyReference);

        UserServlet.followSurvey(token, null, surveyReference);

        EasyMock.verify(user, token, surveyReference);
    }

    /**
     * Tests following a survey using the ID and no version.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testFollowSurveyUsernameUnknown() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        EasyMock.replay(user, token, surveyReference);

        UserServlet.followSurvey(token, "Unknown", surveyReference);

        EasyMock.verify(user, token, surveyReference);
    }

    /**
     * Tests following a survey using the ID and no version.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testFollowSurveyReferenceNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);

        EasyMock.replay(token);

        UserServlet.followSurvey(token, USER_ID, null);

        EasyMock.verify(token);
    }

    /**
     * Tests following a survey using the ID and no version.
     */
    @Test
    public void testFollowSurveyNoVersion() {
        String surveyId = "Survey.ID";

        PowerMock.mockStatic(SurveyBin.class);
        SurveyBin surveyBin = EasyMock.createMock(SurveyBin.class);
        EasyMock
            .expect(SurveyBin.getInstance())
            .andReturn(surveyBin)
            .anyTimes();
        PowerMock.replay(SurveyBin.class);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);
        EasyMock.expect(surveyReference.getSchemaId()).andReturn(surveyId);
        EasyMock.expect(surveyReference.getVersion()).andReturn(null);

        Survey survey = EasyMock.createMock(Survey.class);
        EasyMock.expect(surveyBin.getLatestSurvey(surveyId)).andReturn(survey);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.followSurvey(surveyReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                surveyBin,
                surveyReference,
                survey,
                updatedUser);

        UserServlet.followSurvey(token, USER_ID, surveyReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                surveyBin,
                surveyReference,
                survey,
                updatedUser);
    }

    /**
     * Tests following a survey using the ID and version.
     */
    @Test
    public void testFollowSurveyWithVersion() {
        String surveyId = "Survey.ID";
        Long surveyVersion = 1L;

        PowerMock.mockStatic(SurveyBin.class);
        SurveyBin surveyBin = EasyMock.createMock(SurveyBin.class);
        EasyMock
            .expect(SurveyBin.getInstance())
            .andReturn(surveyBin)
            .anyTimes();
        PowerMock.replay(SurveyBin.class);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);
        EasyMock.expect(surveyReference.getSchemaId()).andReturn(surveyId);
        EasyMock
            .expect(surveyReference.getVersion())
            .andReturn(surveyVersion)
            .times(2);

        Survey survey = EasyMock.createMock(Survey.class);
        EasyMock
            .expect(surveyBin.getSurvey(surveyId, surveyVersion))
            .andReturn(survey);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.followSurvey(surveyReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                surveyBin,
                surveyReference,
                survey,
                updatedUser);

        UserServlet.followSurvey(token, USER_ID, surveyReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                surveyBin,
                surveyReference,
                survey,
                updatedUser);
    }

    /**
     * Tests that the surveys a user is following are returned.
     */
    @Test(expected = AuthenticationException.class)
    public void testGetFollowedSurveysTokenNull() {
        EasyMock.replay(user);

        UserServlet.getFollowedSurveys(null, USER_ID);
    }

    /**
     * Tests that the surveys a user is following are returned.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testGetFollowedSurveysUsernameNull() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, token);

        UserServlet.getFollowedSurveys(token, null);
    }

    /**
     * Tests that the surveys a user is following are returned.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testGetFollowedSurveysUsernameUnknown() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        EasyMock.replay(user, token);

        UserServlet.getFollowedSurveys(token, "Unknown");
    }

    /**
     * Tests that the surveys a user is following are returned.
     */
    @Test
    public void testGetFollowedSurveys() {
        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);
        EasyMock.expect(user.getId()).andReturn(USER_ID);

        Set<SchemaReference> expected = new HashSet<SchemaReference>();
        expected.add(EasyMock.createMock(SchemaReference.class));
        EasyMock.expect(user.getSurveys()).andReturn(expected);

        EasyMock.replay(user, token);

        Set<SchemaReference> actual =
            UserServlet.getFollowedSurveys(token, USER_ID);

        EasyMock.verify(user, token);

        Assert
            .assertEquals(
                "The given survey reference and the returned survey " +
                    "references were not the same.",
                expected,
                actual);
    }

    /**
     * Tests when a user decides to no longer follow a survey.
     */
    @Test
    public void testForgetSurvey() {
        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        AuthorizationToken token =
            EasyMock.createMock(AuthorizationToken.class);
        EasyMock.expect(token.getUser()).andReturn(user);

        SchemaReference surveyReference =
            EasyMock.createMock(SchemaReference.class);

        User updatedUser = EasyMock.createMock(User.class);
        EasyMock
            .expect(user.ignoreSurvey(surveyReference))
            .andReturn(updatedUser);

        userBin.updateUser(updatedUser);
        EasyMock.expectLastCall();

        EasyMock
            .replay(
                user,
                userBin,
                token,
                surveyReference,
                updatedUser);

        UserServlet.forgetSurvey(token, USER_ID, surveyReference);

        EasyMock
            .verify(
                user,
                userBin,
                token,
                surveyReference,
                updatedUser);
    }

    /**
     * Tests when a user is deleting their own account using a password.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithPasswordUsernameNull() {
        EasyMock.replay(user, userBin);

        UserServlet.deleteUserWithPassword(null, PASSWORD);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using a password.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithPasswordUsernameUnknown() {
        EasyMock.expect(userBin.getUser(USER_ID)).andReturn(null);

        EasyMock.replay(user, userBin);

        UserServlet.deleteUserWithPassword(USER_ID, PASSWORD);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using a password.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithPasswordPasswordNull() {
        EasyMock.replay(user, userBin);

        UserServlet.deleteUserWithPassword(USER_ID, null);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using a password.
     */
    @Test(expected = AuthenticationException.class)
    public void testDeleteUserWithPasswordPasswordWrong() {
        EasyMock.expect(userBin.getUser(USER_ID)).andReturn(user);
        EasyMock.expect(user.verifyPassword(PASSWORD)).andReturn(false);

        EasyMock.replay(user, userBin);

        UserServlet.deleteUserWithPassword(USER_ID, PASSWORD);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using a password.
     */
    @Test
    public void testDeleteUserWithPassword() {
        EasyMock.expect(userBin.getUser(USER_ID)).andReturn(user);
        EasyMock.expect(user.verifyPassword(PASSWORD)).andReturn(true);
        userBin.disableUser(USER_ID);
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBin);

        UserServlet.deleteUserWithPassword(USER_ID, PASSWORD);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithProviderUsernameNull() {
        PowerMock.mockStatic(ProviderRegistry.class);
        Provider provider = EasyMock.createMock(Provider.class);
        EasyMock.expect(ProviderRegistry.get(PROVIDER_ID)).andReturn(provider);
        PowerMock.replay(ProviderRegistry.class);

        ProviderUserInformation information =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(provider.getUserInformation(PROVIDER_ACCESS_TOKEN))
            .andReturn(information);

        EasyMock.expect(information.getProviderId()).andReturn(PROVIDER_ID);
        EasyMock.expect(information.getUserId()).andReturn(USER_ID);

        EasyMock
            .expect(userBin.getUserFromProvider(PROVIDER_ID, USER_ID))
            .andReturn(user);

        EasyMock.replay(user, userBin, provider, information);

        UserServlet
            .deleteUserWithProvider(
                null,
                PROVIDER_ID,
                PROVIDER_ACCESS_TOKEN);

        EasyMock.verify(user, userBin, provider, information);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test(expected = InsufficientPermissionsException.class)
    public void testDeleteUserWithProviderUsernameUnknown() {
        PowerMock.mockStatic(ProviderRegistry.class);
        Provider provider = EasyMock.createMock(Provider.class);
        EasyMock.expect(ProviderRegistry.get(PROVIDER_ID)).andReturn(provider);
        PowerMock.replay(ProviderRegistry.class);

        ProviderUserInformation information =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(provider.getUserInformation(PROVIDER_ACCESS_TOKEN))
            .andReturn(information);

        EasyMock.expect(information.getProviderId()).andReturn(PROVIDER_ID);
        EasyMock.expect(information.getUserId()).andReturn(USER_ID);

        EasyMock
            .expect(userBin.getUserFromProvider(PROVIDER_ID, USER_ID))
            .andReturn(user);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        EasyMock.replay(user, userBin, provider, information);

        UserServlet
            .deleteUserWithProvider(
                "Unknown",
                PROVIDER_ID,
                PROVIDER_ACCESS_TOKEN);

        EasyMock.verify(user, userBin, provider, information);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithProviderProviderIdNull() {
        EasyMock.replay(user, userBin);

        UserServlet
            .deleteUserWithProvider(
                USER_ID,
                null,
                PROVIDER_ACCESS_TOKEN);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test(expected = UnknownProviderException.class)
    public void testDeleteUserWithProviderIdUnknown() {
        PowerMock.mockStatic(ProviderRegistry.class);
        EasyMock
            .expect(ProviderRegistry.get(PROVIDER_ID))
            .andThrow(
                new UnknownProviderException("The provider is unknown."));
        PowerMock.replay(ProviderRegistry.class);

        EasyMock.replay(user, userBin);

        UserServlet
            .deleteUserWithProvider(
                USER_ID,
                PROVIDER_ID,
                PROVIDER_ACCESS_TOKEN);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithProviderAccessTokenNull() {
        EasyMock.replay(user, userBin);

        UserServlet
            .deleteUserWithProvider(
                USER_ID,
                PROVIDER_ID,
                null);

        EasyMock.verify(user, userBin);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testDeleteUserWithProviderAccessTokenInvalid() {
        PowerMock.mockStatic(ProviderRegistry.class);
        Provider provider = EasyMock.createMock(Provider.class);
        EasyMock.expect(ProviderRegistry.get(PROVIDER_ID)).andReturn(provider);
        PowerMock.replay(ProviderRegistry.class);

        EasyMock
            .expect(provider.getUserInformation(PROVIDER_ACCESS_TOKEN))
            .andThrow(
                new InvalidArgumentException("The access token is invalid."));

        EasyMock.replay(user, userBin, provider);

        UserServlet
            .deleteUserWithProvider(
                USER_ID,
                PROVIDER_ID,
                PROVIDER_ACCESS_TOKEN);

        EasyMock.verify(user, userBin, provider);
    }

    /**
     * Tests when a user is deleting their own account using their
     * provider-based credentials.
     */
    @Test
    public void testDeleteUserWithProvider() {
        PowerMock.mockStatic(ProviderRegistry.class);
        Provider provider = EasyMock.createMock(Provider.class);
        EasyMock.expect(ProviderRegistry.get(PROVIDER_ID)).andReturn(provider);
        PowerMock.replay(ProviderRegistry.class);

        ProviderUserInformation information =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(provider.getUserInformation(PROVIDER_ACCESS_TOKEN))
            .andReturn(information);

        EasyMock.expect(information.getProviderId()).andReturn(PROVIDER_ID);
        EasyMock.expect(information.getUserId()).andReturn(USER_ID);

        EasyMock
            .expect(userBin.getUserFromProvider(PROVIDER_ID, USER_ID))
            .andReturn(user);

        EasyMock.expect(user.getId()).andReturn(USER_ID).anyTimes();

        userBin.disableUser(USER_ID);
        EasyMock.expectLastCall();

        EasyMock.replay(user, userBin, provider, information);

        UserServlet
            .deleteUserWithProvider(
                USER_ID,
                PROVIDER_ID,
                PROVIDER_ACCESS_TOKEN);

        EasyMock.verify(user, userBin, provider, information);
    }
}