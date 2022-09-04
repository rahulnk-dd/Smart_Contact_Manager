
package com.smartcontactmanager.controller;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.smartcontactmanager.dao.ContactRepo;
import com.smartcontactmanager.dao.MyOrderRepo;
import com.smartcontactmanager.dao.UserRepo;
import com.smartcontactmanager.entities.Contact;
import com.smartcontactmanager.entities.MyOrder;
import com.smartcontactmanager.entities.User;
import com.smartcontactmanager.helper.Message;

import com.razorpay.*;

@Controller
@RequestMapping("/user")
public class UserController {

	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;
	
	@Autowired
	private UserRepo dao;
	
	@Autowired
	private ContactRepo contactRepoDao;
	
	@Autowired
	private MyOrderRepo myOrderRepo;
	
//	method for common data response
	@ModelAttribute
	public void addCommonData(Model model, Principal principal) {
		String userName = principal.getName();
		System.out.println("UserName "+ userName);
		
		User user = dao.getUserByUserName(userName);
		System.out.println("User data "+user);
		
		model.addAttribute("user", user);
	}
	
//	dashboard home
	@RequestMapping("/index")
	public String index(Model model, Principal principal) {
		model.addAttribute("title", "User Dashboard");
		return "normal/user_dashboard";
	}
	
//	open add form handler
	@GetMapping("/add-contact")
	public String openAddContactForm(Model model, Principal principal) {
		model.addAttribute("title", "Add Contact");
		model.addAttribute("contact", new Contact());
		return "normal/add_contact_form";
	}
	
//	processing add contact form
	@PostMapping("/process-contact")
	public String processContact(@ModelAttribute Contact contact,@RequestParam("profileImage") MultipartFile file ,Principal principal, HttpSession session) {
		
		try {
		
		String name = principal.getName();
		User user = dao.getUserByUserName(name);
		
		
//		processing and uploading file...
		if(file.isEmpty()) {
			System.out.println("File is emplty");
			contact.setImage("contact.png");
			
		}else {
			
			
			contact.setImage(file.getOriginalFilename());
			
			File saveFile = new ClassPathResource("static/image").getFile();
			
			Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
			
			Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
			
			System.out.println("Image is uploaded");
		}
		
		contact.setUser(user);
	
		user.getContacts().add(contact);
		dao.save(user);
		System.out.println(contact);
		
//		success message
		session.setAttribute("message", new Message("Your contact is added", "success"));
		
		return "normal/add_contact_form";
	
		}catch (Exception e) {
		System.out.println(e.getMessage());
		e.printStackTrace();
//		error message
		session.setAttribute("message", new Message("Something went wrong", "danger"));
	}
		return "normal/add_contact_form";

	}
	
	
//	show contact handler

	@GetMapping("/show-contacts/{page}")
	public String showContact(@PathVariable("page") Integer page ,Model m, Principal principal) {
		m.addAttribute("title", "View Contacts");
		
		String username = principal.getName();
		User user = dao.getUserByUserName(username);
		
//		current page, page per -5
		Pageable pageable = PageRequest.of(page, 3);
		
		Page<Contact> contacts = contactRepoDao.findContactByUser(user.getId(),pageable);
		
		m.addAttribute("contacts", contacts);
		m.addAttribute("currentPage", page);
		
		m.addAttribute("totalPages", contacts.getTotalPages());
		
		return "normal/show_contacts";
	}
	
//	showing particular contact
	@RequestMapping("/{cId}/contact")
	public String showContactDetails(@PathVariable("cId") Integer cid,  Model model, Principal principal) {
		System.out.println("CID "+cid);
		
		Optional<Contact> contactOptional = contactRepoDao.findById(cid);
		Contact contact= contactOptional.get();
		
		String userName = principal.getName();
		User user = dao.getUserByUserName(userName);
		
		System.out.println("Contact User Id "+ contact.getUser().getId());
		if(user.getId()== contact.getUser().getId())
			model.addAttribute("contact", contact);
		
		
		return "normal/contact_detail";
	}
	
	
//	delete contact
	@GetMapping("/delete/{cId}")
	@Transactional
	public String deleteContact(@PathVariable("cId") Integer cId,Model model, Principal principal,HttpSession session) {
		
		System.out.println("CID"+ cId);
		
		Contact contact = this.contactRepoDao.findById(cId).get();
		
		User user = this.dao.getUserByUserName(principal.getName());
		
		user.getContacts().remove(contact);
		
		this.dao.save(user);
			
		System.out.println("Deleted");
		session.setAttribute("message", new Message("Contact deleted successfully", "success"));
		return "redirect:/user/show-contacts/0";
	}
	
//	open  contact form	
	@PostMapping("/update-contact/{cid}")
	public String updateForm(@PathVariable("cid") Integer cid, Model model) {
		
		model.addAttribute("title", "Update Contact");
		
		Contact contact= contactRepoDao.findById(cid).get();
		
		model.addAttribute("contact", contact);
		return "normal/update_form";
	}
	
//	update contact
	
