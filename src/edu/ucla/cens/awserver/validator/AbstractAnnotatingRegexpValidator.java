package edu.ucla.cens.awserver.validator;

import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import edu.ucla.cens.awserver.util.StringUtils;

public abstract class AbstractAnnotatingRegexpValidator extends AbstractAnnotatingValidator {
	protected Pattern _regexpPattern;
	
	/**
	 * Creates an instance of this class the provided regexp for validation.
	 *
	 * @throws IllegalArgumentException if the passed in regexp is null, empty, or all whitespace
	 * @throws PatternSyntaxException if the passed in regexp is invalid
	 */
	public AbstractAnnotatingRegexpValidator(String regexp, AwRequestAnnotator awRequestAnnotator) {
		super(awRequestAnnotator);
		
		if(StringUtils.isEmptyOrWhitespaceOnly(regexp)) {
			throw new IllegalArgumentException("a null, empty, or all-whitespace string is not allowed");
		}
		
		_regexpPattern = Pattern.compile(regexp);
	}
}
