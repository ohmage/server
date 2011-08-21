package org.ohmage.config.xml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.XMLConstants;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.ohmage.config.grammar.custom.ConditionParseException;
import org.ohmage.config.grammar.custom.ConditionValidator;
import org.ohmage.config.grammar.custom.ConditionValuePair;
import org.ohmage.util.StringUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Giant procedural driver for the campaign validation process.
 * 
 * @author Joshua Selsky
 * @author John Jenkins
 */
public class CampaignValidator {
	private static final Logger LOGGER = Logger.getLogger(CampaignValidator.class);
	private Map<String, PromptTypeValidator> promptTypeValidatorMap; // the map keys are the prompt ids in the input file
	private List<String> validDisplayTypes;
		
	public CampaignValidator() {
		promptTypeValidatorMap = new HashMap<String, PromptTypeValidator>();
		validDisplayTypes = new ArrayList<String>();

		validDisplayTypes.add("measurement");
		validDisplayTypes.add("event");
		validDisplayTypes.add("count");
		validDisplayTypes.add("category");
		validDisplayTypes.add("metadata");
	}
	
	/**
	 * args[0]: the file name of the file to validate
	 * args[1]: the file name of the schema to validate against
	 */
	public static void main(String[] args) throws IOException, SAXException, ParsingException, ValidityException {// Configure log4j. (pointing to System.out)
		// All Spring messages are at the DEBUG level. They are extremely 
		// informative (and verbose).
		BasicConfigurator.configure();
		
		if(args.length < 2) {
			throw new IllegalArgumentException("Invalid arguments: you must pass a file name of the file to be validated as " +
				"the first argument and a file name of the schema to use in validation as the second argument.");
		}
		
		String fileName = args[0];
		String schemaFileName = args[1];
		CampaignValidator validator = new CampaignValidator();
		
		try {
			
			validator.runAgainstFiles(fileName, schemaFileName);
			
		} catch(SAXParseException saxe) {
			
			LOGGER.error("Parsing failed at line number " + saxe.getLineNumber() + " column number " + saxe.getColumnNumber());
			throw saxe;
			
		}
	}
	
	/**
	 * Validates an XML String that is already in memory against a schema and
	 * against our campaign rules.
	 * 
	 * @param xml A String that is the XML to be validated.
	 * 
	 * @param schemaFileName The filename of the schema to validate against.
	 * 
	 * @throws SAXException Thrown if there is a problem validating the XML
	 * 						schema.
	 * 
	 * @throws IllegalStateException Thrown if there is a serious internal
	 * 									 error.
	 * 
	 * @throws ValidityException Thrown if 'xml' is not valid XML.
	 * 
	 * @throws ParsingException Thrown if the 'xml' is not well-formed.
	 * 
	 * @throws IllegalStateException Thrown if a specific part of the XML
	 * 								 fails validation. See the exception's 
	 * 								 message for more information.
	 * 
	 * @throws IllegalArgumentException Thrown if a specific part of the XML
	 * 									fails validation. See the exception's
	 * 									message for more information. 
	 * 
	 * @throws ConditionParseException Thrown if a condition is invalid.
	 */
	public void run(String xml, String schemaFileName) throws SAXException, ValidityException, ParsingException, ConditionParseException {
		LOGGER.info("Starting validation.");
		
		try {
			checkSchemaOnStrings(xml, schemaFileName);
		}
		catch(IOException e) {
			LOGGER.error("Failed to open schema to validate campaign.", e);
			throw new IllegalStateException("Problem reading schema file.");
		}
		LOGGER.info("schema validation successful");
		
		// Now use XOM to retrieve a Document and a root node for further processing. XOM is used because it has a 
		// very simple XPath API	
		Builder builder = new Builder();
		Document document;
		try {
			document = builder.build(new StringReader(xml));
		} catch (IOException e) {
			// This should only be thrown if it can't read the 'xml', but
			// given that it is already in memory this should never happen.
			LOGGER.error("Unable to read 'xml'.", e);
			throw new IllegalStateException("XML was unreadable.");
		}
		
		Element root = document.getRootElement();
		
		checkCampaignUrn(root);
		LOGGER.info("campaignUrn is a valid URN");
		
		checkIdUniqueness(root);
		LOGGER.info("id uniqueness check successful: all ids in the configuration are unique");
		
		checkPromptTypes(root);
		LOGGER.info("prompt type check successful: all prompts have valid prompt types");
		
		checkPromptTypeProperties(root);
		LOGGER.info("prompt property configuration check successful: all prompts have valid configurations for their respective types");
				
		checkConditions(root);		
		LOGGER.info("conditions check successful: all conditions are valid");
		
		checkDefaults(root);		
		LOGGER.info("defaults check successful: all default values are valid");
		
		checkSurveySpecialRules(root);
		LOGGER.info("surveys check successful: all special survey config rules passed");
		
		checkRepeatableSetSpecialRules(root);
		LOGGER.info("repeatableSets check successful: all special repeatableSet config rules passed");
		
		checkPromptSpecialRules(root);
		LOGGER.info("prompts check successful: all special prompt config rules passed");
		
		checkDisplayTypes(root);
		LOGGER.info("displayType check successful: all displayTypes are valid");
		
		LOGGER.info("configuration validation successful");
	}
	
	
	/**
	 * Validates an XML String that is already in memory against a schema and
	 * against our campaign rules.
	 * 
	 * @param xml A String that is the XML to be validated.
	 * 
	 * @param schemaFileName The filename of the schema to validate against.
	 * 
	 * @throws SAXException Thrown if there is a problem validating the XML
	 * 						schema.
	 * 
	 * @throws IllegalStateException Thrown if there is a serious internal
	 * 									 error.
	 * 
	 * @throws ValidityException Thrown if 'xml' is not valid XML.
	 * 
	 * @throws ParsingException Thrown if the 'xml' is not well-formed.
	 * 
	 * @throws IllegalStateException Thrown if a specific part of the XML
	 * 								 fails validation. See the exception's 
	 * 								 message for more information.
	 * 
	 * @throws IllegalArgumentException Thrown if a specific part of the XML
	 * 									fails validation. See the exception's
	 * 									message for more information. 
	 */
	public void runAgainstFiles(String fileName, String schemaFileName) throws IOException, SAXException, ParsingException, ValidityException {
		// Read the contents of fileName and save it in 'xml'.
		BufferedReader br = new BufferedReader(new FileReader(fileName));
		String currLine;
		String xml = "";
		while((currLine = br.readLine()) != null) {
			xml += currLine + "\n";
		}
		
		run(xml, schemaFileName);
	}
	
