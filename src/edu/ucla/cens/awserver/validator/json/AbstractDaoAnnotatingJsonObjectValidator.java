package edu.ucla.cens.awserver.validator.json;

import edu.ucla.cens.awserver.dao.Dao;
import edu.ucla.cens.awserver.validator.AwRequestAnnotator;

/**
 * Abstract base class for JSONValidators that require access to both a DAO and an Annotator for validation.
 * 
 * @author selsky
 */
public abstract class AbstractDaoAnnotatingJsonObjectValidator extends AbstractAnnotatingJsonObjectValidator {
	private Dao _dao;
		
	/**
	 * @throws IllegalArgumentException if the dao is null 
	 */
	public AbstractDaoAnnotatingJsonObjectValidator(AwRequestAnnotator annotator, Dao dao) {
		super(annotator);
		if(null == dao) {
			throw new IllegalArgumentException("dao cannot be null");
		}
		_dao = dao;
	}
	
	protected Dao getDao() {
		return _dao;
	}
}
