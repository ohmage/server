package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * A Service with access to both a DAO and an AwRequestAnnotator.
 * 
 * @author selsky
 */
public abstract class AbstractAnnotatingService implements Service {
	private AwRequestAnnotator _awRequestAnnotator;
	
	/**
	 * @throws IllegalArgumentException if the AwRequestAnnotator is null
	 */
	public AbstractAnnotatingService(AwRequestAnnotator awRequestAnnotator) {
		if(null == awRequestAnnotator) {
			throw new IllegalArgumentException("an awRequestAnnotator is required");
		}
		_awRequestAnnotator = awRequestAnnotator;
	}
	
	protected AwRequestAnnotator getAnnotator() {
		return _awRequestAnnotator;
	}
}
