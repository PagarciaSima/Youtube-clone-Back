package com.programming.pgs.youtubeclone.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.programming.pgs.youtubeclone.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findBySub(String sub);
}
