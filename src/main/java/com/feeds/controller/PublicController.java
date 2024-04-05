package com.feeds.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.feeds.entity.Message;
import com.feeds.entity.User;
import com.feeds.repositiry.UserRepos;

import jakarta.servlet.http.HttpSession;

@Controller
public class PublicController {
	@Autowired
	private UserRepos userRepos;
	@Autowired
	private PasswordEncoder passwordEncoder;

	// This is home page method
	@GetMapping("/welcome")
	public String home(Model m) {
		// Add title attribute to the model
		m.addAttribute("title", "home page");
		return "home";
	}

	// This is about page method
	@GetMapping("/about")
	public String about(Model m) {
		// Add title attribute to the model
		m.addAttribute("title", "about page");
		return "about";
	}

	// This is contactus page method
	@GetMapping("/contactus")
	public String contactus(Model m) {
		// Add title attribute to the model
		m.addAttribute("title", "contactus page");
		return "contactus";
	}

	// This is login page method
	@GetMapping("/login")
	public String login(Model m) {
		// Add title attribute to the model
		m.addAttribute("title", "login page");
		return "login";
	}

	// This is registration page method
	@GetMapping("/registration")
	public String registration(Model m) {
		// Add title attribute to the model
		m.addAttribute("title", "registration page");
		// Add new User object to the model
		m.addAttribute("data", new User());
		return "registration";
	}

	// Add User in database Method
	@PostMapping("/adduser")
	public String adddUser(@ModelAttribute("data") User user, Model m, HttpSession session) {
		try {
			// Encrypt user's password
			user.setPassword(passwordEncoder.encode(user.getPassword()));
			// Save user to the database
			User saveUser = this.userRepos.save(user);
			// Add new User object to the model
			m.addAttribute("saveUser", new User());
			// Set success message in session
			session.setAttribute("message", new Message("User SuccessFully register", "alert-success"));
		} catch (Exception e) {
			// Print stack trace for debugging
			e.printStackTrace();
			// Add user object with errors to the model
			m.addAttribute("data", user);
			// Set error message in session
			session.setAttribute("message", new Message("someting went wrong !!" + e.getMessage(), "alert-danger"));
		}
		return "registration";
	}
}
