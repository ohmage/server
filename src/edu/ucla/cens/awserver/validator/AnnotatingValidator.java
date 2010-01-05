package edu.ucla.cens.awserver.validator;

/**
 * Abstract base class for Validators which need to annotate an AwRequest as part of their processing.
 * 
 * @author selsky
 */
public abstract class AnnotatingValidator implements Validator {
	private AnnotateAwRequestStrategy _annotatingValidationStrategy;
	
	/**
	 * @throws IllegalArgumentException if the provided AnnotateAwRequestStrategy is null
	 */
	public AnnotatingValidator(AnnotateAwRequestStrategy annotatingValidationStrategy) {
		if(null == annotatingValidationStrategy) {
			throw new IllegalArgumentException("a AnnotateAwRequestStrategy is required");
		}
		
		_annotatingValidationStrategy = annotatingValidationStrategy;
	}
	
	protected AnnotateAwRequestStrategy getAnnotator() {
		return _annotatingValidationStrategy;
	}
}
