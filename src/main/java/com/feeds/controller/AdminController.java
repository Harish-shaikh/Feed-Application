package com.feeds.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import com.feeds.entity.Message;
import com.feeds.entity.Post;
import com.feeds.entity.User;
import com.feeds.repositiry.PostRepos;
import com.feeds.repositiry.UserRepos;

import jakarta.servlet.http.HttpSession;

@Controller
@RequestMapping("/api2/admin")
public class AdminController {
	@Autowired
	private UserRepos userRepos;

	@Autowired
	private PostRepos postRepos;

	// Add common data to the model for each request
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		// Get the username of the logged-in user
		String username = principal.getName();
		// Find the user in the database
		User user = userRepos.getUserByEmail(username);
		// Add user information to the model
		model.addAttribute("userName", user);
	}

	@GetMapping("/dashboard")
	@PreAuthorize("hasAuthority('ADMIN')")
	public String userDashboard(Model m) {
		// Add title attribute to the model
		m.addAttribute("title", "ADMIN DASHBOARD");
		return "admin/dashboard"; // Return the admin dashboard page
	}

	// Show all posts
	@GetMapping("/updatedeletePost")
	public String updatedeletePost(Model m) {
		// Retrieve all posts from the database
		List<Post> posts = this.postRepos.findAll();
		// Add posts to the model
		m.addAttribute("myposts", posts);
		return "admin/updatedeletepost"; // Return the page to display all posts
	}

	// Delete a post
	@GetMapping("/deletepost/{postId}")
	public String deletePost(@PathVariable("postId") Integer postId, HttpSession hs) {
		// Delete the post by its ID
		this.postRepos.deleteById(postId);
		// Set a success message in session
		hs.setAttribute("message", new Message("Contact deleted successfully...", "alert-success"));
		// Redirect to the updatedeletepost page
		return "admin/updatedeletepost";
	}

}
