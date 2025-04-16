package com.programming.techie.youtubeclone.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.programming.techie.youtubeclone.model.Video;

public interface VideoRepository extends MongoRepository<Video, String> {
}
