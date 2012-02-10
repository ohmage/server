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
package org.ohmage.spring;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.core.Ordered;
import org.springframework.core.PriorityOrdered;

/**
 * Checks to make sure singleton instances have been created. This is done in
 * order to avoid runtime errors where a required component may have not been
 * initialized, but it is referenced within the application. This can happen
 * because we create components in Spring by reflectively invoking private
 * constructors. The idea is that this class runs on application startup to
 * check any injected singletons and then fails if it can't find them. This is 
 * far preferable to failing at runtime. The reason why there are singleton 
 * objects that are dynamically injected via Spring is in order to facilatate
 * mock objects for testing. Singletons enforce a small application footprint
 * and Spring allows us the flexibility (and the associated problems that come 
 * with it) to swap singletons for mock instances.  
 * 
 * @author Joshua Selsky
 */
public class ConfigurationSingletonValidityTester implements PriorityOrdered, ApplicationContextAware {
	private Logger logger = Logger.getLogger(ConfigurationSingletonValidityTester.class);
	private List<String> classNames;
	private List<String> classesToIgnore;
	private String[] packageDirectoryNames;
	
	/**
	 * Default constructor.
	 * 
	 * @param packageDirectoryNames  An array of package directory names that
	 * must be in the form org/ohmage/service where the leftmost directory
	 * is relative to the web apps classes dir.
	 * 
	 * @throws IllegalStateException  if an invalid directory is detected or if
	 * no classes are found in the directories.
	 * @throws IllegalArgumentException  if packageDirectoryNames is null or
	 * empty.
	 */
	public ConfigurationSingletonValidityTester(String[] packageDirectoryNames, List<String> classesToIgnore) {
		if(packageDirectoryNames == null || packageDirectoryNames.length == 0) {
			throw new IllegalArgumentException("An array of package directory names is required");
		}
		
		String classesDir = System.getProperty("webapp.root") + "WEB-INF/classes/";
		classNames = new ArrayList<String>();
		
		for(String packageDirectoryName : packageDirectoryNames) {
			File directory = new File(classesDir + packageDirectoryName);
			
			if(! directory.isDirectory()) {
				throw new IllegalStateException("Detected invalid directory name: " + directory);
			}
			
			File[] files = directory.listFiles();
			for(File file : files) {
				// Trim off the directory path before adding the class to the list
				classNames.add(file.getAbsolutePath().substring(classesDir.length()));
			}
		}
		
		if(classNames.isEmpty()) {
			throw new IllegalStateException("No classes to validate in the directories: " + Arrays.toString(packageDirectoryNames) 
				+ ". If you do not need to validate this configuration, remove this class from it.");
		}
		
		this.classesToIgnore = classesToIgnore;
		this.packageDirectoryNames = packageDirectoryNames;
	}
	
	/**
	 * Makes sure that the packages set on construction contain beans in the
	 * ApplicationContext that have been instantiated. Requires that the classes
	 * have a static instance() method to invoke.
	 * 
	 * @param applicationContext  The Spring ApplicationContext to query for
	 * class instance existence.
	 * 
	 * @throws IllegalStateException  if any class in classNames cannot be
	 * found in the ApplicationContext, does not have a static instance() 
	 * method, is not accessible due to security constraints, or if the 
	 * underlying instance() method throws an Exception.
	 */
	private void validate(ApplicationContext applicationContext) {
		Object tmp = null;
		Object[] noArgs = new Object[] {};
		
		for(String className : classNames) {
			try {
				String javaStyleClassName = className.substring(0, className.length() - 6).replace('/', '.');
				
				// Ignore inner classes
				if(javaStyleClassName.contains("$")) {
					if(logger.isDebugEnabled()) {
						logger.debug("Skipping inner class " + javaStyleClassName);
					}
					continue;
				}
				
				// Ignore classes that are ignorable
				if(classesToIgnore != null && classesToIgnore.contains(javaStyleClassName)) {
					if(logger.isDebugEnabled()) {
						logger.debug("Ignoring " + javaStyleClassName);
					}
					continue;
				}
				
				Class<?> clazz = Class.forName(javaStyleClassName);
				
				if(logger.isDebugEnabled()) {
					logger.debug("Sanity check for the existence of " + clazz + " in the ApplicationContext");
				}
				
				tmp = applicationContext.getBean(clazz);
				Method m = tmp.getClass().getDeclaredMethod("instance");
				
				// Invoke the static instance() method with no arguments.
				// If nothing is returned, the class is missing from the XML
				// config (i.e., no singleton exists in the application context 
				if(m.invoke(null, noArgs) == null) {
					throw new IllegalStateException("Invalid singleton configuration. No instance exists in the ApplicationContext"
						+ " for class " + className);
				}
			}
			catch (ClassNotFoundException e) {
				throw new IllegalStateException("Could not find class in ApplicationContext: " + className, e);
			}
			catch (BeansException e) {
				throw new IllegalStateException("Could not find bean in ApplicationContext for class: " + className , e);
			}
			catch (NoSuchMethodException e) {
				throw new IllegalStateException("No instance() method found on class: " + className, e);
			}
			catch (IllegalAccessException e) {
				throw new IllegalStateException("Could not access class: " + className, e);
			}
			catch (InvocationTargetException e) {
				throw new IllegalStateException("Exception thrown when invoking instance() on class: " + className, e.getCause());
			}
		}
		
		logger.info("Done with singleton instantion check for the following packages: " + Arrays.toString(packageDirectoryNames));
	}
	
	/**
	 * Provides Spring with hints about when to load this class.
	 */
	@Override
	public int getOrder() {
		// Highest precendence so this component is loaded last by Spring
		return Ordered.HIGHEST_PRECEDENCE; 
	}
	
	/**
	 * Performs validation against the ApplicationContext by checking for
	 * singleton instances in the package names provided on construction. It 
	 * is a bit odd to be performing the validation in a setter method, but
	 * the ApplicationContextAware interface specifies a setter and the context
	 * cannot be validated without it.
	 */
	public void setApplicationContext(ApplicationContext applicationContext) { 
		
		validate(applicationContext);
		
		// clean up because the validation only needs to happen one time
		classNames.clear();
		classNames = null;
		if(classesToIgnore != null) {
			classesToIgnore.clear();
			classesToIgnore = null;
		}
		logger = null;
	}
}