	/**
	 * Checks that the campaign URN exists and is a valid URN as defined by
	 * us.
	 * 
	 * @param root The root of the XML being validated.
	 * 
	 * @throws IllegalStateException Thrown if the URN fails validation.
	 */
	private void checkCampaignUrn(Node root) {
		String campaignUrn = root.query("/campaign/campaignUrn").get(0).getValue().trim();
		if(! StringUtils.isValidUrn(campaignUrn)) {
			throw new IllegalStateException("campaignUrn is not a valid URN: " + campaignUrn);
		}
	}
	
	/**
	 * Checks that configured default values are valid for their associated prompt types. Assumes that promptTypeValidatorMap
	 * has been correctly populated (i.e., that prompt types have been successfully validated).
	 */
	private void checkDefaults(Node root) {
		Nodes prompts = root.query("//prompt"); // get all prompts
		int size = prompts.size();
		
		for(int i = 0; i < size; i++) {
			
			Nodes defaultNodes = prompts.get(i).query("default");
			if(defaultNodes.size() > 0) {
				String promptId = prompts.get(i).query("id").get(0).getValue().trim();
				PromptTypeValidator ptv = promptTypeValidatorMap.get(promptId);
				ptv.checkDefaultValue(defaultNodes.get(0).getValue().trim());
			}
		}
	}
	
