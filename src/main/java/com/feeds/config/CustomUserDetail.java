package com.feeds.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.feeds.entity.User;
import com.feeds.repositiry.UserRepos;


public class CustomUserDetail implements UserDetailsService {

	@Autowired
	private UserRepos userRepos;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = this.userRepos.getUserByEmail(username);

		if (user == null) {
			System.out.println("User not found for username: " + username);
			// Instead of throwing an exception immediately, you can return null or handle
			// it differently based on your requirement.
			// For example, you can throw UsernameNotFoundException here:
			throw new UsernameNotFoundException("User not found with username: " + username);
		}

		UserDetailsInfo userDetailsInfo = new UserDetailsInfo(user);
		return userDetailsInfo;
	}

}
