package org.ohmage.servlet.spring;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.OhmageException;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.RuntimeJsonMappingException;

/**
 * <p>
 * Handles all exceptions thrown by Spring.
 * </p>
 *
 * @author John Jenkins
 */
public class OhmageHandlerExceptionResolver
	implements HandlerExceptionResolver, Ordered {

	/**
	 * Ensures that our handler is always executed first.
	 */
	@Override
	public int getOrder() {
		return Integer.MIN_VALUE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.web.servlet.HandlerExceptionResolver#resolveException(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.Object, java.lang.Exception)
	 */
	@Override
	public ModelAndView resolveException(
		final HttpServletRequest request,
		final HttpServletResponse response,
		final Object handler,
		final Exception exception) {
		
		// The exception may be a Spring wrapper exception, so we unwrap it and
		// evaluate the underlying exception.
		Throwable cause = exception.getCause();
		if(cause instanceof RuntimeJsonMappingException) {
			Throwable nextCause = cause.getCause();
			if(nextCause instanceof JsonProcessingException) {
				cause = nextCause;
			}
		}

		// If it is one of our exceptions, just echo it.
		if(exception instanceof OhmageException) {
			throw (OhmageException) exception;
		}
		// If it is a Jackson exception, parse out the specific error message
		// and wrap it in one of our exceptions.
		else if(cause instanceof JsonProcessingException) {
			throw
				new InvalidArgumentException(
					((JsonProcessingException) cause).getOriginalMessage(),
					exception);
		}
		// This is a special-case runtime exception for Jackson. We attempt to
		// retrieve the underlying exception and process it.
		else if(cause instanceof RuntimeJsonMappingException) {
			throw new InvalidArgumentException(cause.getMessage(), exception);
		}
		// Otherwise, we let the exception cascade.
		else {
			return null;
		}
	}
}