	/**
	 * Checks that any displayType values are valid.
	 */
	private void checkDisplayTypes(Node root) {
		Nodes surveys = root.query("//survey"); // get all surveys
		int numberOfSurveys = surveys.size();
		
		for(int i = 0; i < numberOfSurveys; i++) {
			Nodes prompts = surveys.get(i).query("contentList/prompt | contentList/repeatableSet/prompts/prompt");
			int numberOfPrompts = prompts.size();
			int numberOfMetadataTimestamps = 0;
			
			for(int j = 0; j < numberOfPrompts; j++) {
				Nodes displayTypeNodes = prompts.get(j).query("displayType");
				
				if(displayTypeNodes.size() > 0) {
					
					String dt = displayTypeNodes.get(0).getValue().trim();
					if(! validDisplayTypes.contains(dt)) {
						throw new IllegalArgumentException("invalid display type: " + dt);
					}
					
					String pt = prompts.get(j).query("promptType").get(0).getValue().trim();
					if("timestamp".equals(pt) && "metadata".equals(dt)) {
						numberOfMetadataTimestamps++;
					}
				}
			}
			if(numberOfMetadataTimestamps > 1) {
				LOGGER.warn("more than one metadata timetamp found for survey with id: "
					+ surveys.get(i).query("id").get(0).getValue().trim());
			}
		}
	}
	
	
	/**
	 * Validates dependencies between elements in each survey.
	 */
	private void checkSurveySpecialRules(Node root) {
		Nodes surveys = root.query("//survey"); // get all surveys
		int size = surveys.size();
		
		for(int i = 0; i < size; i++) {
				
			if(Boolean.valueOf(surveys.get(i).query("showSummary").get(0).getValue().trim())) { // summaryText and editSummary must exist
				
				// the schema specifies a non-empty string if summaryText exists, so just check for its existence
				if(surveys.get(i).query("summaryText").size() < 1) {
					
					throw new IllegalStateException("Invalid survey config for survey id " 
						+ surveys.get(i).query("id").get(0).getValue().trim() + ". summaryText is required if showSummary is true");
				}
				
				// the schema specifies a boolean if editSummary exists, so just check for its existence
				if(surveys.get(i).query("editSummary").size() < 1) {
					
					throw new IllegalStateException("Invalid survey config for survey id " 
						+ surveys.get(i).query("id").get(0).getValue().trim() + ". editSummary is required if showSummary is true");
				}
			}
		}
	}
	
	/**
	 * Validates dependencies between elements in each repeatableSet.
	 */
	private void checkRepeatableSetSpecialRules(Node root) {
		Nodes repeatableSets = root.query("//repeatableSet");
		int size = repeatableSets.size();
		
		for(int i = 0; i < size; i++) {
			
			if(Boolean.valueOf(repeatableSets.get(i).query("terminationSkipEnabled").get(0).getValue().trim()) && // terminationSkipLabel must exist
					(repeatableSets.get(i).query("terminationSkipLabel").size() < 1)) { 
				
				throw new IllegalStateException("Invalid repeatableSet config for repeatableSet id " 
						+ repeatableSets.get(i).query("id").get(0).getValue().trim() + ". terminationSkipLabel is required if "
						+ "terminationSkipEnabled is true");
			}	
		}
	}
	
	
	/**
	 * Validates dependencies between elements in each prompt and dependencies between a prompt and its parent repeatableSet or 
	 * survey.
	 */
	private void checkPromptSpecialRules(Node root) {
		Nodes prompts = root.query("//prompt");
		int size = prompts.size();
		
		for(int i = 0; i < size; i++) {
			
			if(Boolean.valueOf(prompts.get(i).query("skippable").get(0).getValue().trim()) &&
					(prompts.get(i).query("skipLabel").size() < 1)) { // skipLabel must exist
				
				throw new IllegalStateException("Invalid prompt config for prompt id " 
						+ prompts.get(i).query("id").get(0).getValue().trim() + ". skipLabel is required if "
						+ "skippable is true");
			}
			
			boolean showSummary = false;
			
			// check showSummary on the parent survey
			if("survey".equals(((Element) prompts.get(i).getParent().getParent()).getLocalName())) {
				
				showSummary = Boolean.valueOf(prompts.get(i).getParent().getParent().query("showSummary").get(0).getValue().trim());
				
				
			} else { // the parent is a repeatableSet so unwind 4 levels. the backwards path is prompt/prompts/repeatableSet/content_list/survey 
				
				showSummary = Boolean.valueOf(prompts.get(i).query("../../../..").get(0).query("showSummary").get(0).getValue().trim());
			}
			
			if(showSummary && (prompts.get(i).query("abbreviatedText").size() < 1)) {

				throw new IllegalStateException("Invalid prompt config for prompt id " 
						+ prompts.get(i).query("id").get(0).getValue().trim() + ". abbreviatedText is required if "
						+ "showSummary on the parent survey is true");
			}
		}
	}

	/**
	 * Validates a campaign's schema.
	 * 
	 * @param xml The campaign that will have its schema validated.
	 * 
	 * @param schema The schema used to validate the campaign XML.
	 * 
	 * @throws IOException Thrown if the schema file cannot be found or read.
	 * 
	 * @throws SAXException Thrown if the schema validation fails.
	 */
	private void checkSchemaOnStrings(String xml, String schemaFileName) throws IOException, SAXException {
		SAXSource xmlSource = new SAXSource(new InputSource(new StringReader(xml)));
		StreamSource schemaDocument = new StreamSource(new File(schemaFileName));
		
		// Originally attempted to use "http://www.w3.org/XML/XMLSchema/v1.1" here, but neither Xerces2 nor the native
		// Java 6 implementation supports it out of the box.  
		SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
		
		Schema s = sf.newSchema(schemaDocument);
		Validator v = s.newValidator();
		v.validate(xmlSource);
	}
	
