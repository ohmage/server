package org.ohmage.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        LoggerFactory.getLogger(UserActivationController.class.getName());

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

        LOGGER.info("Activating a new user.");

        LOGGER.info("Retrieving the user based on the activation ID.");
        User user =
            UserBin.getInstance().getUserFromActivationId(activationId);

        LOGGER.info("Verifying that the user exists.");
        if(user == null) {
            throw
                new UnknownEntityException("The activation ID is unknown.");
        }

        LOGGER.info("Activating the user's account.");
        user = user.activate();

        LOGGER.info("Storing the updated user.");
        UserBin.getInstance().updateUser(user);
    }
}
