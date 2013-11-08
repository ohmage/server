package org.ohmage.mongodb.bin;

import java.util.List;
import java.util.regex.Pattern;

import org.mongojack.DBQuery;
import org.mongojack.DBQuery.Query;
import org.mongojack.JacksonDBCollection;
import org.mongojack.WriteResult;
import org.ohmage.bin.CommunityBin;
import org.ohmage.domain.Community;
import org.ohmage.domain.User;
import org.ohmage.domain.exception.InconsistentDatabaseException;
import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.stream.Stream;
import org.ohmage.mongodb.domain.MongoCommunity;

import com.mongodb.BasicDBObject;
import com.mongodb.MongoException;
import com.mongodb.QueryBuilder;
import com.mongodb.WriteConcern;

/**
 * <p>
 * The MongoDB implementation of the database-backed community repository.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoCommunityBin extends CommunityBin {
	/**
	 * The name of the collection that contains all of the communities.
	 */
	public static final String COLLECTION_NAME = "community_bin";
	
	/**
	 * Get the connection to the community bin with the Jackson wrapper.
	 */
	private static final JacksonDBCollection<Community, Object> COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				Community.class,
				Object.class,
				MongoBinController.getObjectMapper());
	
	/**
	 * Get the connection to the community bin with the Jackson wrapper,
	 * specifically for {@link MongoCommunity} objects.
	 */
	private static final JacksonDBCollection<MongoCommunity, Object> MONGO_COLLECTION =
		JacksonDBCollection
			.wrap(
				MongoBinController
					.getInstance()
					.getDb()
					.getCollection(COLLECTION_NAME),
				MongoCommunity.class,
				Object.class,
				MongoBinController.getObjectMapper());
	
	/**
	 * Default constructor.
	 */
	protected MongoCommunityBin() {
		// Ensure that there is an index on the ID.
		COLLECTION
			.ensureIndex(
				new BasicDBObject(Community.JSON_KEY_ID, 1),
				COLLECTION_NAME + "_" + Community.JSON_KEY_ID,
				false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.CommunityBin#addCommunity(org.ohmage.domain.Community)
	 */
	@Override
	public void addCommunity(
		final Community community)
		throws IllegalArgumentException, IllegalStateException {
		
		// Validate the parameter.
		if(community == null) {
			throw new IllegalArgumentException("The community is null.");
		}
		
		// Save it.
		try {
			COLLECTION.insert(community);
		}
		catch(MongoException.DuplicateKey e) {
			throw
				new InvalidArgumentException(
					"A stream with the same ID-version pair already exists.");
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.CommunityBin#getCommunityIds(java.lang.String, java.lang.String)
	 */
	@Override
	public List<String> getCommunityIds(
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
						// Either, the community's privacy state must be
						// INVITE_ONLY+.
						QueryBuilder
							.start()
							.and(Community.JSON_KEY_PRIVACY_STATE)
							.greaterThanEquals(
								Community.PrivacyState.INVITE_ONLY.ordinal())
							.get(),
						// Or, the user must already be a member.
						QueryBuilder
							.start()
							.and(Community.JSON_KEY_MEMBERS +
								"." +
								Community.Member.JSON_KEY_MEMBER_ID)
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
			MONGO_COLLECTION.distinct(Community.JSON_KEY_ID, queryBuilder.get());
		
		return result;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.CommunityBin#getCommunity(java.lang.String, java.lang.Long)
	 */
	@Override
	public Community getCommunity(
		final String communityId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(communityId == null) {
			throw new IllegalArgumentException("The community ID is null.");
		}
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the community ID.
		queryBuilder.and(Community.JSON_KEY_ID).is(communityId);
		
		// Execute query.
		return MONGO_COLLECTION.findOne(queryBuilder.get());
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.bin.CommunityBin#updateCommunity(org.ohmage.domain.Community)
	 */
	@Override
	public void updateCommunity(
		final Community community)
		throws IllegalArgumentException {
		
		if(community == null) {
			throw new IllegalArgumentException("The community is null.");
		}

		// Create the query.
		// Limit the query only to this community.
		Query query = DBQuery.is(Community.JSON_KEY_ID, community.getId());
		// Ensure that the community has not been updated elsewhere.
		query =
			query
				.is(User.JSON_KEY_INTERNAL_VERSION,
					community.getInternalReadVersion());
		
		// Commit the update and don't return until the collection has heard
		// the result.
		WriteResult<Community, Object> result =
			COLLECTION
				.update(
					query,
					community,
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
	 * @see org.ohmage.bin.CommunityBin#deleteCommunity(java.lang.String)
	 */
	@Override
	public void deleteCommunity(String communityId)
		throws IllegalArgumentException {
		
		// Validate the input.
		if(communityId == null) {
			throw new IllegalArgumentException("The community ID is null.");
		}
		
		// Build the query
		QueryBuilder queryBuilder = QueryBuilder.start();
		
		// Add the community ID.
		queryBuilder.and(Community.JSON_KEY_ID).is(communityId);
		
		// Delete the community.
		COLLECTION.remove(queryBuilder.get());
	}
}