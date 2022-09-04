// Java Program to Illustrate Creation Of
// Service Interface

package com.smartcontactmanager.service;

// Importing required classes


// Interface
public interface EmailService {

	// Method
	// To send a simple email
	boolean sendSimpleMail(String subject, String message, String to);

	
}
