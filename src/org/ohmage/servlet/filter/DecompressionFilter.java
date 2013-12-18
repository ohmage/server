package org.ohmage.servlet.filter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ReadListener;
import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.Part;

import org.apache.commons.io.IOUtils;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * A filter that checks for compression on upload and wraps the request's
 * InputStream and part's InputStream in decompressors.
 * </p>
 *
 * @author John Jenkins
 */
public class DecompressionFilter implements Filter {
    /**
     * <p>
     * A wrapper for {@link ServetInputStream}s that add GZIP decompression
     * when reading.
     * </p>
     *
     * @author John Jenkins
     */
    public static class GZIPServletInputStream extends ServletInputStream {
        /**
         * <p>
         * The maximum amount of data that may be sent to the server in a ZIP
         * file.
         * </p>
         *
         * <p>
         * Currently, this value is set to 500 MB.
         * </p>
         */
        public static final long ZIP_CONTENTS_LIMIT = 500 * 1024 * 1024;

        /**
         * The wrapped {@link ServletInputStream} that should be used to
         * delegate any of the other functionality that the GZIP stream does
         * not directly handle.
         */
        private final ServletInputStream servletInputStream;
        /**
         * The GZIP stream that should be used to for reading data.
         */
        private final GZIPInputStream wrappedGzipInputStream;

        /**
         * The amount of data that has been read.
         */
        private long amountRead = 0;

        /**
         * Wraps a {@link ServletInputStream} with a {@link GZIPInputStream}.
         *
         * @param stream
         *        The stream to wrap.
         *
         * @throws IOException
         *         The stream could not be wrapped.
         */
        public GZIPServletInputStream(final ServletInputStream stream)
            throws IOException {

            if(stream == null) {
                throw
                    new IllegalArgumentException(
                        "The stream to wrap is null.");
            }

            servletInputStream = stream;
            wrappedGzipInputStream = new GZIPInputStream(stream);
        }

