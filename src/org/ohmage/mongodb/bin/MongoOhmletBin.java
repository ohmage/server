package org.ohmage.mongodb.bin;

import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.ohmage.bin.OhmletBin;
import org.ohmage.domain.Ohmlet;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.mongodb.domain.MongoOhmlet;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * <p>
 * The MongoDB implementation of the database-backed ohmlet repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoOhmletBin extends OhmletBin {
	/**
	 * The name of the collection that contains all of the communities.
	 */
	public static final String COLLECTION_NAME = "ohmlet_bin";
	
	/**
	 * Get the connection to the ohmlet bin with the Jackson wrapper.
	 */
	private static final JacksonDBCollection<Ohmlet, Object> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				Ohmlet.class,
				Object.class,
				MongoBinController.getObjectMapper());
	
	/**
	 * Get the connection to the ohmlet bin with the Jackson wrapper,
	 * specifically for {@link MongoOhmlet} objects.
	 */
	private static final JacksonDBCollection<MongoOhmlet, Object> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoOhmlet.class,
				Object.class,
				MongoBinController.getObjectMapper());
	
	/**
	 * Default constructor.
	 */
	protected MongoOhmletBin() {
		// Ensure that there is an index on the ID.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(Ohmlet.JSON_KEY_ID, 1),
				COLLECTION_NAME + "_" + Ohmlet.JSON_KEY_ID,
				false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.OhmletBin#addOhmlet(org.ohmage.domain.Ohmlet)
	 */
	@Override
	public void addOhmlet(
		final Ohmlet ohmlet)
		throws IllegalArgumentException, IllegalStateException {
		
		// Validate the parameter.
		if(ohmlet == null) {
			throw new IllegalArgumentException("The ohmlet is null.");
		}
		
		// Save it.
		try {
			COLLECTION.insert(ohmlet);
		}
		catch(MongoException.DuplicateKey e) {
			throw
				new InvalidArgumentException(
					"A stream with the same ID-version pair already exists.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.OhmletBin#getOhmletIds(java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> getOhmletIds(
		final String username,
		final String query) {
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Be sure that they are visible.
		queryBuilder
			.and(
				QueryBuilder
					.start()
					.or(
						// Either, the ohmlet's privacy state must be
						// INVITE_ONLY+.
						QueryBuilder
							.start()
							.and(Ohmlet.JSON_KEY_PRIVACY_STATE)
							.greaterThanEquals(
								Ohmlet.PrivacyState.INVITE_ONLY.ordinal())
							.get(),
						// Or, the user must already be a member.
						QueryBuilder
							.start()
							.and(Ohmlet.JSON_KEY_MEMBERS +
								"." +
								Ohmlet.Member.JSON_KEY_MEMBER_ID)
							.is(username)
							.get())
				.get());
				
		// If given, add the query for the name and description.
		if(query != null) {
			// Build the query pattern.
			Pattern queryPattern = Pattern.compile(".*" + query + ".*");
			
			// Add the query fields.
			queryBuilder
				.and(
					QueryBuilder
						.start()
						.or(
							// Add the query for the name.
							QueryBuilder
								.start()
								.and(Stream.JSON_KEY_NAME)
								.regex(queryPattern)
								.get(),
							// Add the query for the description.
							QueryBuilder
								.start()
								.and(Stream.JSON_KEY_VERSION)
								.regex(queryPattern)
								.get())
						.get());
		}
		
		// Get the list of results.
		@SuppressWarnings("unchecked")
		List<String> result =
			MONGO_COLLECTION.distinct(Ohmlet.JSON_KEY_ID, queryBuilder.get());
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.OhmletBin#getOhmlet(java.lang.String, java.lang.Long)
	 */
	@Override
	public Ohmlet getOhmlet(
		final String ohmletId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(ohmletId == null) {
			throw new IllegalArgumentException("The ohmlet ID is null.");
		}
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the ohmlet ID.
		queryBuilder.and(Ohmlet.JSON_KEY_ID).is(ohmletId);
		
		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.OhmletBin#updateOhmlet(org.ohmage.domain.Ohmlet)
	 */
	@Override
	public void updateOhmlet(
		final Ohmlet ohmlet)
		throws IllegalArgumentException {
		
		if(ohmlet == null) {
			throw new IllegalArgumentException("The ohmlet is null.");
		}

		// Create the query.
		// Limit the query only to this ohmlet.
		Query query = DBQuery.is(Ohmlet.JSON_KEY_ID, ohmlet.getId());
		// Ensure that the ohmlet has not been updated elsewhere.
		query =
			query
				.is(User.JSON_KEY_INTERNAL_VERSION,
					ohmlet.getInternalReadVersion());
		
		// Commit the update and don't return until the collection has heard
		// the result.
		WriteResult<Ohmlet, Object> result =
			COLLECTION
				.update(
					query,
					ohmlet,
					false,
					false,
					WriteConcern.REPLICA_ACKNOWLEDGED);
		
		// Be sure that at least one document was updated.
		if(result.getN() == 0) {
			throw
				new InconsistentDatabaseException(
					"A conflict occurred. Please, try again.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.OhmletBin#deleteOhmlet(java.lang.String)
	 */
	@Override
	public void deleteOhmlet(String ohmletId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(ohmletId == null) {
			throw new IllegalArgumentException("The ohmlet ID is null.");
		}
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the ohmlet ID.
		queryBuilder.and(Ohmlet.JSON_KEY_ID).is(ohmletId);
		
		// Delete the ohmlet.
		COLLECTION.remove(queryBuilder.get());
	}
}