	@PostMapping("/process-update")
	public String updateHandler(@ModelAttribute Contact contact, @RequestParam("profileImage") MultipartFile file, 
			Model model, HttpSession session, Principal principal) {
		
//		old contact details
		Contact oldContact = contactRepoDao.findById(contact.getcId()).get();
		
		try {
//			image
			if(!file.isEmpty()) {
//				delete image
				File deleteFile = new ClassPathResource("static/image").getFile();
				File file1= new File(deleteFile, oldContact.getImage());
				file1.delete();
				
				
//				update image
				File saveFile = new ClassPathResource("static/image").getFile();
				
				Path path = Paths.get(saveFile.getAbsolutePath()+File.separator+file.getOriginalFilename());
				
				Files.copy(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
				
				System.out.println("Image is uploaded");
				contact.setImage(file.getOriginalFilename());
				
			}else {
				contact.setImage(oldContact.getImage());
			}
			
			User user = dao.getUserByUserName(principal.getName());
			contact.setUser(user);
			
			contactRepoDao.save(contact);
			
			session.setAttribute("message", new Message("Your contact is updated...", "success"));
			
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		System.out.println("Contact Name "+ contact.getName());
		System.out.println("Contact Id "+ contact.getcId());
		return "redirect:/user/"+contact.getcId()+"/contact";
	}
	
//	your profile handler
	@GetMapping("/profile")
	public String yourProfile(Model model) {
		model.addAttribute("title", "Profile Page");
		return "normal/profile";
	}
	
//	open setting handler
	@GetMapping("/settings")
	public String openSettingsHandler() {
		return "normal/settings";
	}
	
	
//	change password handler
	@PostMapping("/change-password")
	public  String changePassword(@RequestParam("oldPassword") String oldPassword,
									@RequestParam("newPassword") String newPassword,
									Principal principal, HttpSession session) {
		System.out.println("old password "+ oldPassword +" new password "+ newPassword);
	
		User user = dao.getUserByUserName(principal.getName());
		System.out.println(user.getPassword());
		
		if(this.bCryptPasswordEncoder.matches(oldPassword, user.getPassword())) {
			
//			change password
			user.setPassword(this.bCryptPasswordEncoder.encode(newPassword));
			this.dao.save(user);
			
			session.setAttribute("message", new Message("Your password is successfully changed..", "success"));
			
		}else {
			session.setAttribute("message", new Message("Your old password is wrong..", "danger"));
			return "redirect:/user/settings";
		}
		
		
		return "redirect:/user/index";
	}
	
	
//	craeting order for payment 
	
	@PostMapping("/create_order")
	@ResponseBody
	public String createOrder(@RequestBody Map<String, Object> data, Principal principal) throws Exception {
	
		System.out.println("Hey order function executed ");
		System.out.println(data);
		
		int amt=Integer.parseInt(data.get("amount").toString());
		
		var client =new RazorpayClient("rzp_test_Gwwm4AGXR3j17G", "Vtu6YsjmlJlCmg5vFDAt4FRB");
		
		JSONObject object = new JSONObject();
		object.put("amount", amt*100); //paise
		object.put("currency", "INR");
		object.put("receipt", "txn_235425");
		
//		crearing new order
		
		Order order = client.orders.create(object);
		System.out.println(order);
		
//		save order to database
		
		MyOrder myOrder = new MyOrder();
		myOrder.setAmount(order.get("amount")+"");
		myOrder.setOrderId(order.get("id"));
		myOrder.setPaymentId(null);
		myOrder.setStatus("created");
		myOrder.setUser(this.dao.getUserByUserName(principal.getName()));
		myOrder.setReceipt(order.get("receipt"));
		
		this.myOrderRepo.save(myOrder);
		
		return order.toString();
	}
	
//	updating payment order
	@PostMapping("/update_order")
	public  ResponseEntity<?> updateOrder(@RequestBody Map<String, Object> data){
		
		 MyOrder myOrder = this.myOrderRepo.findByOrderId(data.get("order_id").toString());
		 
		 myOrder.setPaymentId(data.get("payment_id").toString());
		 myOrder.setStatus(data.get("status").toString());
		 
		 this.myOrderRepo.save(myOrder);
		
		System.out.println(data);
		return ResponseEntity.ok(Map.of("msg","updated"));
	}
}
