package com.programming.pgs.youtubeclone.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.programming.pgs.youtubeclone.service.UserRegistrationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserRegistrationService userRegistrationService;
	
	@PostMapping("/register")
	public String register(Authentication authentication) {
		// Requires Auth from the frontend
		Jwt jwt = (Jwt) authentication.getPrincipal();
		
		userRegistrationService.registerUser(jwt.getTokenValue());
		return "User Registration successfull";
	}

}
