/*******************************************************************************
 * Copyright 2011 The Regents of the University of California
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
package org.ohmage.util;

import java.lang.reflect.Method;

/**
 * Utilities for Reflection.
 * 
 * @author selsky
 */
public class ReflectionUtils {
	
	private ReflectionUtils() { } 
	
	/**
	 * @return an accessor method for the String value represented by instanceVariable (i.e., getInstanceVariableValue)
	 * @throws IllegalArgumentException if the accessor method does not exist on the provided class
	 * @throws IllegalArgumentException if a SecurityManager denied access to the accessor method
	 * @see java.lang.Class.getMethod(String) 
	 */
	public static Method getAccessorMethod(Class<?> clazz, String instanceVariableName) {
		String methodName = null;
		try {
			methodName = "get" + instanceVariableName.substring(0, 1).toUpperCase() + instanceVariableName.substring(1);
			return clazz.getMethod(methodName);
	    }
		catch (NoSuchMethodException nsme) {
			throw new IllegalArgumentException("method name " + methodName + " not found on AwRequest", nsme);
		} 
		catch (SecurityException se) {
			throw new IllegalArgumentException("a security manager disallowed access to method name " + methodName, se);
		}
		
	}
	
}
