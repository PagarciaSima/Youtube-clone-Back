package com.programming.pgs.youtubeclone.controller;

import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.programming.pgs.youtubeclone.service.UserRegistrationService;
import com.programming.pgs.youtubeclone.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserRegistrationService userRegistrationService;
	private final UserService userService;
	
	@PostMapping("/register")
	public String register(Authentication authentication) {
		// Requires Auth from the frontend
		Jwt jwt = (Jwt) authentication.getPrincipal();
		
		return this.userRegistrationService.registerUser(jwt.getTokenValue());
	}
	
	@PostMapping("subscribe/{userId}")
	@ResponseStatus(HttpStatus.OK)
	public boolean subscribeUser(@PathVariable String userId) {
		this.userService.subscribeUser(userId);
		return true;
	}
	
	@PostMapping("unSubscribe/{userId}")
	@ResponseStatus(HttpStatus.OK)
	public boolean unSubscribeUser(@PathVariable String userId) {
		this.userService.unSubscribeUser(userId);
		return true;
	}
	
	@GetMapping("/{userId}/history")
	@ResponseStatus(HttpStatus.OK)
	public Set<String> userHistory(@PathVariable String userId) {
		return this.userService.userHistory(userId);
		
	}

}
