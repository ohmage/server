package org.ohmage.util;

import java.util.Collections;
import java.util.List;

/**
 * List utilities.
 * 
 * @author Joshua Selsky
 */
public final class ListUtils {
	
	/**
	 * Private to prevent instantiation.
	 */
	private ListUtils() { }
	
	/**
	 * Checks the provided stringList for duplicate items.
	 * 
	 * @param stringList  The list to check.
	 * @return true if the list contains duplicates, false otherwise.
	 */
	public static boolean containsDuplicates(List<String> stringList) {
		if(stringList == null) {
			return false;
		}
		
		if(stringList.size() == 1) {
			return false;
		}
		
		Collections.sort(stringList);
		
		int s = stringList.size();
		
		for(int i = 0; i < s; s++) {
			String a = stringList.get(i);
			if(i < s - 1) {
				if(stringList.get(i + 1).equals(a)) {
					return true;
				}
			}
		}
		
		return false;
	}
}
