package org.ohmage.servlet;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.ohmage.bin.ProjectBin;
import org.ohmage.bin.StreamBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.Project;
import org.ohmage.domain.Project.SchemaReference;
import org.ohmage.domain.exception.InsufficientPermissionsException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.exception.UnknownEntityException;
import org.ohmage.servlet.filter.AuthFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.SessionAttributes;

/**
 * <p>
 * The controller for all requests to project entities.
 * </p>
 *
 * @author John Jenkins
 */
@Controller
@RequestMapping(ProjectServlet.ROOT_MAPPING)
@SessionAttributes(
	{
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN,
		AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM
	})
public class ProjectServlet {
	/**
	 * The root API mapping for this Servlet.
	 */
	public static final String ROOT_MAPPING = "/projects";
	
	/**
	 * The path and parameter key for project IDs.
	 */
	public static final String KEY_PROJECT_ID = "id";
	/**
	 * The path and parameter key for project versions.
	 */
	public static final String KEY_PROJECT_VERSION = "version";
	
	/**
	 * The logger for this class.
	 */
	private static final Logger LOGGER =
		Logger.getLogger(ProjectServlet.class.getName());
	
	/**
	 * The usage in this class is entirely static, so there is no need to
	 * instantiate it.
	 */
	private ProjectServlet() {
		// Do nothing.
	}

	/**
	 * Creates a new project.
	 * 
	 * @param token
	 *        The authentication token for the user that is creating this
	 *        request.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the token was sent as a parameter.
	 * 
	 * @param projectBuilder
	 *        The parts of the project that are already set.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.POST)
	public static @ResponseBody void createProject(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@RequestBody
			final Project.Builder projectBuilder) {
		
		LOGGER.log(Level.INFO, "Creating a project creation request.");
		
		LOGGER
			.log(
				Level.FINE,
				"Ensuring that an authentication token is given.");
		if(token == null) {
			throw
				new InvalidArgumentException(
					"The authentication token is missing.");
		}
		LOGGER
			.log(
				Level.FINE,
				"Ensuring that the authentication token is a parameter.");
		if(! tokenIsParam) {
			throw
				new InvalidArgumentException(
					"The authentication token must be a parameter, " +
						"not just a header.");
		}
		
		// Set the owner of the project.
		projectBuilder.setOwner(token.getUsername());
		
		// Create the project.
		Project project = projectBuilder.build();
		
		// Validate that the streams exist.
		LOGGER.log(Level.INFO, "Validating that the given streams exist.");
		List<SchemaReference> streams = project.getStreams();
		for(SchemaReference stream : streams) {
			// Get the schema ID.
			String id = stream.getSchemaId();
			// Get the schema version.
			Long version = stream.getVersion();

			LOGGER
				.log(Level.INFO, "Checking if the stream is a known stream.");
			if(! StreamBin.getInstance().exists(id, version)) {
				throw
					new InvalidArgumentException(
						"No such stream '" +
							id +
							"'" +
							((version == null) ?
								"" :
								" with version '" + version + "'") +
							".");
			}
		}
		
		// TODO: Validate that the surveys exist.

		LOGGER.log(Level.INFO, "Adding the project to the database.");
		ProjectBin.getInstance().addProject(project);
	}
	
	/**
	 * Returns a list of visible project IDs.
	 * 
	 * @return A list of visible project IDs.
	 */
	@RequestMapping(value = { "", "/" }, method = RequestMethod.GET)
	public static @ResponseBody List<String> getStreamIds() {
		LOGGER.log(Level.INFO, "Creating a project ID read request.");

		return ProjectBin.getInstance().getProjectIds();
	}
	
