package edu.ucla.cens.awserver.jee.servlet;

import java.io.IOException;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import edu.ucla.cens.awserver.controller.Controller;

/**
 * Idempotent DB query to test application configuration.
 * 
 * @author selsky
 */
@SuppressWarnings("serial") 
public class TestDbServlet extends HttpServlet {
	
	private static Logger logger = Logger.getLogger(TestDbServlet.class);
	private Controller controller;
			
	public TestDbServlet() {
		logger.info("Created.");
	}
		
	public void init(ServletConfig config) throws ServletException {
	
		// TODO
		// Bind to controller.
		
	}
	
	/**
	 * Only expose this servlet to HTTP GET.
	 */
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
		throws ServletException, IOException {
	    
		logger.info("Hello");
		
		resp.getWriter().write("Hello");
		
	}
	
	
//	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
//		throws ServletException, IOException {
//    
//		service(req, resp);
//	
//	}
	
}
