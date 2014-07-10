package org.ohmage.javax.servlet.filter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * <p>
 * <b>This filter should never be enabled in production. It will prevent some
 * requests from being processed.</b>
 * </p>
 *
 * <p>
 * Dumps the request to the logs.
 * </p>
 *
 * @author John Jenkins
 */
public class DumpFilter implements Filter {
    /**
     * The logger to use to dump the information.
     */
    private static final Logger LOGGER =
        LoggerFactory.getLogger(DumpFilter.class.toString());

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(final FilterConfig filterConfig) throws ServletException {
        // Do nothing.
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
     * javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(
        final ServletRequest request,
        final ServletResponse response,
        final FilterChain filterChain)
        throws IOException, ServletException {

        byte[] buffer = new byte[4096];
        InputStream in = request.getInputStream();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int numRead;
        while((numRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, numRead);
        }

        LOGGER.info("Contents: " + out.toString());

        filterChain.doFilter(request, response);
    }

    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {
        // Do nothing.
    }
}