	/**
	 * Checks each id in the instance document for uniqueness.
	 * 
	 * @throws IllegalStateException Thrown if there were duplicate ids.
	 */
	private void checkIdUniqueness(Element root) {
		Nodes idNodes = root.query("//id"); // find all of the id elements in the file
		int size = idNodes.size();
		
		Set<String> stringSet = new HashSet<String>(size);
		
		for(int i = 0; i < size; i++) {
			String value = idNodes.get(i).getValue().trim();
			if(! stringSet.add(value)) { // if add() returns false, it means there is a duplicate in the list
				
				throw new IllegalStateException("Invalid configuration: a duplicate id was found: " + value);
				
			}
		}
	}

	/**
	 * Checks that each condition conforms to our grammar, contains ids that are allowable (conditions can only refer to prompts
	 * or repeatable sets that occur before the current condition and within the current survey), and contains values that are valid
	 * for the prompt type represented by the id.
	 * 
	 * @throws ConditionParseException Thrown if a condition is not well 
	 * 								   formed.
	 */
	private void checkConditions(Element root) throws ConditionParseException {
		Nodes surveys = root.query("//survey"); // get all surveys
		int numberOfSurveys = surveys.size();
		
		// Now check the conditions within each survey
		for(int x = 0; x < numberOfSurveys; x++) {
			
			Node survey = surveys.get(x);
			Nodes contentList = survey.query("contentList");
			
			int numberOfItemsInContentList = contentList.size();
			
			for(int y = 0; y < numberOfItemsInContentList; y++) {
				// Content lists can contain conditions in prompts, repeatable sets, and prompts in repeatable sets 
				
				Nodes promptsAndRepeatableSets = contentList.get(y).query("prompt | repeatableSet");
				int numberOfOuterElements = promptsAndRepeatableSets.size();
				List<String> idList = new ArrayList<String>();
				
				for(int outerIndex = 0; outerIndex < numberOfOuterElements; outerIndex++) {
					
					Node currentNode = promptsAndRepeatableSets.get(outerIndex);
					String currentId = currentNode.query("id").get(0).getValue().trim(); 
					idList.add(currentId);
					int currentIdIndex = idList.indexOf(currentId);
					
					String currentNodeType = ((Element) currentNode).getLocalName();
					
					if("prompt".equals(currentNodeType)) {
						
						LOGGER.info("checking for a condition for prompt: " + currentId);
						validateCondition(currentNode, outerIndex, currentId, currentIdIndex, idList);
						
					} else { 
						
						 LOGGER.info("checking a for a condition for repeatableSet: " + currentId);
						 validateCondition(currentNode, outerIndex, currentId, currentIdIndex, idList);
						 
						 // Now check out each prompt in the repeatable set
						 Nodes repeatableSetPromptNodes = currentNode.query("prompts/prompt");
						 int numberOfInnerElements = repeatableSetPromptNodes.size();
						 
						 List<String> cumulativeIdList = new ArrayList<String>();
						 cumulativeIdList.addAll(idList);  // copy the Id list so it is not changed in the inner loop
						 // FIXME: Sonar pointed out that this is not being 
						 // anywhere however there is a similarly named
						 // 'cumulativeIdIndex'. I am leaving this comment here
						 // to suggest to someone to check this out and make
						 // sure the code below is correct.
						 //int cumulativeIndex = outerIndex; // make sure not to increment the outer index
						 						 
						 for(int i = 0; i < numberOfInnerElements; i++/*, cumulativeIndex++*/) {
							 Node currentInnerNode = repeatableSetPromptNodes.get(i);
							 String currentInnerId = currentInnerNode.query("id").get(0).getValue().trim();
							 LOGGER.info("checking condition for a prompt inside of a repeatableSet: " + currentInnerId);
							 cumulativeIdList.add(currentInnerId);
							 int cumulativeIdIndex = cumulativeIdList.indexOf(currentInnerId);
							 
							 validateCondition(currentInnerNode, cumulativeIdIndex, currentInnerId, cumulativeIdIndex, cumulativeIdList);
						 }
					}
				}
			}
		}
	}
	
