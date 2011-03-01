package edu.ucla.cens.awserver.domain;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;

/**
 * Builder class that converts XML into a List of Surveys.
 * 
 * @author selsky
 */
public class SurveyMapFromXmlBuilder implements SurveyMapBuilder {
	// private static Logger _logger = Logger.getLogger(SurveyMapFromXmlBuilder.class);
	
	/**
	 * Creates a List of Surveys from the provided String that must be valid XML according to configuration.xsd. This validity
	 * is assumed because the configuration has been successfully loaded into the system.
	 */
	@Override
	public Map<String, Survey> buildFrom(String string) {
		Builder builder = new Builder();
		Document document = null;
		
		try {
			document = builder.build(new StringReader(string));
		}
		catch (IOException ioe) {
			throw new IllegalArgumentException("invalid configuration XML", ioe);
		}
		catch (ParsingException pe) {
			throw new IllegalArgumentException("invalid configuration XML", pe);
		}
		
		Element root = document.getRootElement();
		Nodes surveyNodes = root.query("//survey");
		int numberOfSurveys = surveyNodes.size();
		Map<String, Survey> surveyMap = new HashMap<String, Survey>();
		
		for(int i = 0; i < numberOfSurveys; i++) {
			Map<String, SurveyItem> surveyItemMap = new HashMap<String, SurveyItem>();
			
			String surveyId = surveyNodes.get(i).query("id").get(0).getValue();
			
			// first, grab the prompts
			Nodes nodes = surveyNodes.get(i).query("contentList/prompt | contentList/repeatableSet/prompts");
			int numberOfPromptsAndRepeatableSets = nodes.size();
			
			for(int j = 0; j < numberOfPromptsAndRepeatableSets; j++) {
				
				String nodeType = ((Element) nodes.get(j)).getLocalName();
				
				if("prompts".equals(nodeType)) { // a list of prompts from a repeatableSet
					Node node = nodes.get(j);
					String repeatableSetId = node.query("../id").get(0).getValue();
					Nodes promptNodes = node.query("prompt");
					int numberOfPrompts = promptNodes.size();
					Map<String, Prompt> promptMap = new HashMap<String, Prompt>();
					
					for(int k = 0; k < numberOfPrompts; k++) {
						Prompt p = getPromptFromNode(promptNodes.get(k));
						promptMap.put(p.getId(), p);
					}
					
					RepeatableSet rs = new RepeatableSet(repeatableSetId, promptMap);
					surveyItemMap.put(repeatableSetId, rs);
					
				} else { // it's a single prompt
					
					Prompt p = getPromptFromNode(nodes.get(j));
					surveyItemMap.put(p.getId(), p);
				}
			}
			
			Survey survey = new Survey(surveyId, surveyItemMap);
			surveyMap.put(surveyId, survey);
			// TODO make this a debug prop
			// _logger.info(survey);
			
		}
		
		return surveyMap;
	}
	
	/**
	 * Converts the provided XOM Node object into a Prompt object.
	 */
	private Prompt getPromptFromNode(Node node) {
		Nodes propNodes = node.query("properties/property");
		int numberOfPropNodes = propNodes.size();
		Map<String , PromptProperty> ppMap = new HashMap<String, PromptProperty>();
		
		for(int i = 0; i < numberOfPropNodes; i++) {
			Node propNode = propNodes.get(i);
			String key = propNode.query("key").get(0).getValue();
			boolean valueNodeExists = propNode.query("value").size() > 0;
			String value = valueNodeExists ? propNode.query("value").get(0).getValue() : null;
			String label = propNode.query("label").get(0).getValue();
			PromptProperty pp = new PromptProperty(key, value, label);
			ppMap.put(key, pp);
		}
		
		String id = node.query("id").get(0).getValue();
		String displayType = node.query("displayType").get(0).getValue();
		String displayLabel = node.query("displayLabel").get(0).getValue();
		String unit = null;
		Nodes nodes = node.query("unit");
		if(nodes.size() > 0) {
			unit = nodes.get(0).getValue();
		}
		
		String type = node.query("promptType").get(0).getValue();
		boolean skippable = Boolean.valueOf(node.query("skippable").get(0).getValue());
		
		return new Prompt(id, displayType, type, ppMap, skippable, displayLabel, unit);
	}
}
