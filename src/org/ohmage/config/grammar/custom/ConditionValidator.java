package org.ohmage.config.grammar.custom;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ohmage.config.grammar.parser.ConditionParser;
import org.ohmage.config.grammar.parser.ParseException;
import org.ohmage.config.grammar.syntaxtree.Start;

/**
 * A validator for conditions that relies on classes generated from JavaCC and JTB to parse and retrieve data from condition
 * sentences.  
 * 
 * @author selsky
 */
public final class ConditionValidator {
	private static boolean first = true;
	
	/**
	 * Prevent instantiation.
	 */
	private ConditionValidator() {
		
	}
	
	/**
	 * For command line use for testing. Provide the condition to be validated as the first argument. The condition must be a
	 * double-quoted string.
	 */
	public static void main(String args[]) throws IOException, ConditionParseException {
		Map<String, List<ConditionValuePair>> map = validate(args[0]);
		System.out.println(map);
	}
	
	/**
	 * Validates the provided Condition Sentence.
	 * 
	 * @param conditionSentence
	 * @return Map of Id-Value list pairs for each Id-operation-Value in the provided Sentence
	 * @throws ConditionParseException if the Sentence does not conform to our grammar (see spec/condition-grammar.jj) 
	 */
	@SuppressWarnings("static-access")
	public static Map<String, List<ConditionValuePair>> validate(String conditionSentence) {
		Start s = null;
		
		try {
			
			// TODO - fix the ConditionParser instantiation logic by rebuilding the parser and setting the JavaCC parameter STATIC
			// to false. Without the false setting, the weird ReInit logic below must occur.
			
			if(first) {
				
				first = false;
				s = new ConditionParser(new StringReader(conditionSentence)).start();
				
			} else {
				ConditionParser.ReInit(new StringReader(conditionSentence)); // ReInit must be called for a multiple parse scenario
				                                                             // i.e., when this method is called from a loop
				s = ConditionParser.start();
			}
			
			ConditionDepthFirst<Map<String, List<ConditionValuePair>>> visitor 
				= new ConditionDepthFirst<Map<String, List<ConditionValuePair>>>();
			Map<String, List<ConditionValuePair>>map = new HashMap<String, List<ConditionValuePair>>(); 
			visitor.visit(s, map);
			return map;
			
		} catch (ParseException pe) {
			
			throw new ConditionParseException("Condition parse failed for condition Sentence: " + conditionSentence, pe);
		}
		catch(Throwable e) {
			throw new ConditionParseException("The condition sentence is not well-formed: " + conditionSentence, e);
		}
	}
}
