package org.ohmage.validator;

import java.net.MalformedURLException;
import java.net.URL;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.CookieUtils;
import org.ohmage.util.StringUtils;

public class ServerValidators {
	/**
	 * Default constructor. Private so that it cannot be instantiated.
	 */
	private ServerValidators() {}
	
	/**
	 * Validates that a path is a valid path and returns a URL with this
	 * server's FQDN and this path.
	 * 
	 * @param value The path to validate.
	 * 
	 * @return A URL with this FQDN and the given path.
	 */
	public static URL validateRedirect(
		final String value)
		throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(value)) {
			return null;
		}
		
		try {
			return
				new URL(CookieUtils.buildServerRootUrl().toString() + value);
		}
		catch(DomainException e) {
			throw
				new ValidationException(
					"The server URL could not be built.",
					e);
		}
		catch(MalformedURLException e) {
			throw 
				new ValidationException(
					ErrorCode.SERVER_INVALID_REDIRECT,
					"The redirect value is not a valid path: " + value,
					e);
		}
	}
}
