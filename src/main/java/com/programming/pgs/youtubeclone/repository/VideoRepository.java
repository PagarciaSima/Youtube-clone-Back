package com.programming.pgs.youtubeclone.repository;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.programming.pgs.youtubeclone.model.Video;

public interface VideoRepository extends MongoRepository<Video, String> {
}
