package com.programming.techie.youtubeclone.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.programming.techie.youtubeclone.service.VideoService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/videos")
@RequiredArgsConstructor
public class VideoController {

	private final VideoService videoService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public void uploadVideo(@RequestParam("file") MultipartFile file) {
		videoService.uploadVideo(file);
	}

}