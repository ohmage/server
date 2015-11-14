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
package org.ohmage.util;

import java.util.Collection;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.NoSuchProviderException;
import javax.mail.SendFailedException;
import javax.mail.Session;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import com.sun.mail.smtp.SMTPTransport;

import org.ohmage.cache.PreferenceCache;
import org.ohmage.exception.CacheMissException;
import org.ohmage.exception.ServiceException;


/**
 * A collection of methods for manipulating emails. 
 * 
 * @author John Jenkins
 * @author Hongsuda T.
 */

public class MailUtils {

	private static final String MAIL_PROTOCOL = "smtp";
	private static final String MAIL_PROPERTY_HOST = 
			"mail." + MAIL_PROTOCOL + ".host";
	private static final String MAIL_PROPERTY_PORT =
			"mail." + MAIL_PROTOCOL + ".port";
	private static final String MAIL_PROPERTY_SSL_ENABLED =
			"mail." + MAIL_PROTOCOL + ".ssl.enable";

	
	/**
	 * It is unnecessary to instantiate this class as it is a collection of 
	 * static methods.
	 */
	private MailUtils() {}
	
	/**
	 * Creates and returns a new mail Session.
	 * 
	 * @return The mail session based on the current state of the preferences
	 * 		   in the database.
	 * 
	 * @throws ServiceException There was a problem creating the session.
	 */
	public static Session getMailSession() throws ServiceException {	
		// Get the email properties.
		Properties sessionProperties = new Properties();
		try {
			String host = 
					PreferenceCache.instance().lookup(
						PreferenceCache.KEY_MAIL_HOST);
			
			sessionProperties.put(MAIL_PROPERTY_HOST, host);
		}
		catch(CacheMissException e) {
			// This is acceptable. It simply tells JavaMail to use the
			// default.
		}
		
		try {
			sessionProperties.put(
					MAIL_PROPERTY_PORT, 
					PreferenceCache.instance().lookup(
						PreferenceCache.KEY_MAIL_PORT));
		}
		catch(CacheMissException e) {
			// This is acceptable. It simply tells JavaMail to use the
			// default.
		}
		
		try {
			sessionProperties.put(
					MAIL_PROPERTY_SSL_ENABLED, 
					PreferenceCache.instance().lookup(
						PreferenceCache.KEY_MAIL_SSL));
		}
		catch(CacheMissException e) {
			// This is acceptable. It simply tells JavaMail to use the
			// default.
		}
		
		// Create the session and return it.
		return Session.getInstance(sessionProperties);
	}
	
	/**
	 * Set the from, to to the message
	 */
	public static void setMailMessageFrom(Message message, String senderKey)
		throws ServiceException {

		// Add the sender.
		try {
			message.setFrom(
					new InternetAddress(
							PreferenceCache.instance().lookup(senderKey)));
							
		}
		catch(CacheMissException e) {
			throw new ServiceException(
				"The mail property is not in the preference table: " + senderKey,
				e);
		}
		catch(AddressException e) {
			throw new ServiceException(
					"The origin address is not a valid email address.",
					e);
		}
		catch(MessagingException e) {
			throw new ServiceException(
					"Could not update the sender's email address.",
					e);
		}	
	}
	
	/* set the message recipient */
	public static void setMailMessageTo(Message message, String toAddress)
			throws ServiceException { 
		
		// Add the recipient.
		try {
			message.setRecipient(
					Message.RecipientType.TO, 
					new InternetAddress(toAddress));
		}
		catch(AddressException e) {
			throw new ServiceException(
					"The destination address is not a valid email address.",
					e);
		}
		catch(MessagingException e) {
			throw new ServiceException(
					"Could not add the recipient to the message.",
					e);
		}
	}
	
	/**
	 * Set the message subject
	 */
	public static void setMailMessageSubject(Message message, String subjectKey)
		throws ServiceException {	
		
		// Add the subject.
		try {
			message.setSubject(
					PreferenceCache.instance().lookup(subjectKey));
		}
		catch(CacheMissException e) {
			throw new ServiceException(
				"The mail property is not in the preference table: " + subjectKey,
				e);
		}
		catch(MessagingException e) {
			throw new ServiceException(
					"Could not set the subject on the message.",
					e);
		}
	}
	
	
	/**
	 * Sends a mail message.
	 * 
	 * @param smtpSession The session used to create the message.
	 * 
	 * @param message The message to be sent.
	 * 
	 * @throws ServiceException There was a problem creating the connection to
	 * 							the mail server or sending the message.
	 */
	public static void sendMailMessage(Session smtpSession, Message message) throws ServiceException {

		// If use Transport.send(), no need to save the message. 
		// Otherwise, save the message before sending.  
		try {
			message.saveChanges();
		}
		catch(MessagingException e) {
			throw new ServiceException(
					"Could not save the changes to the message.",
					e);
		}
		

		// Get the transport from the session.
		SMTPTransport transport;
		try {
			transport = 
					(SMTPTransport) smtpSession.getTransport(MAIL_PROTOCOL);
		}
		catch(NoSuchProviderException e) {
			throw new ServiceException(
					"There is no provider for SMTP. " +
						"This means the library has changed as it has built-in support for SMTP.",
					e);
		}

		Boolean auth = null;
		try {
			auth = StringUtils.decodeBoolean(
					PreferenceCache.instance().lookup(
						PreferenceCache.KEY_MAIL_AUTH));
		}
		catch(CacheMissException e) {
			// This is acceptable. It simply tells JavaMail to use the
			// default.
		}
		
		if((auth != null) && auth) {
			String mailUsername;
			try {
				mailUsername = 
						PreferenceCache.instance().lookup(
							PreferenceCache.KEY_MAIL_USERNAME);
			}
			catch(CacheMissException e) {
				throw new ServiceException(
					"The mail property is not in the preference table: " +
						PreferenceCache.KEY_MAIL_USERNAME,
					e);
			}
			
			String mailPassword;
			try {
				mailPassword = 
						PreferenceCache.instance().lookup(
							PreferenceCache.KEY_MAIL_PASSWORD);
			}
			catch(CacheMissException e) {
				throw new ServiceException(
					"The mail property is not in the preference table: " +
						PreferenceCache.KEY_MAIL_PASSWORD,
					e);
			}
			
			try {
				transport.connect(
						smtpSession.getProperty(MAIL_PROPERTY_HOST), 
						mailUsername, 
						mailPassword);
			}
			catch(MessagingException e) {
				throw new ServiceException(
						"Could not authenticate with or connect to the mail server.",
						e);
			}
		}
		else {
			try {
				transport.connect();
			}
			catch(MessagingException e) {
				throw new ServiceException(
						"Could not connect to the mail server.",
						e);
			}
		}
		

		try {
			transport.sendMessage(message, message.getAllRecipients());
		}
		catch(SendFailedException e) {
			throw new ServiceException(
					"Failed to send the message.",
					e);
		}
		catch(MessagingException e) {
			throw new ServiceException(
					"There was a problem while sending the message.",
					e);
		}
		
		try {
			transport.close();
		}
		catch(MessagingException e) {
			throw new ServiceException(
					"After sending the message there was an error closing the connection.",
					e);
		}
	}

}
