package org.ohmage.bin;

import java.util.List;

import org.ohmage.domain.Project;
import org.ohmage.domain.exception.InvalidArgumentException;

/**
 * <p>
 * The interface to the database-backed project repository.
 * </p>
 *
 * @author John Jenkins
 */
public abstract class ProjectBin {
	/**
	 * The instance of this ProjectBin to use.
	 */
	protected static ProjectBin instance;

	/**
	 * Default constructor.
	 */
	protected ProjectBin() {
		instance = this;
	}

	/**
	 * Returns the singular instance of this class.
	 * 
	 * @return The singular instance of this class.
	 */
	public static ProjectBin getInstance() {
		return instance;
	}

	/**
	 * Stores a new project.
	 * 
	 * @param project
	 *        The project to be saved.
	 * 
	 * @throws IllegalArgumentExceptoin
	 *         The project is null.
	 * 
	 * @throws InvalidArgumentException
	 *         Another project already exists with the same ID-version pair.
	 */
	public abstract void addProject(
		final Project project)
		throws IllegalArgumentException, InvalidArgumentException;
	
	/**
	 * Returns a list of the visible project IDs.
	 * 
	 * @return A list of the visible project IDs.
	 */
	public abstract List<String> getProjectIds();
	
	/**
	 * Returns a list of the versions for a given project.
	 * 
	 * @param projectId
	 *        The unique identifier for the project.
	 * 
	 * @return A list of the versions of the project.
	 * 
	 * @throws IllegalArgumentException
	 *         The project ID is null.
	 */
	public abstract List<Long> getProjectVersions(
		final String projectId)
		throws IllegalArgumentException;
	
	/**
	 * Returns a Project object for the desired project.
	 * 
	 * @param projectId
	 *        The unique identifier for the project.
	 * 
	 * @param projectVersion
	 *        The version of the project.
	 * 
	 * @return A Project object that represents this project.
	 * 
	 * @throws IllegalArgumentException
	 *         The project ID and/or version are null.
	 */
	public abstract Project getProject(
		final String projectId,
		final Long projectVersion)
		throws IllegalArgumentException;
	
	/**
	 * Returns a Project object that represents the project with the greatest
	 * version number or null if no project exists with the given ID.
	 * 
	 * @param projectId
	 *        The unique identifier for the project.
	 * 
	 * @return A Project object that represents the project with the greatest
	 *         version number or null if no project exists with the given ID.
	 * 
	 * @throws IllegalArgumentException
	 *         The project ID is null.
	 */
	public abstract Project getLatestProject(
		final String projectId)
		throws IllegalArgumentException;
}