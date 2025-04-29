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

/**
 * Service responsible for registering a user by retrieving user information 
 * from an external identity provider using an access token.
 */
@Service
@RequiredArgsConstructor
public class UserRegistrationService {

    @Value("${auth0.userinfoEndpoint}")
    private String userInfoEndpoint;

    private final UserRepository userRepository;

    /**
     * Registers a user by calling the external user info endpoint,
     * parsing the response, mapping it to a User entity, and saving it to the database.
     *
     * @param tokenValue the access token used to fetch user information
     */
    public void registerUser(String tokenValue) {
        String userInfoJson = fetchUserInfo(tokenValue);
        UserInfoDto userInfoDto = parseUserInfo(userInfoJson);
        User user = mapToUser(userInfoDto);
        userRepository.save(user);
    }

    /**
     * Calls the external user info endpoint using the provided token and retrieves the user info in JSON format.
     *
     * @param token the access token used in the Authorization header
     * @return the raw JSON string containing user information
     * @throws RuntimeException if the HTTP request fails
     */
    private String fetchUserInfo(String token) {
        HttpRequest request = HttpRequest.newBuilder()
            .GET()
            .uri(URI.create(userInfoEndpoint))
            .setHeader("Authorization", "Bearer " + token)
            .build();

        try {
            HttpClient client = HttpClient.newBuilder()
                .version(Version.HTTP_2)
                .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch user info", e);
        }
    }

    /**
     * Parses the JSON string returned by the user info endpoint into a UserInfoDto object.
     *
     * @param json the JSON string to parse
     * @return a UserInfoDto containing the user information
     * @throws RuntimeException if parsing the JSON fails
     */
    private UserInfoDto parseUserInfo(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            return mapper.readValue(json, UserInfoDto.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse user info JSON", e);
        }
    }

    /**
     * Maps a UserInfoDto to a User entity.
     *
     * @param dto the user info DTO
     * @return a User entity populated with data from the DTO
     */
    private User mapToUser(UserInfoDto dto) {
        User user = new User();
        user.setFirstName(dto.getGivenName());
        user.setLastName(dto.getFamilyName());
        user.setFullName(dto.getName());
        user.setEmailAddress(dto.getEmail());
        user.setSub(dto.getSub());
        return user;
    }
}
