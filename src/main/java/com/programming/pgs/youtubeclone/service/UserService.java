package com.programming.pgs.youtubeclone.service;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.programming.pgs.youtubeclone.model.User;
import com.programming.pgs.youtubeclone.repository.UserRepository;

import lombok.RequiredArgsConstructor;

/**
 * Service class for handling user-related operations such as managing liked videos, disliked videos, subscriptions, and video history.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);

    /**
     * Retrieves the current authenticated user based on the JWT token.
     * 
     * @return the current authenticated user.
     * @throws IllegalArgumentException if the user cannot be found.
     */
    public User getCurrentUser() {
        String sub = ((Jwt) (SecurityContextHolder.getContext().getAuthentication().getPrincipal())).getClaim("sub");

        LOGGER.debug("Fetching user with sub: {}", sub);
        
        return this.userRepository.findBySub(sub)
                .orElseThrow(() -> {
                    LOGGER.error("Cannot find user with sub - {}", sub);
                    return new IllegalArgumentException("Cannot find user with sub - " + sub);
                });
    }

    /**
     * Adds a video to the liked videos list of the current user.
     * 
     * @param videoId the ID of the video to like.
     */
    public void addToLikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        LOGGER.info("Adding video with ID {} to liked videos of user {}", videoId, currentUser.getId());
        currentUser.addToLikeVideos(videoId);
        this.userRepository.save(currentUser);
    }

    /**
     * Checks if the current user has liked the specified video.
     * 
     * @param videoId the ID of the video to check.
     * @return true if the video is liked by the user, false otherwise.
     */
    public boolean isLikedVideo(String videoId) {
        boolean isLiked = getCurrentUser().getLikedVideos().stream().anyMatch(likedVideo -> likedVideo.equals(videoId));
        LOGGER.debug("Is video with ID {} liked by the current user? {}", videoId, isLiked);
        return isLiked;
    }

    /**
     * Checks if the current user has disliked the specified video.
     * 
     * @param videoId the ID of the video to check.
     * @return true if the video is disliked by the user, false otherwise.
     */
    public boolean isDislikedVideo(String videoId) {
        boolean isDisliked = getCurrentUser().getDisLikedVideos().stream().anyMatch(likedVideo -> likedVideo.equals(videoId));
        LOGGER.debug("Is video with ID {} disliked by the current user? {}", videoId, isDisliked);
        return isDisliked;
    }

    /**
     * Removes a video from the liked videos list of the current user.
     * 
     * @param videoId the ID of the video to remove from liked videos.
     */
    public void removeFromLikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        LOGGER.info("Removing video with ID {} from liked videos of user {}", videoId, currentUser.getId());
        currentUser.removeFromLikedVideos(videoId);
        this.userRepository.save(currentUser);
    }

    /**
     * Removes a video from the disliked videos list of the current user.
     * 
     * @param videoId the ID of the video to remove from disliked videos.
     */
    public void removeFromDislikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        LOGGER.info("Removing video with ID {} from disliked videos of user {}", videoId, currentUser.getId());
        currentUser.removeFromDislikedVideos(videoId);
        this.userRepository.save(currentUser);
    }

    /**
     * Adds a video to the disliked videos list of the current user.
     * 
     * @param videoId the ID of the video to dislike.
     */
    public void addToDislikedVideos(String videoId) {
        User currentUser = getCurrentUser();
        LOGGER.info("Adding video with ID {} to disliked videos of user {}", videoId, currentUser.getId());
        currentUser.addToDislikedVideos(videoId);
        this.userRepository.save(currentUser);
    }

    /**
     * Adds a video to the current user's viewing history.
     * 
     * @param videoId the ID of the video to add to the history.
     */
    public void addVideoToHistory(String videoId) {
        User currentUser = getCurrentUser();
        LOGGER.info("Adding video with ID {} to history of user {}", videoId, currentUser.getId());
        currentUser.addToVideoHistory(videoId);
        this.userRepository.save(currentUser);
    }

    /**
     * Subscribes the current user to another user.
     * 
     * @param userId the ID of the user to subscribe to.
     */
    public void subscribeUser(String userId) {
        User currentUser = getCurrentUser();
        LOGGER.info("User {} is subscribing to user {}", currentUser.getId(), userId);
        currentUser.addToSubscribedToUsers(userId);

        User user = getUserById(userId);
        user.addToSubscribers(currentUser.getId());

        this.userRepository.save(currentUser);
        this.userRepository.save(user);
    }

    /**
     * Unsubscribes the current user from another user.
     * 
     * @param userId the ID of the user to unsubscribe from.
     */
    public void unSubscribeUser(String userId) {
        User currentUser = getCurrentUser();
        LOGGER.info("User {} is unsubscribing from user {}", currentUser.getId(), userId);
        currentUser.removeFromSubscribedToUsers(userId);

        User user = getUserById(userId);
        user.removeFromSubscribers(currentUser.getId());

        this.userRepository.save(currentUser);
        this.userRepository.save(user);
    }

    /**
     * Retrieves a user by their ID.
     * 
     * @param userId the ID of the user to retrieve.
     * @return the user with the specified ID.
     * @throws IllegalArgumentException if the user cannot be found.
     */
    private User getUserById(String userId) {
        LOGGER.debug("Fetching user with ID: {}", userId);
        return this.userRepository.findById(userId)
                .orElseThrow(() -> {
                    LOGGER.error("Cannot find user with userId {}", userId);
                    return new IllegalArgumentException("Cannot find user with userId " + userId);
                });
    }

    /**
     * Retrieves the video watch history for a specific user.
     *
     * <p>
     * This method fetches the {@link User} by ID and returns the set of video IDs
     * that represent the user's watch history.
     * </p>
     *
     * @param userId the unique identifier of the user
     * @return a {@link Set} of video IDs representing the user's watch history
     * @throws IllegalArgumentException if the user with the specified ID does not exist
     */
    public Set<String> userHistory(String userId) {
        LOGGER.info("Fetching video history for user ID: {}", userId);
        
        User user = getUserById(userId);
        Set<String> history = user.getVideoHistory();
        
        LOGGER.debug("User ID: {} has {} videos in watch history", userId, history.size());
        return history;
    }

}