        /**
         * This is a private constructor to only be used when a zip-bomb-aware,
         * GZIP'd input stream is required.
         *
         * @param stream
         *        The {@link InputStream} to wrap.
         *
         * @throws IOException
         *         The stream to wrap could not be read.
         */
        private GZIPServletInputStream(final InputStream stream)
            throws IOException {

            servletInputStream = null;
            wrappedGzipInputStream = new GZIPInputStream(stream);
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.ServletInputStream#isReady()
         */
        @Override
        public boolean isReady() {
            return servletInputStream.isReady();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.ServletInputStream#isFinished()
         */
        @Override
        public boolean isFinished() {
            return servletInputStream.isFinished();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.ServletInputStream#setReadListener(javax.servlet.ReadListener)
         */
        @Override
        public void setReadListener(final ReadListener readListener) {
            servletInputStream.setReadListener(readListener);
        }

        /*
         * (non-Javadoc)
         * @see java.io.InputStream#read()
         */
        @Override
        public int read() throws IOException {
            // The simplest form of zip-bomb detection, which should work well
            // for systems that efficiently utilize streaming.
            if(amountRead++ > ZIP_CONTENTS_LIMIT) {
                throw
                    new IOException(
                        "The zip file contents have exceeded the maximum " +
                            "allowed size.");
            }

            return wrappedGzipInputStream.read();
        }
    }

    /**
     * <p>
     * A wrapper for {@link Parts}s that add GZIP decompression when reading.
     * </p>
     *
     * @author John Jenkins
     */
    public static class GZIPPart implements Part {
        /**
         * The {@link Part} that backs this part.
         */
        private final Part part;

        /**
         * Creates a new GZIPPart object.
         *
         * @param part
         *        The {@link Part} to wrap.
         */
        public GZIPPart(final Part part) {
            if(part == null) {
                throw new IllegalArgumentException("The part is null.");
            }

            this.part = part;
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#delete()
         */
        @Override
        public void delete() throws IOException {
            part.delete();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getContentType()
         */
        @Override
        public String getContentType() {
            return part.getContentType();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getHeader(java.lang.String)
         */
        @Override
        public String getHeader(final String name) {
            return part.getHeader(name);
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getHeaderNames()
         */
        @Override
        public Collection<String> getHeaderNames() {
            return part.getHeaderNames();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getHeaders(java.lang.String)
         */
        @Override
        public Collection<String> getHeaders(final String name) {
            return part.getHeaders(name);
        }

        /**
         * Returns the {@link InputStream} wrapped around a
         * {@link GZIPInputStream} to facilitate decompression.
         */
        @Override
        public InputStream getInputStream() throws IOException {
            return new GZIPServletInputStream(part.getInputStream());
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getName()
         */
        @Override
        public String getName() {
            return part.getName();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getSize()
         */
        @Override
        public long getSize() {
            return part.getSize();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#getSubmittedFileName()
         */
        @Override
        public String getSubmittedFileName() {
            return part.getSubmittedFileName();
        }

        /*
         * (non-Javadoc)
         * @see javax.servlet.http.Part#write(java.lang.String)
         */
        @Override
        public void write(final String filename) throws IOException {
            IOUtils
                .copy(
                    getInputStream(),
                    new FileOutputStream(new File(filename)));
        }
    }

    /**
     * <p>
     * A wrapper {@link HttpServletRequest}s that adds GZIP decompression when
     * reading.
     * </p>
     *
     * @author John Jenkins
     */
    public static class DecompressionAwareHttpServletRequest
        extends HttpServletRequestWrapper {

        /**
         * Wraps a {@link HttpServletRequest} with the GZIP input stream.
         *
         * @param request
         *        The {@link HttpServletRequset} to wrap.
         */
        public DecompressionAwareHttpServletRequest(
            final HttpServletRequest request) {

            super(request);
        }

        /**
         * Returns a {@link GZIPServletInputStream}.
         */
        @Override
        public ServletInputStream getInputStream() throws IOException {
            return new GZIPServletInputStream(super.getInputStream());
        }

        /**
         * Checks if the part had a "Content-Encoding" header that we
         * understand and, if so, wraps it appropriately.
         */
        @Override
        public Part getPart(final String name)
            throws IOException, ServletException {

            // Get the part from the parent.
            Part wrappedPart = super.getPart(name);

            // Make sure the part exists.
            if(wrappedPart != null) {
                wrappedPart = checkPart(wrappedPart);
            }

            // Return the wrapped part.
            return wrappedPart;
        }

        /**
         * Checks if any of the parts had a "Content-Encoding" header that we
         * understand and, if so, wraps that part appropriately.
         */
        @Override
        public Collection<Part> getParts()
            throws IOException, ServletException {

            // Check if any parts exist and, if so, check if they need to be
            // wrapped.
            List<Part> parts = new LinkedList<Part>();

            // Check each of the parts.
            for(Part part : super.getParts()) {
                // Check and add the part to the result list.
                parts.add(checkPart(part));
            }

            // Return the updated list.
            return parts;
        }

        /**
         * Examines a part's headers and wraps it as appropriate.
         *
         * @param part
         *        The part to be examined and, potentially, wrapped.
         *
         * @return The wrapped part.
         */
        protected Part checkPart(final Part part) {
            // Create a handle to the result.
            Part result = part;

            // Check all of the "Content-Encoding" headers.
            for(String partHeader : part.getHeaders(HEADER_CONTENT_ENCODING)) {
                // The encodings are comma-separated.
                String[] partHeaderParts = partHeader.split(",");

                // Check each encoding.
                for(String partHeaderPart : partHeaderParts) {
                    // Check if it is GZIP.
                    if(CONTENT_ENCODING_GZIP
                        .equals(partHeaderPart.trim())) {

                        LOGGER
                            .log(
                                Level.INFO,
                                "A part was encoded using GZIP.");
                        result = new GZIPPart(result);
                    }
                    // Otherwise, we don't understand it and cannot satisfy
                    // the request.
                    else {
                        throw
                            new InvalidArgumentException(
                                "The content encoding is unknown: " +
                                    partHeaderPart);
                    }
                }
            }

            // Return the result.
            return result;
        }
    }

    /**
     * The header for request content encoding.
     */
    public static final String HEADER_CONTENT_ENCODING = "Content-Encoding";
    /**
     * The GZIP content encoding name.
     */
    public static final String CONTENT_ENCODING_GZIP = "gzip";

    /**
     * The logger for this class.
     */
    private static final Logger LOGGER =
        Logger.getLogger(DecompressionFilter.class.getName());

    /**
     * Does nothing.
     */
    @Override
    public void init(final FilterConfig config) throws ServletException {
        // Do nothing.
    }

    /**
     * Check if Content-Encoding is enabled on the request itself or on any of
     * the parts. If so, wrap the request and/or parts in a GZIP filter.
     */
    @Override
    public void doFilter(
        final ServletRequest request,
        final ServletResponse response,
        final FilterChain chain)
        throws IOException, ServletException {

        LOGGER.log(Level.INFO, "Executing the decompression filter.");

        // Prepare a wrapper for the request.
        ServletRequest wrappedRequest = request;

        // This provides support for HTTP requests. For other Servlet types,
        // use a complimentary "else if" statement and custom ServletRequest
        // wrapper.
        if(request instanceof HttpServletRequest) {
            LOGGER
                .log(
                    Level.FINE,
                    "This is a HTTP request. Checking the headers.");

            // Cast the request.
            HttpServletRequest httpRequest = (HttpServletRequest) request;

            // Get the headers.
            Enumeration<String> contentEncodings =
                httpRequest.getHeaders(HEADER_CONTENT_ENCODING);

            // Check each Content-Encoding header.
            while(contentEncodings.hasMoreElements()) {
                // Get the value of the header.
                String element = contentEncodings.nextElement();

                // Split the header based on the commas to get the individual
                // encodings.
                String[] elements = element.split(",");

                // Check each encoding.
                for(String elementPart : elements) {
                    // Check if it is GZIP.
                    if(CONTENT_ENCODING_GZIP.equals(elementPart.trim())) {
                        LOGGER
                            .log(
                                Level.INFO,
                                "The content was encoded using GZIP.");

                        wrappedRequest =
                            new DecompressionAwareHttpServletRequest(
                                (HttpServletRequest) wrappedRequest);
                    }
                    // Otherwise, we don't understand it and cannot satisfy the
                    // request.
                    else {
                        throw
                            new InvalidArgumentException(
                                "The content encoding is unknown: " +
                                    elementPart);
                    }
                }
            }
        }

        // Continue processing, potentially with a wrapped part.
        chain.doFilter(wrappedRequest, response);
    }

    /**
     * Does nothing.
     */
    @Override
    public void destroy() {
        // Do nothing.
    }
}