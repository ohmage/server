package org.ohmage.request.omh;

import java.io.IOException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonGenerator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.InvalidRequestException;
import org.ohmage.exception.ServiceException;
import org.ohmage.request.UserRequest;
import org.ohmage.request.observer.StreamReadRequest.ColumnNode;
import org.ohmage.service.OmhServices;

/**
 * This class is responsible for Mood Map requests if we decide to use the App
 * Engine version.
 * 
 * NOTE: This is not working. This is being left in case it will be used, but
 * it is incomplete.
 *
 * @author John Jenkins
 */
public class OmhReadMoodMapRequest 
		extends UserRequest
		implements OmhReadResponder {
	
	private static final Logger LOGGER =
		Logger.getLogger(OmhReadMoodMapRequest.class);
	
	private static final DateTimeFormatter MOOD_MAP_DATE_TIME_FORMAT =
		DateTimeFormat.forPattern("yyyy-MM-dd-HH-mm-ss");
	
	private final DateTime startDate;
	private final DateTime endDate;
	private final long numToSkip;
	private final long numToReturn;
	
	/**
	 * Creates a new OMH read request for Mood Map information.
	 * 
	 * @param httpRequest The HTTP request.
	 * 
	 * @param parameters The parameters already decoded out of the request.
	 * 
	 * @param hashPassword Whether or not to hash the password. If it is null,
	 * 					   a username and password won't be used for 
	 * 					   authentication.
	 * 
	 * @param tokenLocation Where to look for the authentication token. If it 
	 * 						is null, an authentication token is not allowed for
	 * 						this request.
	 * 
	 * @param callClientRequester Refers to the "client" parameter as the
	 * 							  "requester".
	 * 
	 * @throws IOException There was a problem reading the parameters.
	 * 
	 * @throws InvalidRequestException The request parameters were invalid.
	 */
	public OmhReadMoodMapRequest(
			final HttpServletRequest httpRequest,
			final Map<String, String[]> parameters,
			final Boolean hashPassword,
			final TokenLocation tokenLocation,
			final boolean callClientRequester,
			final DateTime startDate,
			final DateTime endDate,
			final long numToSkip,
			final long numToReturn)
			throws IOException, InvalidRequestException {
		
		super(httpRequest, hashPassword, tokenLocation, parameters, callClientRequester);
		
		if(! isFailed()) {
			LOGGER.info("Creating an OMH read request for Mood Map.");
		}
		this.startDate = startDate;
		this.endDate = endDate;
		this.numToSkip = numToSkip;
		this.numToReturn = numToReturn;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.Request#service()
	 */
	@Override
	public void service() {
		LOGGER.info("Servicing an OMH read Mood Map request.");
		
		if(! authenticate(AllowNewAccount.NEW_ACCOUNT_DISALLOWED)) {
			return;
		}
		
		try {
			// TODO: Verify that the user is allowed to query this data through
			// ohmage.
			// This is being ignored for now because there is no "user" 
			// parameter. When the "user" parameter is added, the ACL states
			// that the requesting user must be a supervisor in any campaign to
			// which the user is a participant.
			// Note: This should not be pushed to a public server because, by
			// default everyone is a participant in every campaign. Therefore,
			// anyone who manages to elevate their privileges to supervisor of
			// a campaign will be able to execute this call against everyone
			// else in the system.
			
			// Get the authentication information from the database.
			LOGGER
				.info("Getting the authentication credentials for Mood Map.");
			Map<String, String> moodMapCredentials =
				OmhServices.instance().getCredentials("mood_map");
			
			// TODO: Get the data and massage it into a form we like.
			getData();
		}
		catch(ServiceException e) {
			e.failRequest(this);
			e.logException(LOGGER);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#getNumDataPoints()
	 */
	@Override
	public long getNumDataPoints() {
		// FIXME: This needs to be implemented once the getData() method is
		// completed.
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.request.omh.OmhReadResponder#respond(org.codehaus.jackson.JsonGenerator, org.ohmage.request.observer.StreamReadRequest.ColumnNode)
	 */
	@Override
	public void respond(
			final JsonGenerator generator,
			final ColumnNode<String> columns)
			throws JsonGenerationException, IOException, DomainException {
		
		// FIXME: This needs to be completed once the getData() method is
		// completed.
	}

	/**
	 * HTTP requests are not allowed for this API.
	 * 
	 * @throws UnsupportedOperationException HTTP requests are not allowed for
	 * 										 this request.
	 */
	@Override
	public void respond(
			final HttpServletRequest httpRequest,
			final HttpServletResponse httpResponse)
			throws UnsupportedOperationException {

		if(isFailed()) {
			super.respond(httpRequest, httpResponse, null);
		}
		else {
			throw new UnsupportedOperationException(
				"HTTP requests are not allowed for this API.");
		}
	}

	/**
	 * Retrieves the data and molds it into a form that is more 
	 * client-friendly.
	 * 
	 * @return The molded data.
	 * 
	 * @throws ServiceException There was an error reading or molding the data.
	 */
	private void getData() throws ServiceException {
		// The method involving the API.
		/*
		 * The following dependencies are required:
		 * 
		 * lib/google-api-client-1.10.3-beta.jar
		 * lib/google-api-client-appengine-1.10.3-beta.jar
		 * lib/google-http-client-1.10.3-beta.jar
		 * lib/google-http-client-appengine-1.10.3-beta.jar
		 * lib/google-oauth-client-1.10.1-beta.jar
		 * lib/google-oauth-client-appengine-1.10.1-beta.jar
		 * lib/guava-11.0.1.jar
		 * 
		 * They can be retrieved from:
		 * 
		 * http://code.google.com/p/google-api-java-client/wiki/ClientLogin
		 * 
		// Get the Google authentication token.
		HttpRequestFactory requestFactory;
		String token;
		try {
			// Create the transport object that will be used to build the
			// request from the authenticated token.
			HttpTransport httpTransport = new NetHttpTransport();
			
			// Authenticate with ClientLogin
			ClientLogin authenticator = new ClientLogin();
			authenticator.authTokenType = "ah";
			authenticator.username = "omh.moodphone@gmail.com"; 
			authenticator.password = "jump.they.s4y";
			authenticator.transport = httpTransport;
			Response response = authenticator.authenticate();
			requestFactory = httpTransport.createRequestFactory(response);
			token = response.getAuthorizationHeaderValue().substring(17);
		}
		catch(HttpResponseException e) {
			throw new ServiceException(
				"There was an problem reading the response.",
				e);
		}
		catch(IOException e) {
			throw new ServiceException(
				"There was an problem communicating with the server.",
				e);
		}
		
		// Build the request URL.
		StringBuilder urlBuilder = 
			new StringBuilder(
				"http://moodphoneomh.appspot.com/studies/manage");
		
		// Add the start date parameter.
		if(startDate != null) {
			urlBuilder
				.append("&DateMin=")
				.append(MOOD_MAP_DATE_TIME_FORMAT.print(startDate));
		}

		// Add the end date parameter.
		if(endDate != null) {
			urlBuilder
				.append("&DateMax=")
				.append(MOOD_MAP_DATE_TIME_FORMAT.print(endDate));
		}
		
		// Build the request.
		HttpRequest request;
		try {
			request =
				requestFactory
					.buildGetRequest(new GenericUrl(urlBuilder.toString()));

			//request = new HttpGet(urlBuilder.toString());
		}
		catch(IOException e) {
			throw new ServiceException(
				"There was a problem creating the request.",
				e);
		}
		
		//request.addHeader("Cookie", "ACSID=" + token);
		
		//HttpClient client = new DefaultHttpClient();
		
		// Get the request's response.
		HttpResponse response;
		try {
			response = request.execute();
		}
		catch(IOException e) {
			throw new ServiceException(
				"There was an error performing the request.",
				e);
		}
		
		// Check to make sure we got a HTTP 200 response.
		if(response.getStatusCode() != HttpStatusCodes.STATUS_CODE_OK) {
			throw new ServiceException(
				"There was an error reading the data (" +
					response.getStatusCode() +
					"):" +
					response.getStatusMessage());
		}
		
		// Get the input stream.
		InputStream inputStream;
		try {
			inputStream = response.getContent();
		}
		catch(IOException e) {
			throw new ServiceException("Error connecting to the response.", e);
		}
		
		// Read the data.
		int amountRead;
		byte[] chunk = new byte[4096];
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			while((amountRead = inputStream.read(chunk, 0, chunk.length)) != -1) {
				baos.write(chunk, 0, amountRead);
			}
		}
		catch(IOException e) {
			throw new ServiceException("Error while reading the response.", e);
		}
		
		LOGGER.debug("Data: " + baos.toString());
		*/
		
		// The method involving using AppEngine directly.
		/*
		 * The following dependencies are required:
		 * 
		 * appengine-api.jar
		 * appengine-remote-api.jar
		 * 
		 * They can be retrieved from the App Engine SDK.
		 * 
		// Set the required information for the remote server.
		RemoteApiOptions options =
			new RemoteApiOptions()
				.server("moodphoneomh.appspot.com", 443)
				.credentials("omh.moodphone@gmail.com", "jump.they.s4y");
		
		// Set our "local" database to the remote database.
		RemoteApiInstaller installer = new RemoteApiInstaller();
		try {
			installer.install(options);
			
			// Get the datastore service which should now be connected to the
			// remote datastore.
			DatastoreService ds = 
				DatastoreServiceFactory.getDatastoreService();
			
			// TODO: Perform the query against the datastore.
		}
		catch(IOException e) {
			throw new ServiceException(
				"There was an error reading from the remote server.",
				e);
		}
		// Disconnect from the remote database.
		finally {
			installer.uninstall();
		}
		*/
	}
}
