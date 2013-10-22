package org.ohmage.mongodb.domain;

import org.mongojack.ObjectId;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

/**
 * <p>
 * The base class for any MongoDB object. This contains basic MongoDB
 * information like the MongoDB-generated ID.
 * </p>
 * 
 * @author John Jenkins
 */
@JsonAutoDetect(
	fieldVisibility = Visibility.DEFAULT,
	getterVisibility = Visibility.NONE,
	isGetterVisibility = Visibility.NONE,
	setterVisibility = Visibility.NONE,
	creatorVisibility = Visibility.DEFAULT)
public interface MongoDbObject {
	/**
	 * Returns the database ID for this entity.
	 * 
	 * @return The database ID for this entity.
	 */
	@ObjectId
	public abstract String getDbId();
}