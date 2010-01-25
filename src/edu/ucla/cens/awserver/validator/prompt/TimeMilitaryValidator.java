package edu.ucla.cens.awserver.validator.prompt;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.util.StringUtils;

/**
 * @author selsky
 */
public class TimeMilitaryValidator extends NullValidator {
	private static Logger _logger = Logger.getLogger(TimeMilitaryValidator.class);
	
	/**
	 * @return true if the response contains a valid military time
	 * @return false otherwise
	 */
	public boolean validate(String response) {
		if(super.validate(response)) {
			return true;
		}
		
		if(! StringUtils.isEmptyOrWhitespaceOnly(response)) {
			
			if(response.toCharArray().length != 5) {
				_logger.info("time military value is of incorrect length");
				return false;
			}
			
			String[] pieces = response.split(":");
			
			if(pieces.length != 2) {
				_logger.info("time military value is of incorrect length");
				return false;
			}
			
			int hours = 0;
			int minutes = 0;
			
			try {
				
				hours = Integer.parseInt(pieces[0]);
				
				//_logger.info(hours);
			
			} catch(NumberFormatException nfe) {
				
				_logger.info("time military value has an invalid hours element");
				return false;
			}
			
			try {
				
				minutes = Integer.parseInt(pieces[1]);
			
				//_logger.info(minutes);
				
			} catch(NumberFormatException nfe) {
				
				_logger.info("time military value has an invalid minutes element");
				return false;
			}
			
			if(hours < 0 || hours > 23) {
				_logger.info("time military value has an invalid hours element");
				return false;
			}
			
			if(minutes < 0 || minutes > 59) {
				_logger.info("time military value has an invalid minutes element");
				return false;
			}
			
			return true;
			
		}
		
		return false;
	}

}
