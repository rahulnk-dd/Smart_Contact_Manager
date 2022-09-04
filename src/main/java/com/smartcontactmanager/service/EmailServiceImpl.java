// Java Program to Illustrate Creation Of
// Service implementation class

package com.smartcontactmanager.service;

// Importing required classes

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

// Annotation
@Service
// Class
// Implementing EmailService interface
public class EmailServiceImpl implements EmailService {

	@Autowired private JavaMailSender javaMailSender;

	@Value("${spring.mail.username}") private String sender;

	// Method 1
	// To send a simple email
	public boolean sendSimpleMail(String subject, String message, String to)
	{

		boolean f=false;
		
		// Try block to check for exceptions
		try {

			// Creating a simple mail message
			SimpleMailMessage mailMessage
				= new SimpleMailMessage();

			// Setting up necessary details
			mailMessage.setFrom(sender);
			mailMessage.setTo(to);
			mailMessage.setText(message);
			mailMessage.setSubject(subject);

			// Sending the mail
			javaMailSender.send(mailMessage);
			f=true;
			return f;
		}

		// Catch block to handle the exceptions
		catch (Exception e) {
			return f;
		}
	}

}
