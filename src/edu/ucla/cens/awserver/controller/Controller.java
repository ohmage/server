package edu.ucla.cens.awserver.controller;

/**
 * Controllers execute a specific task. The idea is that application tasks can be split up into separate controller
 * implementations in order to increase application modularity.  
 * 
 * @author selsky
 */
public interface Controller {

	public void execute();
	
}
