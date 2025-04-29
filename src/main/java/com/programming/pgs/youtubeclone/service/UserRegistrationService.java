package com.programming.pgs.youtubeclone.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.programming.pgs.youtubeclone.dto.UserInfoDto;
import com.programming.pgs.youtubeclone.model.User;
import com.programming.pgs.youtubeclone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserRegistrationService {

	@Value("${auth0.userinfoEndpoint}")
	private String userInfoEndpoint;
	
	private final UserRepository userRepository;
	
	public void registerUser(String tokenValue) {
		// Call userInfo Endpoint
		HttpRequest httpRequest = HttpRequest.newBuilder()
			.GET()
			.uri(URI.create(userInfoEndpoint))
			.setHeader("Authorization", String.format("Bearer %s", tokenValue))
			.build();
		
		HttpClient httpClient = HttpClient.newBuilder()
			.version(Version.HTTP_2)
			.build();
		
		// Set response as String / json
		try {
			HttpResponse<String> responseString = httpClient.send(httpRequest, HttpResponse.BodyHandlers.ofString());
			// Convert String to json to allow serialization
			String responseBody = responseString.body();
			
			ObjectMapper objectMapper = new ObjectMapper();
			// Configure objectMapper to not fail if some extra properties are given
			objectMapper.configure(DeserializationFeature.FAIL_ON_IGNORED_PROPERTIES, false);
            UserInfoDto userInfoDto = objectMapper.readValue(responseBody, UserInfoDto.class);
			
			User user = new User();
            user.setFirstName(userInfoDto.getGivenName());
            user.setLastName(userInfoDto.getFamilyName());
            user.setFullName(userInfoDto.getName());
            user.setEmailAddress(userInfoDto.getEmail());
            userRepository.save(user);

		} catch (Exception e) {
			throw new RuntimeException("Exception ocurred while registering user", e);
		} 
	}
}
