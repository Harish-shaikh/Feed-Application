package com.feeds.repositiry;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.feeds.entity.Post;
import com.feeds.entity.User;



@Repository
public interface PostRepos extends JpaRepository<Post, Integer>{

	public List<Post> findByUserId(Integer userId);
	
	 @Query("SELECT p FROM Post p WHERE p.user != :user")
	    List<Post> findAllByUserNot(User user);

	
}