	/**
	 * Validates conditions: checks grammar adherence, checks that ids exist and are for previous prompts, and checks that 
	 * values are valid for the prompt type of the id on the left-hand side of expressions.
	 */
	private void validateCondition(Node currentNode, int surveyIndex, String currentId, int currentIdIndex, List<String> idList) {
		Nodes conditionNodes = currentNode.query("condition");
		
		if(conditionNodes.size() > 0) { // conditions are optional
			
			String condition = conditionNodes.get(0).getValue().trim();
			
			if(! "".equals(condition)) { // don't validate an empty node
				
				LOGGER.info("validating condition [id: " + currentId + "][condition: " + condition + "]");
				
				if(0 == surveyIndex) {
					throw new IllegalArgumentException("a condition is not allowed on the first prompt of a " +
						"survey. invalid prompt id: " + currentId);
				}
				
				try {
					// check condition syntax
					Map<String, List<ConditionValuePair>> idPairsMap = ConditionValidator.validate(condition);
					
					// check each id to make sure it references a prompt previous to the current prompt
					Set<String> keySet = idPairsMap.keySet();
					Iterator<String> keySetIterator = keySet.iterator();
					
					while(keySetIterator.hasNext()) {
						String key = keySetIterator.next();
						LOGGER.info("validating condition value for condition id " + key);
						int keyIndex = idList.indexOf(key); 
						
						if(keyIndex >= currentIdIndex || keyIndex == -1) {
							throw new IllegalStateException("invalid id in condition for prompt id: " + currentId);
						}
						
						// check each condition-value pair
						List<ConditionValuePair> pairs = idPairsMap.get(key);
						PromptTypeValidator promptTypeValidator = promptTypeValidatorMap.get(key);
						
						for(ConditionValuePair pair : pairs) {
							promptTypeValidator.validateConditionValuePair(pair);
						}
					}
					
				} catch (ConditionParseException cpe) {
					
					LOGGER.info("invalid condition at id: " + currentId);
					throw cpe;
				}
				
			} else {
				
				LOGGER.info("no condition found");
			}
		} else {
			
			LOGGER.info("no condition found");
		}
	}
	
	
	/**
	 * Checks all prompt types to make sure that they are supported.
	 * 
	 * @throws IllegalStateException Thrown if a prompt type was found that
	 * 								 the server doesn't know about.
	 */
	private void checkPromptTypes(Element root) {
		Nodes promptTypeNodes = root.query("//promptType");
		List<String> stringList = nodesToValueList(promptTypeNodes);
		
		for(String string : stringList) {
			if(! PromptTypeValidatorFactory.isValidPromptType(string)) {
				throw new IllegalStateException("Invalid configuration: an unknown prompt type was found: " + string);
			}
		}
	}
	
	/**
	 * Checks all prompt types to make sure that they have valid
	 * configurations.
	 * 
	 * @throws IllegalStateException Thrown if the properties of a prompt
	 * 								 don't successfully validate.
	 */
	private void checkPromptTypeProperties(Element root) {
		Nodes prompts = root.query("//prompt");
		int size = prompts.size();
		
		for(int i = 0; i < size; i++) {
			// Get the prompt type and then validate the configured properties
			Node promptNode = prompts.get(i);
			String promptId = promptNode.query("id").get(0).getValue().trim();
			LOGGER.info("validating property configuration for prompt id: " + promptId);
			String promptType = promptNode.query("promptType").get(0).getValue().trim();
			PromptTypeValidator v = PromptTypeValidatorFactory.getValidator(promptType);
			v.validateAndSetConfiguration(promptNode);
			// add the validator for use in validating condition values
			promptTypeValidatorMap.put(promptId, v);
		}	
	}
	
	/**
	 * Converts the provided XOM Nodes to a List of values contained in each Node in the Nodes. The XOM API is outdated and 
	 * its Nodes class is not a java.util.List (or some other standard collection).
	 * 
	 * This method can only be used if the Nodes represent Text nodes (instead of say, elements with child elements).
	 */
	private static List<String> nodesToValueList(Nodes nodes) {
		if(null == nodes) {
			return null; // TODO should be an empty immutable list
		}
		
		int size = nodes.size();
		List<String> stringList = new ArrayList<String>();
		
		for(int i = 0; i < size; i++) {
			stringList.add(nodes.get(i).getValue().trim());
		}
		
		return stringList;
	}
}
