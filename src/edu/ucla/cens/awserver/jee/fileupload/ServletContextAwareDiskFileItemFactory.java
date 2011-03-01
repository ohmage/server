package edu.ucla.cens.awserver.jee.fileupload;

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
