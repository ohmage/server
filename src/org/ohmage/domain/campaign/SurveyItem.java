package org.ohmage.domain.campaign;

/**
 * This class corresponds to all objects that present information to or sample
 * information from a user.  
 * 
 * @author John Jenkins
 */
public abstract class SurveyItem {
	/**
	 * The prompt's campaign-unique identifier.
	 */
	private final String id;
	
	/**
	 * The condition as to whether or not this abstract prompt should be 
	 * processed.
	 */
	private final String condition;
	
	/**
	 * The index of the survey item in its group of survey items.
	 */
	private final int index;
	
	/**
	 * The repeatable set that contains this survey item or null if it is at
	 * the top of the survey hierarchy.
	 */
	private RepeatableSet parent = null;
	
	/**
	 * Creates an abstract prompt with a condition determining if it should be
	 * displayed to the user or not.
	 * 
	 * @param id The campaign-unique identifier for this prompt.
	 * 
	 * @param condition Determines if this abstract prompt should be displayed
	 * 					to the user or not.
	 * 
	 * @param index The index of the survey item in its list of survey items.
	 * 
	 * @throws NullPointerException Thrown if the ID or condition are null.
	 */
	public SurveyItem(final String id, final String condition, final int index) {
		if(id == null) {
			throw new NullPointerException("The ID is null.");
		}
		
		this.id = id;
		this.condition = condition;
		this.index = index;
	}
	
	/**
	 * Returns the campaign-unique identifier for this prompt.
	 * 
	 * @return The campaign-unique identifier for this prompt.
	 */
	public String getId() {
		return id;
	}
	
	/**
	 * Returns the condition.
	 * 
	 * @return The condition as a string.
	 */
	public String getCondition() {
		return condition;
	}
	
	/**
	 * Returns this survey item's index in its group of survey items.
	 * 
	 * @return This survey item's index in its group of survey items.
	 */
	public int getIndex() {
		return index;
	}
	
	/**
	 * Returns this survey item's parent which must be a repeatable set.
	 * 
	 * @return The RepeatableSet object that is the parent of this survey item
	 * 		   or null if the survey item is not part of a repeatable set.
	 */
	public RepeatableSet getParent() {
		return parent;
	}
	
	/**
	 * Sets the parent of this survey item which must be a repeatable set.
	 * 
	 * @param parent The repeatable set that contains this survey item.
	 */
	protected void setParent(final RepeatableSet parent) {
		this.parent = parent;
	}
	
	/**
	 * Returns the number of items contained in this survey item. For messages
	 * and prompts this will only be one, but for repeatable sets this may be
	 * many. Also, repeatable sets should return at least one indicating
	 * themselves.
	 * 
	 * @return The number of survey items that this survey item 
	 * 		   contains/represents.
	 */
	public abstract int getNumSurveyItems();
	
	/**
	 * Returns the number of prompts that may be displayed to a user from this
	 * abstract prompt.
	 * 
	 * @return The number of prompts that may be displayed to a user from this
	 * 		   abstract prompt.
	 */
	public abstract int getNumPrompts();

	/**
	 * Creates a hash code value for this survey item.
	 * 
	 * @return A hash code value for this survey item.
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	/**
	 * Determines if this survey item is equal to another object.
	 * 
	 * @return True if this survey item is logically equivalent to the other
	 * 		   object; false, otherwise.
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SurveyItem other = (SurveyItem) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
