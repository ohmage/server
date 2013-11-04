package org.ohmage.mongodb.domain;

import java.util.Iterator;

import org.mongojack.DBCursor;
import org.ohmage.domain.MultiValueResult;

/**
 * <p>
 * The {@link MultiValueResult} for MongoDB based on a {@link DBCursor}.
 * </p>
 *
 * @author John Jenkins
 */
public class MongoCursorMultiValueResult<T> implements MultiValueResult<T> {
	/**
	 * The cursor that was used to make the query and contains the results.
	 */
	private final DBCursor<T> cursor;
	
	/**
	 * Creates a new MongoDB multi-value result from a cursor.
	 * 
	 * @param cursor
	 *        The cursor used to make the query and that contains the results.
	 */
	public MongoCursorMultiValueResult(final DBCursor<T> cursor) {
		this.cursor = cursor;
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.MultiValueResult#count()
	 */
	@Override
	public int count() {
		return cursor.count();
	}

	/*
	 * (non-Javadoc)
	 * @see org.ohmage.domain.MultiValueResult#size()
	 */
	@Override
	public int size() {
		return cursor.size();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	@Override
	public Iterator<T> iterator() {
		return cursor.iterator();
	}
}