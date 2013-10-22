package org.ohmage.mongodb.bin;

import java.util.List;

import org.mongojack.DBCursor;
import org.mongojack.JacksonDBCollection;
import org.mongojack.internal.MongoJackModule;
import org.ohmage.bin.ProjectBin;
import org.ohmage.domain.AuthenticationToken;
import org.ohmage.domain.Project;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.mongodb.domain.MongoProject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * <p>
 * The MongoDB implementation of the database-backed project repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoProjectBin extends ProjectBin {
	/**
	 * The name of the collection that contains all of the projects.
	 */
	public static final String COLLECTION_NAME = "project_bin";
	
	/**
	 * The object mapper that should be used to parse
	 * {@link AuthenticationToken}s.
	 */
	private static final ObjectMapper JSON_MAPPER;
	static {
		// Create the object mapper.
		ObjectMapper mapper = new ObjectMapper();
		
		// Create the FilterProvider.
		SimpleFilterProvider filterProvider = new SimpleFilterProvider();
		filterProvider.setFailOnUnknownId(false);
		mapper.setFilters(filterProvider);
		
		// Finally, we must configure the mapper to work with the MongoJack
		// configuration.
		JSON_MAPPER = MongoJackModule.configure(mapper);
	}	
	
	/**
	 * Default constructor.
	 */
	protected MongoProjectBin() {
		// Get the collection to add indexes to.
		DBCollection collection =
			MongoBinController
			.getInstance()
			.getDb()
			.getCollection(COLLECTION_NAME);

		// Ensure that there is an index on the ID.
		collection
			.ensureIndex(
				new BasicDBObject(Project.JSON_KEY_ID, 1),
				COLLECTION_NAME + "_" + Project.JSON_KEY_ID,
				false);
		
		// Ensure that there is an index on the version.
		collection
			.ensureIndex(
				new BasicDBObject(Project.JSON_KEY_VERSION, 1),
				COLLECTION_NAME + "_" + Project.JSON_KEY_VERSION,
				false);
		
		// Ensure that there is an unique index on the ID and version.
		DBObject indexes = new BasicDBObject();
		// Index the ID.
		indexes.put(Project.JSON_KEY_ID, 1);
		// Index the version.
		indexes.put(Project.JSON_KEY_VERSION, 1);
		// Add the index.
		collection
			.ensureIndex(
				indexes, 
				COLLECTION_NAME + "_" +
					Project.JSON_KEY_ID + "_" +
					Project.JSON_KEY_VERSION + "_unique",
				true);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.ProjectBin#addProject(org.ohmage.domain.Project)
	 */
	@Override
	public void addProject(
		final Project project)
		throws IllegalArgumentException, IllegalStateException {
		
		// Validate the parameter.
		if(project == null) {
			throw new IllegalArgumentException("The project is null.");
		}
		
		// Get the authentication token collection.
		JacksonDBCollection<Project, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					Project.class,
					Object.class,
					JSON_MAPPER);
		
		// Save it.
		try {
			collection.insert(project, WriteConcern.REPLICA_ACKNOWLEDGED);
		}
		catch(MongoException.DuplicateKey e) {
			throw
				new InvalidArgumentException(
					"A stream with the same ID-version pair already exists.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.ProjectBin#getProjectIds()
	 */
	@Override
	public List<String> getProjectIds() {
		// Get the connection to the registry with the Jackson wrapper.
		JacksonDBCollection<MongoProject, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoProject.class);
		
		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<String> result = collection.distinct(Project.JSON_KEY_ID);
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.ProjectBin#getProjectVersions(java.lang.String)
	 */
	@Override
	public List<Long> getProjectVersions(
		final String projectId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(projectId == null) {
			throw new IllegalArgumentException("The project ID is null.");
		}
		
		// Get the connection to the registry with the Jackson wrapper.
		JacksonDBCollection<MongoProject, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoProject.class);
		
		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<Long> result =
			collection
				.distinct(
					Project.JSON_KEY_VERSION,
					new BasicDBObject(Project.JSON_KEY_ID, projectId));
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.ProjectBin#getProject(java.lang.String, java.lang.Long)
	 */
	@Override
	public Project getProject(
		final String projectId,
		final Long projectVersion)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(projectId == null) {
			throw new IllegalArgumentException("The project ID is null.");
		}
		if(projectVersion == null) {
			throw new IllegalArgumentException("The project version is null.");
		}
		
		// Get the connection to the registry with the Jackson wrapper.
		JacksonDBCollection<MongoProject, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoProject.class);
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the project ID.
		queryBuilder.and(Project.JSON_KEY_ID).is(projectId);
		
		// Add the project version.
		queryBuilder.and(Project.JSON_KEY_VERSION).is(projectVersion);
		
		// Execute query.
		return collection.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.ProjectBin#getLatestProject(java.lang.String)
	 */
	@Override
	public Project getLatestProject(
		final String projectId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(projectId == null) {
			throw new IllegalArgumentException("The project ID is null.");
		}
		
		// Get the connection to the registry with the Jackson wrapper.
		JacksonDBCollection<MongoProject, Object> collection =
			JacksonDBCollection
				.wrap(
					MongoBinController
						.getInstance()
						.getDb()
						.getCollection(COLLECTION_NAME),
					MongoProject.class);
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the project ID.
		queryBuilder.and(Project.JSON_KEY_ID).is(projectId);
		
		// Create the sort.
		DBObject sort = new BasicDBObject(Project.JSON_KEY_VERSION, -1);
		
		// Make the query.
		DBCursor<MongoProject> result =
			collection.find(queryBuilder.get()).sort(sort).limit(1);
		
		// Return null or the schema based on what the query returned.
		if(result.count() == 0) {
			return null;
		}
		else {
			return result.next();
		}
	}
}