	/**
	 * Returns a list of versions for the given project.
	 * 
	 * @param projectId
	 *        The project's unique identifier.
	 * 
	 * @return A list of the visible versions.
	 */
	@RequestMapping(
		value = "{" + KEY_PROJECT_ID + "}",
		method = RequestMethod.GET)
	public static @ResponseBody List<Long> getStreamVersions(
		@PathVariable(KEY_PROJECT_ID) final String projectId) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to read the versions of a project: " +
					projectId);
		
		return ProjectBin.getInstance().getProjectVersions(projectId);
	}
	
	/**
	 * Returns the definition for a given project.
	 * 
	 * @param projectId The project's unique identifier.
	 * 
	 * @param projectVersion The version of the project.
	 * 
	 * @return The project definition.
	 */
	@RequestMapping(
		value = "{" + KEY_PROJECT_ID + "}/{" + KEY_PROJECT_VERSION + "}",
		method = RequestMethod.GET)
	public static @ResponseBody Project getStreamDefinition(
		@PathVariable(KEY_PROJECT_ID) final String projectId,
		@PathVariable(KEY_PROJECT_VERSION) final Long projectVersion) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request for a project definition: " +
					projectId + ", " +
					projectVersion);
		
		LOGGER.log(Level.INFO, "Retrieving the project.");
		Project result =
			ProjectBin
				.getInstance()
				.getProject(projectId, projectVersion);

		LOGGER.log(Level.FINE, "Ensuring that a project was found.");
		if(result == null) {
			throw
				new UnknownEntityException(
					"The project ID-verion pair is unknown.");
		}

		LOGGER.log(Level.INFO, "Returning the project.");
		return result;
	}
	
	/**
	 * Updates this project by creating a new project with a new version
	 * number.
	 * 
	 * @param token
	 *        The authentication token for the user updating this project. This
	 *        must belong to the creator of the original project.
	 * 
	 * @param tokenIsParam
	 *        Whether or not the authentication token was passed as a
	 *        parameter.
	 * 
	 * @param projectId
	 *        The original project's identifier, which will be used as the
	 *        identifier for this project as well.
	 * 
	 * @param projectBuilder
	 *        The parts of the project that are already set.
	 */
	@RequestMapping(
		value = "{" + KEY_PROJECT_ID + "}",
		method = RequestMethod.POST)
	public static @ResponseBody void updateProject(
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN)
			final AuthenticationToken token,
		@ModelAttribute(AuthFilter.ATTRIBUTE_AUTHENTICATION_TOKEN_IS_PARAM)
			final boolean tokenIsParam,
		@PathVariable(KEY_PROJECT_ID) final String projectId,
		@RequestBody
			final Project.Builder projectBuilder) {
		
		LOGGER
			.log(
				Level.INFO,
				"Creating a request to update a project with a new version: " +
					projectId);
		
		LOGGER.log(Level.FINE, "Validating the parameters.");
		LOGGER
			.log(
				Level.FINER,
				"Ensuring that an authentication token is given.");
		if(token == null) {
			throw
				new InvalidArgumentException(
					"The authentication token is missing.");
		}
		LOGGER
			.log(
				Level.FINER,
				"Ensuring that the authentication token is a parameter.");
		if(! tokenIsParam) {
			throw
				new InvalidArgumentException(
					"The authentication token must be a parameter, " +
						"not just a header.");
		}
		
		LOGGER
			.log(Level.INFO, "Retrieving the latest version of the project.");
		Project latestProject =
			ProjectBin.getInstance().getLatestProject(projectId);
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the new version of the project is greater " +
					"than all existing ones.");
		long latestVersion = latestProject.getVersion();
		if(latestVersion >= projectBuilder.getVersion()) {
			throw
				new InvalidArgumentException(
					"The new version of this project must be greater than " +
						"the existing latest version of " +
						latestVersion +
						".");
		}
		
		LOGGER
			.log(
				Level.INFO,
				"Verifying that the user updating the project is the owner " +
					"of the original project.");
		if(! latestProject.getOwner().equals(token.getUsername())) {
			throw
				new InsufficientPermissionsException(
					"Only the owner of this project may update it.");
		}
		
		LOGGER
			.log(
				Level.FINE,
				"Setting the request user as the owner of this new project.");
		projectBuilder.setOwner(token.getUsername());
		
		LOGGER.log(Level.INFO, "Saving the updated project.");
		ProjectBin.getInstance().addProject(projectBuilder.build());
	}
		
}