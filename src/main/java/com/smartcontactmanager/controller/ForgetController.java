package com.smartcontactmanager.controller;

import java.util.Random;

import javax.servlet.http.HttpSession;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smartcontactmanager.dao.UserRepo;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.service.EmailService;

@Controller
public class ForgetController {

	@Autowired
	private EmailService emailService;
	
	@Autowired
	private UserRepo userRepo;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	Random random = new Random(1000);
	
	
	@RequestMapping("/forget")
	public String openForgetForm() {
		return "forget_form";
	}
	
	
	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {
		
		System.out.println("EMAIL "+email);
		
//		generating 4digit otp

		int otp = random.nextInt(9999);
		System.out.println("OTP "+otp);
		
		String subject="OTP FROM SMART CONTACT MANAGER";
		String message="OTP = "+otp+"";
		String to=email;
		
		boolean flag = this.emailService.sendSimpleMail(subject, message, to);
		
		if(flag) {
			
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);
			return "verfiy_otp";
		}else {
			
			session.setAttribute("message", "Check your email id!!");
			
			return "forget_form";
		}
		
	}
	
//	verfiy otp
	@PostMapping("/verify-otp")
	public String verfiyOtp(@RequestParam("otp") int otp, HttpSession session) {
	
		int myotp= (int) session.getAttribute("myotp");
		String email= (String) session.getAttribute("email");
		
		if(myotp==otp) {
//			change password view page
			
			User user = userRepo.getUserByUserName(email);
			
			if(user==null) {
				//send error
				
				session.setAttribute("message", "Please Enter Registered Email ID");
				return "forget_form";
				
			}else {
//				send change password form page
			}
			
			return "password_change_form";
		}else {
			
			session.setAttribute("message", "You have entered wrong otp");
			return "verfiy_otp"; 
		}
		
		
	}
	
//	change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email= (String) session.getAttribute("email");
		User user = this.userRepo.getUserByUserName(email);
		
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		
		this.userRepo.save(user);
		
		return "redirect:/signin?change=Password changed successfully...";
	}
}
