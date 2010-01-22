package edu.ucla.cens.awserver.service;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * A Service with access to both a DAO and an AwRequestAnnotator.
 * 
 * @author selsky
 */
public abstract class AbstractAnnotatingDaoService extends AbstractDaoService {
	private AwRequestAnnotator _awRequestAnnotator;
	
	/**
	 * @throws IllegalArgumentException if the AwRequestAnnotator is null
	 */
	public AbstractAnnotatingDaoService(Dao dao, AwRequestAnnotator awRequestAnnotator) {
		super(dao);
		if(null == awRequestAnnotator) {
			throw new IllegalArgumentException("an awRequestAnnotator is required");
		}
		_awRequestAnnotator = awRequestAnnotator;
	}
	
	protected AwRequestAnnotator getAnnotator() {
		return _awRequestAnnotator;
	}
}
