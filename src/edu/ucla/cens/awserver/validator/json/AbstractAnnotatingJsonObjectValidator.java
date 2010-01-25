package edu.ucla.cens.awserver.validator.json;

import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Abstract base class for Validators which need to annotate an AwRequest as part of their processing.
 * 
 * @author selsky
 */
public abstract class AbstractAnnotatingJsonObjectValidator implements JsonObjectValidator {
	private AwRequestAnnotator _annotator;
	
	/**
	 * @throws IllegalArgumentException if the provided AnnotateAwRequestStrategy is null
	 */
	public AbstractAnnotatingJsonObjectValidator(AwRequestAnnotator annotator) {
		if(null == annotator) {
			throw new IllegalArgumentException("a non-null annotator is required");
		}
		
		_annotator = annotator;
	}
	
	protected AwRequestAnnotator getAnnotator() {
		return _annotator;
	}
}
