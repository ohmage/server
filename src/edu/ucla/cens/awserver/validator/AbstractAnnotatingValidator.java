package edu.ucla.cens.awserver.validator;

/**
 * Abstract base class for Validators which need to annotate an AwRequest as part of their processing.
 * 
 * @author selsky
 */
public abstract class AbstractAnnotatingValidator implements Validator {
	private AwRequestAnnotator _annotator;
	
	/**
	 * @throws IllegalArgumentException if the provided AnnotateAwRequestStrategy is null
	 */
	public AbstractAnnotatingValidator(AwRequestAnnotator annotator) {
		if(null == annotator) {
			throw new IllegalArgumentException("a non-null annotator is required");
		}
		
		_annotator = annotator;
	}
	
	protected AwRequestAnnotator getAnnotator() {
		return _annotator;
	}
}
