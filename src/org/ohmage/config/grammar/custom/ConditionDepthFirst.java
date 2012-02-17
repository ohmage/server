/*******************************************************************************
 * Copyright 2012 The Regents of the University of California
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.ohmage.config.grammar.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.ohmage.config.grammar.syntaxtree.NodeToken;
import org.ohmage.config.grammar.syntaxtree.condition;
import org.ohmage.config.grammar.syntaxtree.id;
import org.ohmage.config.grammar.syntaxtree.value;
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
    public void visit(id n, A map) {
    	
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
    public void visit(condition n, A map) {
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
    public void visit(value n, A map) {
    	@SuppressWarnings("unchecked")
		List<ConditionValuePair> valueList = ((Map<String, List<ConditionValuePair>>) map).get(currentId);
    	currentPair.setValue(n.f0.tokenImage);
    	valueList.add(currentPair);
    	
        n.f0.accept(this, map);
    }
}
