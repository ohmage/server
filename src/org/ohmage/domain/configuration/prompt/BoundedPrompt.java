package org.ohmage.domain.configuration.prompt;

import java.math.BigInteger;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.ohmage.domain.configuration.Prompt;

public abstract class BoundedPrompt extends Prompt {
	public static final String KEY_MIN = "min";
	public static final String KEY_MAX = "max";
	
	/**
	 */
	private final long min;
	/**
	 */
	private final long max;
	
	private final Long defaultValue;
	
	public BoundedPrompt(final String condition, 
			final String id, final String unit,
			final String text, 
			final String abbreviatedText, final String explanationText,
			final boolean skippable, final String skipLabel,
			final DisplayType displayType, final String displayLabel,
			final long min, final long max, final Long defaultValue,
			final Type type, final int index) {
		
		super(condition, id, unit, text, abbreviatedText, explanationText,
				skippable, skipLabel, displayType, displayLabel, 
				type, index);
		
		this.min = min;
		this.max = max;
		
		if(defaultValue != null) {
			if(defaultValue < min) {
				throw new IllegalArgumentException(
						"The default value is less than the minimum value.");
			}
			else if(defaultValue > max) {
				throw new IllegalArgumentException(
						"The default value is greater than hte maximum value.");
			}
		}
		this.defaultValue = defaultValue;
	}
	
	/**
	 * @return
	 */
	public long getMin() {
		return min;
	}
	
	/**
	 * @return
	 */
	public long getMax() {
		return max;
	}
	
	/**
	 * Returns the default value.
	 * 
	 * @return The default value. This may be null if none was given.
	 */
	public Long getDefault() {
		return defaultValue;
	}

	@Override
	public boolean validateValue(Object value) {
		long longValue;
		if(value instanceof String) {
			try {
				longValue = Long.decode((String) value);
			}
			catch(NumberFormatException e) {
				// It's not a number, so it's not valid.
				return false;
			}
		}
		else if(value instanceof Number) {
			if((value instanceof AtomicInteger) ||
					(value instanceof AtomicLong) ||
					(value instanceof BigInteger) ||
					(value instanceof Integer) ||
					(value instanceof Long) ||
					(value instanceof Short)) {
				longValue = ((Number) value).longValue();
			}
			else {
				return false;
			}
		}
		else {
			return false;
		}
		
		if((longValue < min) || (longValue > max)) {
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result
				+ ((defaultValue == null) ? 0 : defaultValue.hashCode());
		result = prime * result + (int) (max ^ (max >>> 32));
		result = prime * result + (int) (min ^ (min >>> 32));
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
		BoundedPrompt other = (BoundedPrompt) obj;
		if (defaultValue == null) {
			if (other.defaultValue != null)
				return false;
		} else if (!defaultValue.equals(other.defaultValue))
			return false;
		if (max != other.max)
			return false;
		if (min != other.min)
			return false;
		return true;
	}
}