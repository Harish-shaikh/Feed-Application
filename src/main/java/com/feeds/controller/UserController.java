package com.feeds.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.feeds.entity.Message;
import com.feeds.entity.Post;
import com.feeds.entity.User;
import com.feeds.repositiry.PostRepos;
import com.feeds.repositiry.UserRepos;

import jakarta.servlet.http.HttpSession;
import jakarta.websocket.Session;

@Controller
public class UserController {
	@Autowired
	private UserRepos userRepos;

	@Autowired
	private PostRepos postRepos;

	// Add common data to the model for each request
	@ModelAttribute
	public void addCommanData(Model model, Principal principal) {
		String username = principal.getName();
		User user = userRepos.getUserByEmail(username);
		model.addAttribute("userName", user);
	}

	// Handler for displaying the user dashboard
	@GetMapping("/api1/user/dashboard")
	public String userDashboard(Model m) {
		m.addAttribute("title", "DASHBOARD");
		return "user/dashboard";
	}

	// Handler for displaying the form to add a new post
	@GetMapping("/api1/user/createpost")
	public String addPost(Model model) {
		model.addAttribute("title", "Addpost");
		return "user/addpost";
	}

	// Handler for submitting a new post
	@PostMapping("/api1/user/submit-post")
	public String submitPost(@ModelAttribute Post post, Model model, Principal principal, HttpSession session,
			@RequestParam("postImage") MultipartFile file) {
		try {
			// Get the username of the logged-in user
			String username = principal.getName();
			// Find the user in the database
			User user = userRepos.getUserByEmail(username);

			// Save the image and get the filename
			String imageName = saveImage(file);
			// Set the image URL for the post
			post.setPostImageUrl(imageName != null ? imageName : "deafault.png");

			// Set the post date
			post.setPostDate(new Date());
			// Set the user for the post
			post.setUser(user);

			// Save the post to the database
			this.postRepos.save(post);

			// Set success attribute to true
			model.addAttribute("success", true);
			session.setAttribute("message", new Message("post SuccessFully done", "alert-success"));

		} catch (IOException e) {
			// Handle IO exception
			e.printStackTrace();
			// Set success attribute to false
			model.addAttribute("success", false);
			session.setAttribute("message", new Message("something went wrong" + e.getMessage(), "alert-success"));
		}
		return "user/addpost";
	}

	// Method to save the uploaded image file
	private String saveImage(MultipartFile file) throws IOException {
		if (file != null && !file.isEmpty()) {
			String originalFileName = file.getOriginalFilename();
			String fileName = UUID.randomUUID().toString() + "_" + originalFileName;
			String uploadDir = "static/images";
			Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();

			// Log the upload directory
			System.out.println("Upload directory: " + uploadPath);

			// Create the directory if it doesn't exist
			if (!Files.exists(uploadPath)) {
				Files.createDirectories(uploadPath);
				System.out.println("Upload directory created successfully.");
			}

			// Save the file
			Path imagePath = uploadPath.resolve(fileName);
			Files.copy(file.getInputStream(), imagePath, StandardCopyOption.REPLACE_EXISTING);
			System.out.println("File saved successfully: " + imagePath);

			return fileName;
		}
		return null;
	}

	// get User Posts
	@GetMapping("/api1/user/mypost")
	public String allPost(Model m, Principal p) {
		String username = p.getName();
		// Find the user in the database
		User user = this.userRepos.getUserByEmail(username);
		if (user != null) {
			int userId = user.getId();
			List<Post> posts = this.postRepos.findByUserId(userId);

			System.out.println(posts);
			m.addAttribute("myposts", posts);
		} else {
			// Handle case where user is not found
			// You may redirect to an error page or perform other actions
			System.out.println("User not found");
		}

		return "user/mypost";
	}

	// show user all post
	@GetMapping("/api1/user/updatedeletePost")
	public String updatedeletePost(Principal p, Model m) {
		String username = p.getName();
		// Find the user in the database
		User user = this.userRepos.getUserByEmail(username);
		if (user != null) {
			int userId = user.getId();
			List<Post> posts = this.postRepos.findByUserId(userId);

			m.addAttribute("myposts", posts);
		} else {
			// Handle case where user is not found
			// You may redirect to an error page or perform other actions
			System.out.println("User not found");
		}

		return "user/updatedeletepost";
	}

	@GetMapping("/api1/user/deletepost/{postId}")
	public String deletePost(@PathVariable("postId") Integer postId, RedirectAttributes redirectAttributes,
			HttpSession session) {
		try {
			this.postRepos.deleteById(postId);
			session.setAttribute("message", new Message("post SuccessFully done", "alert-success"));

		} catch (Exception e) {
			// Handle IO exception
			e.printStackTrace();
			// Set success attribute to false

			session.setAttribute("message", new Message("something went wrong" + e.getMessage(), "alert-success"));
		}

		return "user/updatedeletepost"; // Redirect back to the user's posts page
	}

	@GetMapping("/api1/user/allpost")
	public String updatedeletePost(Model m, Principal principal) {
		// Get the username of the logged-in user
		String username = principal.getName();

		// Find the user in the database
		User user = userRepos.getUserByEmail(username);

		List<Post> posts;

		if (user != null) {
			// Find all posts except those created by the current user
			posts = postRepos.findAllByUserNot(user);
		} else {
			// If user not found, fetch all posts
			posts = postRepos.findAll();
		}

		m.addAttribute("myposts", posts);

		return "user/mypost";
	}

	// open update contrller handller
	@PostMapping("/api1/user/updatepost/{postId}")
	public String updateData(@PathVariable("postId") Integer postId, Model m, Principal p) {
		Post post = this.postRepos.findById(postId).get();

		m.addAttribute("title", "update data");

		m.addAttribute("postData", post);
		System.out.println("this postId" + post.getPostId());

		return "user/updatepost";
	}

	@PostMapping("/api1/user/process-update")
	public String updateHandeller(@ModelAttribute Post posts, @RequestParam("postImages") MultipartFile file,
			Principal p, Model m, HttpSession hp) {
		try {
			// Retrieve existing post
			Optional<Post> getPost = this.postRepos.findById(posts.getPostId());
			if (getPost.isEmpty()) {
				throw new IllegalArgumentException("Invalid post ID");
			}

			// Check if there's a new image uploaded
			if (!file.isEmpty()) {
				// Delete old photo if it exists
				String postImageUrl = posts.getPostImageUrl();
				if (postImageUrl != null) {
					Path oldImagePath = Paths.get("static/image", postImageUrl);
					if (Files.exists(oldImagePath)) {
						Files.delete(oldImagePath);
					}
				}

				// Save new photo
				String newImageUrl = saveImage(file); // Save the new image and get its URL
				posts.setPostImageUrl(newImageUrl);
			}

			User user = this.userRepos.getUserByEmail(p.getName());
			posts.setUser(user);

			// Set the current date for the post
			posts.setPostDate(new Date());

			// Save updated post
			Post updatedPost = this.postRepos.save(posts);

			hp.setAttribute("message", new Message("post SuccessFully done", "alert-success"));

		} catch (IOException e) {
			// Handle IO exception
			e.printStackTrace();
			// Set success attribute to false

			hp.setAttribute("message", new Message("something went wrong" + e.getMessage(), "alert-success"));
		}

		return "user/updatepost";
	}

}
