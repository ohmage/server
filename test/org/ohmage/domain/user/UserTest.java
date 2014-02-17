package org.ohmage.domain.user;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;

import org.easymock.EasyMock;
import org.easymock.EasyMockRunner;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.ohmlet.Ohmlet.SchemaReference;
import org.ohmage.domain.ohmlet.OhmletReference;
import org.ohmage.servlet.listener.ConfigurationFileImport;

/**
 * <p>
 * Test everything about the {@link User} class.
 * </p>
 *
 * @author John Jenkins
 */
@RunWith(EasyMockRunner.class)
public class UserTest {
    /**
     * A valid username to use for testing.
     */
    public static final String USERNAME = "Valid.username";

    /**
     * A valid password to use for testing.
     */
    public static final String PASSWORD = "Valid.password";

    /**
     * A valid email address to use for testing.
     */
    public static final String EMAIL = "Valid.email@example.com";

    /**
     * A valid full name to use for testing.
     */
    public static final String FULL_NAME = "Valid Full Name";

    /**
     * A valid invitation ID.
     */
    public static final String INVITATION_ID = "Invitation.ID";

    /**
     * A valid internal version to use for testing.
     */
    public static final Long INTERNAL_VERSION = 1L;

    /**
     * A {@link Registration} object that is setup and torn down between each
     * test.
     */
    private Registration registration;

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
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/")).andReturn("web");
        EasyMock.replay(context);

        ServletContextEvent event =
            EasyMock.createMock(ServletContextEvent.class);
        EasyMock
            .expect(event.getServletContext())
            .andReturn(context)
            .anyTimes();
        EasyMock.replay(event);

