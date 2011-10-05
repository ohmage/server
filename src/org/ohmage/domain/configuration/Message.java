package org.ohmage.domain.configuration;

import org.ohmage.util.StringUtils;

public final class Message extends SurveyItem {
	/**
	 */
	private final String text;
	
	public Message(final String id, final String condition, final int index,
			final String text) {
		super(id, condition, index);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(text)) {
			throw new IllegalArgumentException("The text cannot be null or whitespace only.");
		}
		
		this.text = text;
	}
	
	/**
	 * @return
	 */
	public String getText() {
		return text;
	}
	
	@Override
	public int getNumPrompts() {
		return 0;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((text == null) ? 0 : text.hashCode());
		return result;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		Message other = (Message) obj;
		if (text == null) {
			if (other.text != null)
				return false;
		} else if (!text.equals(other.text))
			return false;
		return true;
	}
}
