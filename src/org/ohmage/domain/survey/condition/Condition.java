package org.ohmage.domain.survey.condition;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.ohmage.domain.exception.InvalidArgumentException;
import org.ohmage.domain.survey.SurveyItem;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * <p>
 * The root class for all conditions.
 * </p>
 *
 * @author John Jenkins
 */
public class Condition {
    /**
     * The sentence string.
     */
    @JsonIgnore
    private final String sentence;
    /**
     * The root of the sentence tree.
     */
    @JsonIgnore
    private final Fragment root;

    /**
     * Creates a new condition.
     *
     * @param input
     *        The condition string that needs to be parsed.
     *
     * @throws InvalidArgumentException
     *         The input is null or not a valid condition.
     */
    public Condition(final String input) throws InvalidArgumentException {
        this(input, 0);

        // Verify that the parenthesis match.
        if(
            StringUtils.countOccurrencesOf(input, "(") !=
            StringUtils.countOccurrencesOf(input,  ")")) {

            throw
                new InvalidArgumentException(
                    "Parenthetical missmatch: " + input);
        }
    }

    /**
     * Parses the input and starting from some position and continuing until
     * the end of the string or an end parenthesis is reached.
     *
     * @param input
     *        The string to process.
     *
     * @param startPosition
     *        Where to begin processing.
     */
    private Condition(final String input, final int startPosition) {
        // Validate the input.
        if(input == null) {
            throw new InvalidArgumentException("The input is null.");
        }

        // Create a handler for the sentence. Once we are done parsing, we will
        // create a sub-string, which this will represent.
        String sentence = null;

        // Tokenize the string into Fragments.
        int pos;
        char currChar;
        int length = input.length();
        StringBuilder wordBuilder = new StringBuilder();
        List<Fragment.Builder<?>> clauses =
            new LinkedList<Fragment.Builder<?>>();
        for(pos = startPosition; pos < length; pos++) {
            // Get the current character.
            currChar = input.charAt(pos);

            // Validate the character.
            switch(currChar) {
                // If we are starting a sub-condition, parse it.
                case Parenthetical.START:
                    // If we were in the process of building a word, end the
                    // word and add it to the list of clauses.
                    if(wordBuilder.length() > 0) {
                        clauses
                            .add(Fragment.parseWord(wordBuilder.toString()));
                        wordBuilder = new StringBuilder();
                    }

                    // Parse the sub-condition, wrap it in a parenthetical, and
                    // add it as a clause.
                    Condition subCondition = new Condition(input, pos + 1);
                    Parenthetical.Builder parentheticalBuilder =
                        new Parenthetical.Builder(subCondition);
                    clauses.add(parentheticalBuilder);

                    // Move the cursor by the size of the sub-condition, then
                    // subtract one because the "for" above will add it right
                    // back.
                    pos += subCondition.getLength() - 1;
                    break;

                // We are a sub-condition and are ending.
                case Parenthetical.END:
                    // If we were in the process of building a word, end the
                    // word and add it to the list of clauses.
                    if(wordBuilder.length() > 0) {
                        clauses
                            .add(Fragment.parseWord(wordBuilder.toString()));
                        wordBuilder = new StringBuilder();
                    }

                    // Create the sub-sentence based on the parenthetical that
                    sentence =
                        input
                            .substring(
                                // If this wasn't the start of the string, step
                                // back and catch the parenthesis.
                                (startPosition == 0) ? 0 : startPosition - 1,
                                // The sub-string function cuts off this
                                // character, but it is desired.
                                pos + 1);
                    break;

                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    if(wordBuilder.length() > 0) {
                        clauses
                            .add(Fragment.parseWord(wordBuilder.toString()));
                        wordBuilder = new StringBuilder();
                    }
                    break;

                default:
                    wordBuilder.append(currChar);
                    break;
            }

            // Break out if we have reached an end parenthetical.
            if(sentence != null) {
                break;
            }
        }

        // If we were building a word when we ended, be sure to add it.
        if(wordBuilder.length() > 0) {
            clauses
                .add(Fragment.parseWord(wordBuilder.toString()));
            wordBuilder = new StringBuilder();
        }

        // If we are the root processor then the sentence is the whole
        // sentence.
        if(sentence == null) {
            this.sentence = input;
        }
        // Otherwise, we need to store our sub-sentence.
        else {
            this.sentence = sentence;
        }

        // Build the root by processing the fragments.
        Fragment.Builder<?> rootFragmentBuilder = null;

        // Use each of the builders to construct a tree.
        for(Fragment.Builder<?> currFragment : clauses) {
            if(rootFragmentBuilder == null) {
                rootFragmentBuilder = currFragment;
            }
            else {
                rootFragmentBuilder = rootFragmentBuilder.merge(currFragment);
            }
        }

        // Build the tree and set it as the root.
        root = rootFragmentBuilder.build();
    }

    /**
     * Returns the sentence.
     *
     * @return The sentence.
     */
    @JsonValue
    public String getSentence() {
        return sentence;
    }

    /**
     * Returns the length of the string that was parsed for this condition.
     *
     * @return The length of the string that was parsed for this condition.
     */
    public int getLength() {
        return sentence.length();
    }

    /**
     * Validates that the condition is a valid condition based on the prompts
     * that have been seen.
     *
     * @param surveyItems
     *        The map of survey item IDs to their {@link SurveyItem}.
     */
    public void validate(final Map<String, SurveyItem> surveyItems) {
        root.validate(surveyItems);
    }

    /**
     * Evaluates whether or not a prompt with this condition should be
     * displayed.
     *
     * @param responses
     *        The previous responses.
     *
     * @return True if this condition would
     */
    public boolean evaluate(final Map<String, Object> responses) {
       return root.evaluate(responses);
    }
}