        config = new ConfigurationFileImport();
        config.contextInitialized(event);
    }

    /**
     * Runs after all of the tests have finished.
     */
    @AfterClass
    public static void destroy() {
        ServletContext context = EasyMock.createMock(ServletContext.class);
        EasyMock.expect(context.getRealPath("/")).andReturn("web");
        EasyMock.replay(context);

        ServletContextEvent event =
            EasyMock.createMock(ServletContextEvent.class);
        EasyMock
            .expect(event.getServletContext())
            .andReturn(context)
            .anyTimes();
        EasyMock.replay(event);

        config.contextDestroyed(event);
        config = null;
    }

    /**
     * Initializes the registration and its builder.
     */
    @Before
    public void setup() {
        registration = new Registration.Builder(USERNAME, EMAIL).build();
    }

    /**
     * Tears down and registration and its builder.
     */
    @After
    public void tearDown() {
        registration = null;
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUserNewPasswordNull() {
        new User(
            null,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUserNewEmailNull() {
        new User(
            PASSWORD,
            null,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewFullNameNull() {
        new User(
            PASSWORD,
            EMAIL,
            null,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewProvidersNull() {
        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            null,
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewProviders() {
        ProviderUserInformation provider =
            EasyMock.createMock(ProviderUserInformation.class);
        List<ProviderUserInformation> providers =
            new ArrayList<ProviderUserInformation>(1);
        providers.add(provider);

        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            providers,
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewOhmletsNull() {
        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            null,
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewOhmlets() {
        OhmletReference ohmlet = EasyMock.createMock(OhmletReference.class);
        Set<OhmletReference> ohmlets = new HashSet<OhmletReference>();
        ohmlets.add(ohmlet);

        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            ohmlets,
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewStreamsNull() {
        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            null,
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewStreams() {
        SchemaReference stream = EasyMock.createMock(SchemaReference.class);
        Set<SchemaReference> streams = new HashSet<SchemaReference>();
        streams.add(stream);

        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            streams,
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewSurveysNull() {
        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            null,
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewSurveys() {
        SchemaReference survey = EasyMock.createMock(SchemaReference.class);
        Set<SchemaReference> surveys = new HashSet<SchemaReference>();
        surveys.add(survey);

        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            surveys,
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNewRegistrationNull() {
        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            null,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserNew() {
        new User(
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUserRecreateIdNull() {
        new User(
            null,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUserRecreatePasswordNull() {
        new User(
            USERNAME,
            null,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test(expected = InvalidArgumentException.class)
    public void testUserRecreateEmailNull() {
        new User(
            USERNAME,
            PASSWORD,
            null,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateFullNameNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            null,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateProvidersNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            null,
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateProviders() {
        ProviderUserInformation provider =
            EasyMock.createMock(ProviderUserInformation.class);
        List<ProviderUserInformation> providers =
            new ArrayList<ProviderUserInformation>(1);
        providers.add(provider);

        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            providers,
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateOhmletsNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            null,
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateOhmlets() {
        OhmletReference ohmlet = EasyMock.createMock(OhmletReference.class);
        Set<OhmletReference> ohmlets = new HashSet<OhmletReference>();
        ohmlets.add(ohmlet);

        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            ohmlets,
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateStreamsNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            null,
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateStreams() {
        SchemaReference stream = EasyMock.createMock(SchemaReference.class);
        Set<SchemaReference> streams = new HashSet<SchemaReference>();
        streams.add(stream);

        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            streams,
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateSurveysNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            null,
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateSurveys() {
        SchemaReference survey = EasyMock.createMock(SchemaReference.class);
        Set<SchemaReference> surveys = new HashSet<SchemaReference>();
        surveys.add(survey);

        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            surveys,
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Tests that a brand-new user may be built.
     */
    @Test
    public void testUserRecreateRegistrationNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            null,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Test that a user can be recreated.
     */
    @Test
    public void testUserRecreateInternalVersionNull() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            null);
    }

    /**
     * Test that a user can be recreated.
     */
    @Test
    public void testUserRecreate() {
        new User(
            USERNAME,
            PASSWORD,
            EMAIL,
            FULL_NAME,
            Collections.<ProviderUserInformation>emptyList(),
            Collections.<OhmletReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            Collections.<SchemaReference>emptySet(),
            registration,
            INVITATION_ID,
            INTERNAL_VERSION);
    }

    /**
     * Test that the password is the same as the one with which it was created.
     */
    @Test
    public void testGetPassword() {
        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);
        Assert
            .assertEquals(
                "The returned password did not match the built password.",
                PASSWORD,
                user.getPassword());
    }

    /**
     * Test that the email is the same as the one with which it was created.
     */
    @Test
    public void testGetEmail() {
        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);
        Assert
            .assertEquals(
                "The returned email did not match the built email.",
                EMAIL,
                user.getEmail());
    }

    /**
     * Test that an unknown provider returns null.
     */
    @Test
    public void testGetProviderMissing() {
        String providerId = "Provider.ID";
        ProviderUserInformation provider =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(provider.getProviderId())
            .andReturn(providerId)
            .anyTimes();
        EasyMock.replay(provider);
        List<ProviderUserInformation> providers =
            new ArrayList<ProviderUserInformation>(1);
        providers.add(provider);

        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                providers,
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);
        Assert
            .assertNull(
                "A non-existant provider was associated with a user.",
                user.getProvider("Does not exist"));
    }

    /**
     * Test that a known provider returns a valid provider.
     */
    @Test
    public void testGetProvider() {
        String providerId = "Provider.ID";
        ProviderUserInformation provider =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(provider.getProviderId())
            .andReturn(providerId)
            .anyTimes();
        EasyMock.replay(provider);
        List<ProviderUserInformation> providers =
            new ArrayList<ProviderUserInformation>(1);
        providers.add(provider);

        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                providers,
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);
        Assert.assertEquals(provider, user.getProvider(providerId));
    }

    /**
     * Tests updating a provider, specifically their email address.
     */
    @Test
    public void testUpdateProviderEmail() {
        // Set the provider's ID.
        String providerId = "Provider.ID";

        // Build the original provider with an original email address.
        ProviderUserInformation originalProvider =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(originalProvider.getProviderId())
            .andReturn(providerId)
            .anyTimes();
        EasyMock
            .expect(originalProvider.getEmail())
            .andReturn("original@example.com")
            .anyTimes();
        EasyMock.replay(originalProvider);

        // Add the original provider to a list to use to create the user.
        List<ProviderUserInformation> providers =
            new ArrayList<ProviderUserInformation>(1);
        providers.add(originalProvider);

        // Create the original user with the original provider.
        User originalUser =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                providers,
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        // Update the provider.
        ProviderUserInformation newProvider =
            EasyMock.createMock(ProviderUserInformation.class);
        EasyMock
            .expect(newProvider.getProviderId())
            .andReturn(providerId)
            .anyTimes();
        EasyMock
            .expect(newProvider.getEmail())
            .andReturn("new@example.com")
            .anyTimes();
        EasyMock.replay(newProvider);

        // Update the user with the new provider.
        User newUser = originalUser.updateProvider(newProvider);

        // Test.
        Assert
            .assertNotEquals(
                "The original user and the new user are the same user after " +
                    "updating the user's provider.",
                originalUser,
                newUser);
        Assert
            .assertNotNull(
                "The new user does not have a provider.",
                newUser.getProvider(providerId));
        Assert
            .assertNotEquals(
                "The original user's provider and the new user's provider " +
                    "are equal.",
                originalUser.getProvider(providerId),
                newUser.getProvider(providerId));
        Assert
            .assertNotEquals(
                "The original user's provider's email address and the new " +
                    "user's provider's email address are equal.",
                originalUser.getProvider(providerId).getEmail(),
                newUser.getProvider(providerId).getEmail());

        Assert
            .assertEquals(
                "The new user's read version has changed.",
                originalUser.getInternalReadVersion(),
                newUser.getInternalReadVersion());
        Assert
            .assertTrue(
                "The new user's write version is not greater than the " +
                    "original user's write version.",
                originalUser.getInternalWriteVersion() <
                    newUser.getInternalWriteVersion());
    }

    /**
     * Test that the ohmlets that are used to build the user are the same ones
     * that are returned.
     */
    @Test
    public void testGetOhmlets() {
        String ohmletId = "Ohmlet.ID";
        OhmletReference ohmlet = EasyMock.createMock(OhmletReference.class);
        EasyMock.expect(ohmlet.getOhmletId()).andReturn(ohmletId).anyTimes();
        EasyMock.replay(ohmlet);
        Set<OhmletReference> ohmlets = new HashSet<OhmletReference>();
        ohmlets.add(ohmlet);

        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                ohmlets,
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);
        Collection<OhmletReference> ohmletReferences = user.getOhmlets();

        Assert
            .assertEquals(
                "The incorrect number of ohmlet references were returned.",
                1,
                ohmletReferences.size());
        Assert
            .assertEquals(
                "The ohmlet reference is not the same as the one that was " +
                    "returned.",
                ohmlet,
                ohmletReferences.iterator().next());
    }

    /**
     * Test that the ohmlet that is used to build the user is the same one that
     * is returned.
     */
    @Test
    public void testGetOhmlet() {
        String ohmletId = "Ohmlet.ID";
        OhmletReference ohmlet = EasyMock.createMock(OhmletReference.class);
        EasyMock.expect(ohmlet.getOhmletId()).andReturn(ohmletId).anyTimes();
        EasyMock.replay(ohmlet);
        Set<OhmletReference> ohmlets = new HashSet<OhmletReference>();
        ohmlets.add(ohmlet);

        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                ohmlets,
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);
        OhmletReference retrievedOhmlet = user.getOhmlet(ohmletId);

        Assert
            .assertEquals(
                "The ohmlet used to create the object was not the same one " +
                    "that was returned.",
                    ohmlet,
                    retrievedOhmlet);
    }

    /**
     * Test that the streams that are used to build the user are the same ones
     * that are returned.
     */
    @Test
    public void testGetStreams() {
        SchemaReference stream = EasyMock.createMock(SchemaReference.class);
        Set<SchemaReference> streams = new HashSet<SchemaReference>();
        streams.add(stream);

        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                streams,
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        Set<SchemaReference> retrievedStreams = user.getStreams();

        Assert
            .assertEquals(
                "The incorrect number of stream references were returned.",
                1,
                retrievedStreams.size());
        Assert
            .assertEquals(
                "The stream reference is not the same as the one that was " +
                    "returned.",
                stream,
                retrievedStreams.iterator().next());
    }

    /**
     * Test that the surveys that are used to build the user are the same ones
     * that are returned.
     */
    @Test
    public void testGetSurveys() {
        SchemaReference survey = EasyMock.createMock(SchemaReference.class);
        Set<SchemaReference> surveys = new HashSet<SchemaReference>();
        surveys.add(survey);

        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                surveys,
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        Set<SchemaReference> retrievedSurveys = user.getStreams();

        Assert
            .assertEquals(
                "The incorrect number of survey references were returned.",
                1,
                retrievedSurveys.size());
        Assert
            .assertEquals(
                "The survey reference is not the same as the one that was " +
                    "returned.",
                survey,
                retrievedSurveys.iterator().next());
    }

    /**
     * Test that the registration that is used to build the user is the same
     * one that is returned.
     */
    @Test
    public void testGetRegistration() {
        User user =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        Assert
            .assertEquals(
                "The registration used to build the object is not the same " +
                    "as the registraion returned.",
                registration,
                user.getRegistration());
    }

    /**
     * Test that a user can properly be activated.
     */
    @Test
    public void testActivateRegistered() {
        User originalUser =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        User newUser = originalUser.activate();

        // Test.
        Assert
            .assertNotEquals(
                "The original user and the new user are the same user after " +
                    "updating the user's provider.",
                originalUser,
                newUser);
        Assert
            .assertNotNull(
                "The new user does not have a registration.",
                originalUser.getRegistration());
        Assert
            .assertNotEquals(
                "The original user's registration and the new user's " +
                    "registration are equal.",
                originalUser.getRegistration(),
                newUser.getRegistration());
        Assert
            .assertNotNull(
                "The new user does not have an activation timestamp.",
                newUser.getRegistration().getActivationTimestamp());

        Assert
            .assertEquals(
                "The new user's read version has changed.",
                originalUser.getInternalReadVersion(),
                newUser.getInternalReadVersion());
        Assert
            .assertTrue(
                "The new user's write version is not greater than the " +
                    "original user's write version.",
                originalUser.getInternalWriteVersion() <
                    newUser.getInternalWriteVersion());
    }

    /**
     * Test that a password can be properly validated.
     */
    @Test
    public void testVerifyPassword() {
        String hashedPassword = User.hashPassword(PASSWORD);
        User user =
            new User(
                hashedPassword,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        Assert
            .assertTrue(
                "The password wasn't properly validated.",
                user.verifyPassword(PASSWORD));
    }

    /**
     * Test updating a user's password
     */
    @Test
    public void testUpdatePassword() {
        // Set the new password
        String newPassword = PASSWORD + "a";

        // Create the original user with the original provider.
        User originalUser =
            new User(
                PASSWORD,
                EMAIL,
                FULL_NAME,
                Collections.<ProviderUserInformation>emptyList(),
                Collections.<OhmletReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                Collections.<SchemaReference>emptySet(),
                registration,
                INVITATION_ID);

        // Update the user with the new provider.
        User newUser = originalUser.updatePassword(newPassword);

        // Test.
        Assert
            .assertNotEquals(
                "The original user and the new user are the same user after " +
                    "updating the user's provider.",
                originalUser,
                newUser);
        Assert
            .assertNotNull(
                "The new user does not have a password.",
                newUser.getPassword());
        Assert
            .assertEquals(
                "The user's password did not change.",
                newPassword,
                newUser.getPassword());

        Assert
            .assertEquals(
                "The new user's read version has changed.",
                originalUser.getInternalReadVersion(),
                newUser.getInternalReadVersion());
        Assert
            .assertTrue(
                "The new user's write version is not greater than the " +
                    "original user's write version.",
                originalUser.getInternalWriteVersion() <
                    newUser.getInternalWriteVersion());
    }
}