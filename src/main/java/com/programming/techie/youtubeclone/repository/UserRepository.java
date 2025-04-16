package com.programming.techie.youtubeclone.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.programming.techie.youtubeclone.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findBySub(String sub);
}
