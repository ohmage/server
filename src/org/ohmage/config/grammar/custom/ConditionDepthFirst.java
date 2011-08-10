package org.ohmage.config.grammar.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ohmage.config.grammar.syntaxtree.Condition;
import org.ohmage.config.grammar.syntaxtree.Id;
import org.ohmage.config.grammar.syntaxtree.NodeToken;
import org.ohmage.config.grammar.syntaxtree.Value;
import org.ohmage.config.grammar.visitor.GJVoidDepthFirst;

/**
 * Simple visitor for adding condition ids and their associated values to a Map.
 * 
 * @author selsky
 * @param <A> must be a Map<String, List<String>>
 */
public class ConditionDepthFirst<A> extends GJVoidDepthFirst<A> {
	private String currentId;
	private ConditionValuePair currentPair;
	
    /**
     * f0 -> <TEXT>
     * 
     * Adds an entry for the Id token into the provided map, if the entry doesn't already exist. 
     * map must be a non-null Map<String, List<String>>.
     */
    @Override
    public void visit(Id n, A map) {
    	
    	// Lazy null check only occurs on the Id node because if it is non-null here, it will be non-null throughout the rest
    	// of the visitor process
    	if(! (map instanceof Map<?, ?>)) {
    		throw new IllegalArgumentException("argu parameter must be a Map");
    	}
    	
    	String tokenImage = n.f0.tokenImage;
    	currentId = tokenImage;
    	
    	@SuppressWarnings("unchecked")
		Map<String, List<String>> idValueMap = (Map<String, List<String>>) map; 
    	if(! idValueMap.containsKey(tokenImage)) {
    		idValueMap.put(tokenImage, new ArrayList<String>());
    	}
    	
        n.f0.accept(this, map);
    }
    
    /**
     * f0 -> "=="
     *       | "!="
     *       | "<"
     *       | ">"
     *       | "<="
     *       | ">="
     */
    public void visit(Condition n, A map) {
        String tokenImage = (((NodeToken) n.f0.choice).tokenImage); // ugly cast, but it's the only way to get the Value
        ConditionValuePair pair = new ConditionValuePair();
        pair.setCondition(tokenImage);
        currentPair = pair;
         
        n.f0.accept(this, map);
    }
    
    /**
     * f0 -> <TEXT>
     * 
     * Adds an entry for the Value token to the List retrieved using the current Id (the last Id token seen during 
     * the parse) from the provided map.
     * argu must be a non-null Map. 
     */
    public void visit(Value n, A map) {
    	@SuppressWarnings("unchecked")
		List<ConditionValuePair> valueList = ((Map<String, List<ConditionValuePair>>) map).get(currentId);
    	currentPair.setValue(n.f0.tokenImage);
    	valueList.add(currentPair);
    	
        n.f0.accept(this, map);
    }
}