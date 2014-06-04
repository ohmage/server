package org.ohmage.controller;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.UserBin;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.domain.user.Registration;
import org.ohmage.domain.user.User;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * <p>
 * The controller for requests to activate a user.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(UserActivationController.ROOT_MAPPING)
public class UserActivationController extends OhmageController {
    /**
     * The root API mapping for this Servlet.
     */
    public static final String ROOT_MAPPING = "/activation";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(UserActivationController.class.getName());

    /**
     * Activates a user's account.
     *
     * @param activationId
     *        The activation ID given to the user when they self-registered.
     */
    @RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
    public static @ResponseBody void activateOhmageUser(
        @RequestParam(
            value = Registration.JSON_KEY_ACTIVATION_ID,
            required = true)
            final String activationId) {

        LOGGER.log(Level.INFO, "Activating a new user.");

        LOGGER
            .log(
                Level.INFO,
                "Retrieving the user based on the activation ID.");
        User user =
            UserBin.getInstance().getUserFromActivationId(activationId);

        LOGGER.log(Level.INFO, "Verifying that the user exists.");
        if(user == null) {
            throw
                new UnknownEntityException("The activation ID is unknown.");
        }

        LOGGER.log(Level.INFO, "Activating the user's account.");
        user = user.activate();

        LOGGER.log(Level.INFO, "Storing the updated user.");
        UserBin.getInstance().updateUser(user);
    }
}