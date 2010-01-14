package edu.ucla.cens.awserver.validator;

import edu.ucla.cens.awserver.dao.Dao;

/**
 * Abstract base class for Validators that require access to both a DAO and an AnnotateAwRequestStrategy for validation.
 * 
 * @author selsky
 */
public abstract class AbstractDaoAnnotatingValidator implements Validator {
	private Dao _dao;
	private AwRequestAnnotator _annotator;
		
	/**
	 * @throws IllegalArgumentException if annotateStrategy is null
	 * @throws IllegalArgumentException if the dao is null 
	 */
	public AbstractDaoAnnotatingValidator(AwRequestAnnotator annotator, Dao dao) {
		if(null == annotator) {
			throw new IllegalArgumentException("annotateAwRequestStrategy cannot be null");
		}
		if(null == dao) {
			throw new IllegalArgumentException("dao cannot be null");
		}
		
		_annotator = annotator;
		_dao = dao;
	}
	
	protected Dao getDao() {
		return _dao;
	}

	protected AwRequestAnnotator getAnnotator() {
		return _annotator;
	}
}
