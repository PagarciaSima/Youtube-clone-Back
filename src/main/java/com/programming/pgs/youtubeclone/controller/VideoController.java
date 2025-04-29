package com.programming.pgs.youtubeclone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.programming.pgs.youtubeclone.dto.CommentDto;
import com.programming.pgs.youtubeclone.dto.UploadVideoResponse;
import com.programming.pgs.youtubeclone.dto.VideoDto;
import com.programming.pgs.youtubeclone.service.VideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UploadVideoResponse uploadVideo(@RequestParam("file") MultipartFile file) {
        return videoService.uploadVideo(file);
    }
	
    @PostMapping("/thumbnail")
    @ResponseStatus(HttpStatus.CREATED)
    public String uploadThumbnail(@RequestParam("file") MultipartFile file, @RequestParam("videoId") String videoId) {
        return videoService.uploadThumbnail(file, videoId);
    }

    @PutMapping
    @ResponseStatus(HttpStatus.OK)
    public VideoDto editVideoMetadata(@RequestBody VideoDto videoDto) {
        return videoService.editVideo(videoDto);
    }
    
    @GetMapping("/{videoId}")
    @ResponseStatus(HttpStatus.OK)
    public VideoDto getVideoDetails(@PathVariable String videoId) {
    	return videoService.getVideoDetails(videoId);
    }
    
	@PostMapping("/{videoId}/like")
	@ResponseStatus(HttpStatus.OK)
	public VideoDto likeVideo(@PathVariable String videoId) {
		return this.videoService.likeVideo(videoId);
	}
	
	@PostMapping("/{videoId}/disLike")
	@ResponseStatus(HttpStatus.OK)
	public VideoDto disLikeVideo(@PathVariable String videoId) {
		return this.videoService.disLikeVideo(videoId);
	}
	
	@PostMapping("/{videoId}/comment")
	@ResponseStatus(HttpStatus.OK)
	public void addComment(@PathVariable String videoId, @RequestBody CommentDto commentDto) {
		this.videoService.addComment(videoId, commentDto);
	}
}