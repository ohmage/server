/*******************************************************************************
 * Copyright 2013 Open mHealth
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.domain;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * <p>
 * A list of desired columns.
 * </p>
 *
 * <p>
 * This object works like a tree. The children at any given point should
 * correspond to the data at that same point. Suppose we have some data that
 * looks like:
 * </p>
 *
 * <pre>
 *   {
 *     "field1" : 0,
 *     "field2" : {
 *       "sub-field1" : "bob",
 *       "sub-field2" : "builder"
 *     },
 *     "field3" : 5
 *   }
 * </pre>
 *
 * <p>
 * The current {@link ColumnList} may have the children <tt>["field1",
 * "field2"]</tt>. These can be accessed by calling {@link #getChildren()}. The
 * expressions would be "<tt>field1</tt>" and "<tt>field2</tt>".
 * </p>
 *
 * <p>
 * Because "<tt>field1</tt>" is terminal in the data, its field and value can
 * be safely returned as-is.
 * </p>
 *
 * <p>
 * Because "<tt>field2</tt>" is included as a child, it's field can be safely
 * output, but more work will need to be done to determine what of its value
 * should be returned. This can be achieved by calling
 * {@link #getChild(String)} with a parameter of "<tt>field2</tt>". If null is
 * returned, that would indicate that the entire "<tt>field2</tt>" object
 * should be returned. However, if a {@link ColumnList} is returned, then you
 * must recurse on "<tt>field2</tt>"'s value using this new {@link ColumnList}.
 * </p>
 *
 * <p>
 * Because "<tt>field3</tt>" was not listed, it can be discarded.
 * </p>
 *
 * <p>
 * For example, if the root {@link ColumnList}'s children were
 * <tt>["field1", "field2"]</tt> and "<tt>field2</tt>" had the children
 * <tt>["sub-field2"]</tt>, then the output should be:
 * </p>
 *
 * <pre>
 *   {
 *     "field1" : 0,
 *     "field2" : {
 *       "sub-field2" : "builder"
 *     }
 *   }
 * </pre>
 *
 * <p>
 * Arrays are always output based on the given fields. For example, assume the
 * following data:
 * </p>
 *
 * <pre>
 *   {
 *     "field1" : 0,
 *     "field2" : [
 *       {
 *         "sub-field1" : true,
 *         "sub-field2" : true
 *       },
 *       {
 *         "sub-field1" : false,
 *         "sub-field2" : false
 *       }
 *     ]
 *   }
 * </pre>
 *
 * <p>
 * If the root ColumnList's children were <tt>["field2"]</tt> and "
 * <tt>field2</tt>"'s children were <tt>["sub-field1"]</tt>, then the output
 * should be:
 * </p>
 *
 * <pre>
 *   {
 *     "field2" : [
 *       {
 *         "sub-field1" : true
 *       },
 *       {
 *         "sub-field1" : false
 *       }
 *     ]
 *   }
 * </pre>
 *
 * <p>
 * Due to the relaxed nature of ohmage, it is possible that data may have
 * fields that are not part of the definition. Likewise, fields are sometimes
 * optional. Therefore, it is possible that some {@link ColumnList} children
 * may reference fields that do not exist. For example, for the above data, the
 * root {@link ColumnList}'s children may be
 * <tt>["field1","field2","otherField"]</tt>. "<tt>otherField</tt>" may be
 * safely ignored. Children are only relevant if they correspond to existing
 * data.
 * </p>
 *
 * @author John Jenkins
 */
public class ColumnList {
	/**
	 * The separator for columns as defined by Open mHealth.
	 */
	public static final String COLUMN_SEPARATOR = ".";

	/**
	 * The map of children nodes.
	 */
	final Map<String, ColumnList> children = new HashMap<String, ColumnList>();

	/**
	 * Converts a list of {@link ColumnList#COLUMN_SEPARATOR}-separated columns
	 * into a {@link ColumnList} object.
	 *
	 * @param columns
	 *        The list of columns.
	 */
	public ColumnList(final List<String> columns) {
		// If the columns are null, then this should just be an empty object.
		if(columns == null) {
			return;
		}

		// Add each of the columns as children.
		for(String column : columns) {
			// Process the node.
			addChild(column);
		}
	}

	/**
	 * Creates a new {@link ColumnList} object from the column list.
	 *
	 * @param subColumns
	 *        The string representing the sub-column and, possibly, its
	 *        sub-columns.
	 */
	private ColumnList(final String subColumns) {
		addChild(subColumns);
	}

	/**
	 * Returns an unmodifiable set of the known children.
	 *
	 * @return An unmodifiable set of the known children.
	 */
	public Set<String> getChildren() {
		return Collections.unmodifiableSet(children.keySet());
	}

	/**
	 * Returns the {@link ColumnList} for the given child. If all of the
	 * child's sub-elements are desired, null is returned.
	 *
	 * @param child
	 *        The field whose sub-{@link ColumnList} is desired.
	 *
	 * @return The {@link ColumnList} that describes which of the child's
	 *         fields should also be returned or null indicating that all of
	 *         the child's fields should be returned.
	 *
	 * @throws IllegalArgumentException
	 *         The child value is null or unknown.
	 */
	public ColumnList getChild(
		final String child)
		throws IllegalArgumentException {

		if(child == null) {
			throw new IllegalArgumentException("The child is null.");
		}
		if(! children.containsKey(child)) {
			throw new IllegalArgumentException("The child is unknown.");
		}

		return children.get(child);
	}

	/**
	 * Returns the number of child elements at this depth.
	 *
	 * @return The number of child elements at this depth.
	 */
	public long size() {
		return children.size();
	}

	/**
	 * Creates a list of {@link #COLUMN_SEPARATOR}-separated strings that
	 * represent this column list.
	 *
	 * @return A list of {@link #COLUMN_SEPARATOR}-separated strings that
	 *         represent this list.
	 */
	public List<String> toList() {
		List<String> result = new LinkedList<String>();

		for(String childName : children.keySet()) {
			ColumnList child = children.get(childName);
			if(child == null) {
				result.add(childName);
			}
			else {
				for(String childList : child.toList()) {
					result.add(childName + COLUMN_SEPARATOR + childList);
				}
			}
		}

		return result;
	}

	/**
	 * Creates a comma-separated string of the children.
	 */
	@Override
    public String toString() {
		// Create the builder.
		StringBuilder builder = new StringBuilder();

		// For each element, add it to the string that is being built.
		boolean firstPass = true;
		for(String element : toList()) {
			if(firstPass) {
				firstPass = false;
			}
			else {
				builder.append(',');
			}
			builder.append(element);
		}

		// Return the compiled string.
		return builder.toString();
	}

    /**
     * Adds a new child to the current list of children.
     *
     * @param column
     *        The string representing the child which may be composed of
     *        multiple sub-columns.
     *
     * @throws IllegalArgumentException
     *         The column is null or an empty string.
     */
	private void addChild(final String column)
	    throws IllegalArgumentException {

		if(column == null) {
			throw new IllegalArgumentException("The column is null.");
		}
		if(column.trim().length() == 0) {
			throw new IllegalArgumentException("The column is empty.");
		}

		String[] parts = column.split("\\" + COLUMN_SEPARATOR, 2);
		String childName = parts[0];

		if(parts.length == 1) {
			children.put(childName, null);
		}
		else if(children.containsKey(childName)) {
			ColumnList child = children.get(childName);
			if(child != null) {
				child.addChild(parts[1]);
			}
		}
		else {
			children.put(childName, new ColumnList(parts[1]));
		}
	}
}