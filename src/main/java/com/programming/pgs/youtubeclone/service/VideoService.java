package com.programming.pgs.youtubeclone.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.programming.pgs.youtubeclone.dto.CommentDto;
import com.programming.pgs.youtubeclone.dto.UploadVideoResponse;
import com.programming.pgs.youtubeclone.dto.VideoDto;
import com.programming.pgs.youtubeclone.model.Comment;
import com.programming.pgs.youtubeclone.model.Video;
import com.programming.pgs.youtubeclone.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {

	private final S3Service s3Service;
	private final VideoRepository videoRepository;
	private final UserService userService;

	private static final Logger LOGGER = LoggerFactory.getLogger(VideoService.class);

	/**
	 * Uploads a video file to the storage service and saves a new Video entity in
	 * the database.
	 *
	 * @param multipartFile the video file to upload
	 * @return an {@link UploadVideoResponse} containing the ID and URL of the
	 *         uploaded video
	 * @throws RuntimeException if the file upload fails or saving the video fails
	 */
	public UploadVideoResponse uploadVideo(MultipartFile multipartFile) {

		LOGGER.info("Starting video upload...");
		String videoUrl = s3Service.uploadFile(multipartFile);
		LOGGER.debug("Video uploaded to S3 with URL: {}", videoUrl);

		var video = new Video();
		video.setVideoUrl(videoUrl);

		var savedVideo = videoRepository.save(video);
		LOGGER.info("Video entity saved with ID: {}", savedVideo.getId());

		return new UploadVideoResponse(savedVideo.getId(), savedVideo.getVideoUrl());

	}

	/**
	 * Updates the metadata of an existing video based on the provided
	 * {@link VideoDto}.
	 *
	 * <p>
	 * It retrieves the video by its ID, updates its fields (title, description,
	 * tags, thumbnail URL, and status), and saves the updated entity in the
	 * database.
	 * </p>
	 *
	 * @param videoDto the data transfer object containing the updated video
	 *                 information
	 * @return the same {@link VideoDto} that was passed in, reflecting the updated
	 *         values
	 * @throws RuntimeException if the video is not found
	 */
	public VideoDto editVideo(VideoDto videoDto) {
		LOGGER.info("Editing video metadata for ID: {}", videoDto.getId());
		var savedVideo = getVideoById(videoDto.getId());

		savedVideo.setTitle(videoDto.getTitle());
		savedVideo.setDescription(videoDto.getDescription());
		savedVideo.setTags(videoDto.getTags());
		savedVideo.setThumbnailUrl(videoDto.getThumbnailUrl());
		savedVideo.setVideoStatus(videoDto.getVideoStatus());

		videoRepository.save(savedVideo);
		LOGGER.info("Video metadata updated for ID: {}", videoDto.getId());

		return videoDto;
	}

	/**
	 * Retrieves a {@link Video} entity from the database by its ID.
	 *
	 * <p>
	 * If the video is not found, this method throws an
	 * {@link IllegalArgumentException}.
	 * </p>
	 *
	 * @param videoId the unique identifier of the video
	 * @return the {@link Video} object corresponding to the given ID
	 * @throws IllegalArgumentException if no video is found with the specified ID
	 */
	Video getVideoById(String videoId) {
		LOGGER.debug("Fetching video by ID: {}", videoId);
		return videoRepository.findById(videoId).orElseThrow(() -> {
			LOGGER.error("Video not found for ID: {}", videoId);
			return new IllegalArgumentException("Cannot find video by id - " + videoId);
		});
	}

	/**
	 * Uploads a thumbnail image for a given video, updates the video's thumbnail
	 * URL, and saves the updated video in the repository.
	 *
	 * @param file    the thumbnail image file to upload
	 * @param videoId the ID of the video to associate the thumbnail with
	 * @return the URL of the uploaded thumbnail
	 * @throws RuntimeException if the video is not found or the file upload fails
	 */
	public String uploadThumbnail(MultipartFile file, String videoId) {

		LOGGER.info("Uploading thumbnail for video ID: {}", videoId);
		var savedVideo = getVideoById(videoId);

		String thumbnailUrl = s3Service.uploadFile(file);
		LOGGER.debug("Thumbnail uploaded to S3 with URL: {}", thumbnailUrl);

		savedVideo.setThumbnailUrl(thumbnailUrl);
		videoRepository.save(savedVideo);
		LOGGER.info("Thumbnail URL updated for video ID: {}", videoId);

		return thumbnailUrl;
	}

	/**
	 * Retrieves detailed information about a video as a {@link VideoDto}.
	 *
	 * <p>
	 * This method fetches the {@link Video} entity by its ID, increments the view
	 * count, adds the video to the user's history, maps its fields to a
	 * {@link VideoDto}, and returns the DTO containing the video's metadata.
	 * </p>
	 *
	 * @param videoId the unique identifier of the video
	 * @return a {@link VideoDto} containing the video's details such as title,
	 *         description, URL, thumbnail, tags, and status
	 * @throws IllegalArgumentException if no video is found with the specified ID
	 */
	public VideoDto getVideoDetails(String videoId) {
		LOGGER.info("Fetching video details for video ID: {}", videoId);

		Video savedVideo = getVideoById(videoId);

		increaseVideoCount(savedVideo);
		userService.addVideoToHistory(videoId);
		LOGGER.debug("Returning video details for video ID: {}", videoId);

		return mapToVideoDto(savedVideo);
	}

	/**
	 * Increments the view count of the provided video and saves the updated video.
	 * 
	 * This method updates the view count of a video and persists the changes in the
	 * repository. The view count is incremented by calling the
	 * `incrementViewCount()` method on the `Video` object.
	 * 
	 * @param savedVideo the video whose view count is to be incremented.
	 */
	private void increaseVideoCount(Video savedVideo) {
		LOGGER.info("Incrementing view count for video with ID: {}", savedVideo.getId());

		savedVideo.incrementViewCount();

		LOGGER.debug("New view count for video with ID {}: {}", savedVideo.getId(), savedVideo.getViewCount());

		this.videoRepository.save(savedVideo);
		LOGGER.info("Video with ID {} successfully updated with new view count.", savedVideo.getId());
	}

	/**
	 * Handles the logic for liking a video by the current authenticated user.
	 *
	 * <p>
	 * If the user has already liked the video, it removes the like. If the user had
	 * previously disliked the video, it removes the dislike and adds a like. If the
	 * user had neither liked nor disliked the video, it simply adds a like.
	 * </p>
	 *
	 * @param videoId the ID of the video to be liked
	 * @return a {@link VideoDto} representing the updated video information
	 */
	public VideoDto likeVideo(String videoId) {
		Video videoById = getVideoById(videoId);

		if (userService.isLikedVideo(videoId)) {
			videoById.decrementLikes();
			userService.removeFromLikedVideos(videoId);
		} else if (userService.isDislikedVideo(videoId)) {
			videoById.decrementDisLikes();
			userService.removeFromDislikedVideos(videoId);
			videoById.incrementLikes();
			userService.addToLikedVideos(videoId);
		} else {
			videoById.incrementLikes();
			userService.addToLikedVideos(videoId);
		}

		this.videoRepository.save(videoById);

		return mapToVideoDto(videoById);
	}

	/**
	 * Handles the "dislike" logic for a given video.
	 * <p>
	 * If the user has already disliked the video, it removes the dislike.<br>
	 * If the user had previously liked the video, it removes the like and adds a
	 * dislike.<br>
	 * If the user had no prior reaction, it simply adds a dislike.
	 * </p>
	 *
	 * @param videoId the ID of the video to dislike
	 * @return a {@link VideoDto} containing the updated video data
	 */
	public VideoDto disLikeVideo(String videoId) {
		Video videoById = getVideoById(videoId);

		if (userService.isDislikedVideo(videoId)) {
			videoById.decrementDisLikes();
			userService.removeFromDislikedVideos(videoId);
		} else if (userService.isLikedVideo(videoId)) {
			videoById.decrementLikes();
			userService.removeFromLikedVideos(videoId);
			videoById.incrementDisLikes();
			userService.addToDislikedVideos(videoId);
		} else {
			videoById.incrementDisLikes();
			userService.addToDislikedVideos(videoId);
		}

		videoRepository.save(videoById);

		return mapToVideoDto(videoById);
	}

	/**
	 * Maps a {@link Video} entity to a {@link VideoDto} for data transfer.
	 *
	 * @param videoById the video entity to map
	 * @return a {@link VideoDto} representing the video data
	 */
	private VideoDto mapToVideoDto(Video videoById) {
		VideoDto videoDto = new VideoDto();
		videoDto.setVideoUrl(videoById.getVideoUrl());
		videoDto.setThumbnailUrl(videoById.getThumbnailUrl());
		videoDto.setId(videoById.getId());
		videoDto.setTitle(videoById.getTitle());
		videoDto.setDescription(videoById.getDescription());
		videoDto.setTags(videoById.getTags());
		videoDto.setVideoStatus(videoById.getVideoStatus());
		videoDto.setLikeCount(videoById.getLikes().get());
		videoDto.setDislikeCount(videoById.getDisLikes().get());
		videoDto.setViewCount(videoById.getViewCount().get());
		videoDto.setCreatedAt(videoById.getCreatedAt());
		videoDto.setLastModifiedAt(videoById.getLastModifiedAt());
		return videoDto;
	}

	/**
	 * Adds a new comment to a video.
	 *
	 * <p>
	 * This method retrieves the video by its ID, creates a new {@link Comment} from the
	 * provided {@link CommentDto}, adds the comment to the video's comment list,
	 * and saves the updated video to the repository.
	 * </p>
	 *
	 * @param videoId     the unique identifier of the video to which the comment is added
	 * @param commentDto  the data transfer object containing the comment text and author ID
	 * @throws IllegalArgumentException if no video is found with the specified ID
	 */
	public void addComment(String videoId, CommentDto commentDto) {
	    LOGGER.info("Adding comment to video with ID: {}", videoId);
	    
	    Video video = getVideoById(videoId);
	    
	    Comment comment = new Comment();
	    comment.setText(commentDto.getCommentText());
	    comment.setAuthorId(commentDto.getAuthorId());
	    
	    video.addComment(comment);
	    LOGGER.debug("Added comment by user {} to video {}", comment.getAuthorId(), videoId);

	    this.videoRepository.save(video);
	    LOGGER.info("Saved updated video with new comment to repository for video ID: {}", videoId);
	}

	/**
	 * Retrieves all comments associated with a specific video.
	 *
	 * <p>
	 * This method fetches the video by its ID, retrieves its list of comments, and maps
	 * each {@link Comment} to a {@link CommentDto} to be returned to the client.
	 * </p>
	 *
	 * @param videoId the unique identifier of the video
	 * @return a list of {@link CommentDto} representing all comments on the video
	 * @throws IllegalArgumentException if no video is found with the specified ID
	 */
    public List<CommentDto> getAllComments(String videoId) {
        Video video = getVideoById(videoId);
        List<Comment> commentList = video.getCommentList();

        return commentList.stream().map(this::mapToCommentDto).toList();
    }
    
    /**
     * Maps a {@link Comment} entity to a {@link CommentDto}.
     *
     * @param comment the {@link Comment} to be mapped
     * @return the corresponding {@link CommentDto}
     */
    private CommentDto mapToCommentDto(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setCommentText(comment.getText());
        commentDto.setAuthorId(comment.getAuthorId());
        return commentDto;
    }

    /**
     * Retrieves all videos from the database and maps them to {@link VideoDto} objects.
     *
     * <p>
     * This method fetches all {@link Video} entities, maps each to a {@link VideoDto},
     * and returns the resulting list.
     * </p>
     *
     * @return a list of {@link VideoDto} representing all videos stored in the system
     */
	public List<VideoDto> getAllVideos() {
	    LOGGER.info("Fetching all videos from the database");

	    List<VideoDto> videoDtos = this.videoRepository.findAll().stream()
	            .map(this::mapToVideoDto)
	            .toList();

	    LOGGER.debug("Found {} videos in total", videoDtos.size());

	    return videoDtos;
	}
	
	/**
	 * Retrieves a list of videos liked by the currently authenticated user.
	 *
	 * <p>This method obtains the current user from the user service, fetches the list
	 * of liked video IDs, retrieves the corresponding video entities from the repository,
	 * and converts them into DTOs to return to the client.</p>
	 *
	 * @return a list of {@link VideoDto} representing the liked videos of the current user
	 */
	public List<VideoDto> getLikedVideos() {
	    LOGGER.info("Fetching liked videos for the current user");

	    // Get the authenticated user
	    var currentUser = userService.getCurrentUser();
	    var likedVideoIds = currentUser.getLikedVideos();
	    LOGGER.debug("Liked video IDs: {}", likedVideoIds);

	    // Fetch the videos based on the IDs
	    List<Video> likedVideos = videoRepository.findAllById(likedVideoIds);
	    LOGGER.debug("Retrieved {} liked videos from the repository", likedVideos.size());

	    // Convert to DTOs
	    var likedVideoDtos = likedVideos.stream()
	                                    .map(this::mapToVideoDto)
	                                    .toList();

	    LOGGER.info("Returning {} liked video DTOs", likedVideoDtos.size());
	    return likedVideoDtos;
	}
	
	/**
	 * Retrieves the list of videos that the currently authenticated user has disliked.
	 *
	 * This method performs the following steps:
	 * <ul>
	 *     <li>Obtains the authenticated user from the {@code userService}.</li>
	 *     <li>Retrieves the list of disliked video IDs associated with the user.</li>
	 *     <li>Fetches the corresponding video entities from the {@code videoRepository} using those IDs.</li>
	 *     <li>Maps the video entities to {@code VideoDto} objects.</li>
	 *     <li>Returns the list of {@code VideoDto} objects representing the disliked videos.</li>
	 * </ul>
	 *
	 * @return a list of {@link VideoDto} objects representing the disliked videos of the current user
	 */
	public List<VideoDto> getDisLikedVideos() {
	    LOGGER.info("Fetching liked videos for the current user");

	    // Get the authenticated user
	    var currentUser = userService.getCurrentUser();
	    var dislikedVideoIds = currentUser.getDisLikedVideos();
	    LOGGER.debug("Disliked video IDs: {}", dislikedVideoIds);

	    // Fetch the videos based on the IDs
	    List<Video> dislikedVideos = videoRepository.findAllById(dislikedVideoIds);
	    LOGGER.debug("Retrieved {} disliked videos from the repository", dislikedVideos.size());

	    // Convert to DTOs
	    var dislikedVideoDtos = dislikedVideos.stream()
	                                    .map(this::mapToVideoDto)
	                                    .toList();

	    LOGGER.info("Returning {} liked video DTOs", dislikedVideoDtos.size());
	    return dislikedVideoDtos;
	}
	
	/**
	 * Retrieves the list of videos from the viewing history of the currently authenticated user.
	 *
	 * This method performs the following steps:
	 * <ul>
	 *     <li>Obtains the authenticated user from the {@code userService}.</li>
	 *     <li>Retrieves the list of video IDs from the user's history.</li>
	 *     <li>Fetches the corresponding video entities from the {@code videoRepository}.</li>
	 *     <li>Maps the video entities to {@code VideoDto} objects.</li>
	 *     <li>Returns the list of {@code VideoDto} objects representing the history.</li>
	 * </ul>
	 *
	 * @return a list of {@link VideoDto} objects representing the user's video history
	 */
	public List<VideoDto> getVideoHistory() {
	    LOGGER.info("Fetching video history for the current user");

	    // Get the authenticated user
	    var currentUser = userService.getCurrentUser();
	    var videoHistoryIds = currentUser.getVideoHistory();
	    LOGGER.debug("History video IDs: {}", videoHistoryIds);

	    // Fetch the videos based on the IDs
	    List<Video> historyVideos = videoRepository.findAllById(videoHistoryIds);
	    LOGGER.debug("Retrieved {} history videos from the repository", historyVideos.size());

	    // Convert to DTOs
	    List<VideoDto> historyVideoDtos = historyVideos.stream()
	            .map(this::mapToVideoDto)
	            .toList();

	    LOGGER.info("Returning {} video DTOs from history", historyVideoDtos.size());
	    return historyVideoDtos;
	}



}