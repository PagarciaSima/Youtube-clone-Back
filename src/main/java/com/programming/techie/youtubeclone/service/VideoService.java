package com.programming.techie.youtubeclone.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.programming.techie.youtubeclone.dto.VideoDto;
import com.programming.techie.youtubeclone.model.Video;
import com.programming.techie.youtubeclone.repository.VideoRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class VideoService {

	private final S3Service s3Service;
	private final VideoRepository videoRepository;
	
    public void uploadVideo(MultipartFile multipartFile) {
    	String videoUrl = s3Service.uploadFile(multipartFile);
    	var video = new Video();
    	video.setVideoUrl(videoUrl);
    	
    	videoRepository.save(video);
    }

    public VideoDto editVideo(VideoDto videoDto) {
        // Find the video by videoId
        var savedVideo = getVideoById(videoDto.getId());
        // Map the videoDto fields to video
        savedVideo.setTitle(videoDto.getTitle());
        savedVideo.setDescription(videoDto.getDescription());
        savedVideo.setTags(videoDto.getTags());
        savedVideo.setThumbnailUrl(videoDto.getThumbnailUrl());
        savedVideo.setVideoStatus(videoDto.getVideoStatus());

        // save the video  to the database
        videoRepository.save(savedVideo);
        return videoDto;
    }
    
    Video getVideoById(String videoId) {
        return videoRepository.findById(videoId)
                .orElseThrow(() -> new IllegalArgumentException("Cannot find video by id - " + videoId));
    }


   
}