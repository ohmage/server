package org.ohmage.validator;

import org.ohmage.annotator.Annotator.ErrorCode;
import org.ohmage.domain.Probe;
import org.ohmage.exception.DomainException;
import org.ohmage.exception.ValidationException;
import org.ohmage.util.StringUtils;

/**
 * This class is responsible for all of the probe validators.
 *
 * @author John Jenkins
 */
public class ProbeValidators {
	/**
	 * Empty default constructor.
	 */
	private ProbeValidators() {}
	
	/**
	 * Validates that a probe definition is valid and creates a Probe from it.
	 * 
	 * @param definition The XML definition as a String.
	 * 
	 * @return The Probe represented by the definition or NULL if the
	 * 		   definition is null or only whitespace.
	 * 
	 * @throws ValidationException The probe definition was not valid.
	 */
	public static final Probe validateProbeDefinitionXml(
			final String definition)
			throws ValidationException {
		
		if(StringUtils.isEmptyOrWhitespaceOnly(definition)) {
			return null;
		}
		
		try {
			return new Probe(definition);
		}
		catch(DomainException e) {
			throw new ValidationException(
				ErrorCode.PROBE_INVALID_DEFINITION,
				e.getMessage(),
				e);
		}
	}
}
