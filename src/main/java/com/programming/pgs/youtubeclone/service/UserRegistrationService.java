package com.programming.pgs.youtubeclone.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Version;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Optional;

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
     * Registers a user based on the provided OAuth token.
     *
     * <p>This method fetches user information using the provided token,
     * parses it into a DTO, and checks whether a user with the same
     * subject ("sub") already exists in the database.</p>
     *
     * <ul>
     *   <li>If the user already exists, it returns their existing ID.</li>
     *   <li>If the user does not exist, it creates a new user and returns the new ID.</li>
     * </ul>
     *
     * @param tokenValue the OAuth access token used to fetch user info
     * @return the ID of the existing or newly created user
     */
    public String registerUser(String tokenValue) {
        String userInfoJson = fetchUserInfo(tokenValue);
        UserInfoDto userInfoDto = parseUserInfo(userInfoJson);
        
        // Check if user already exists by subject
        Optional<User> userBySubject = userRepository.findBySub(userInfoDto.getSub());
        if(userBySubject.isPresent()) {
        	return userBySubject.get().getId();
        }
        
        User user = mapToUser(userInfoDto);
        return userRepository.save(user).getId();
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
