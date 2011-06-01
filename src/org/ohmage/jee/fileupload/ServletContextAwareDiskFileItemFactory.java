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
package org.ohmage.jee.fileupload;

import javax.servlet.ServletContext;

import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.FileCleanerCleanup;
import org.apache.commons.io.FileCleaningTracker;
import org.springframework.web.context.ServletContextAware;

/**
 * Mash-up class to make Commons IO aware of the ServletContext for management of temp files created by Commons FileUpload.
 * 
 * @author selsky
 */
public class ServletContextAwareDiskFileItemFactory extends DiskFileItemFactory implements ServletContextAware {
	// private static Logger _logger = Logger.getLogger(ServletContextAwareDiskFileItemFactory.class);
	
	public void setSizeThreshold(int numberOfBytes) {
		super.setSizeThreshold(numberOfBytes);
	}
	
	@Override
	public void setServletContext(ServletContext servletContext) {
		FileCleaningTracker fct = FileCleanerCleanup.getFileCleaningTracker(servletContext);
		setFileCleaningTracker(fct);
	}
}
