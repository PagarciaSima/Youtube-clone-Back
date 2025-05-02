package com.programming.pgs.youtubeclone.dto;

import java.time.Instant;
import java.util.Set;

import com.programming.pgs.youtubeclone.model.VideoStatus;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoDto {
    private String id;
    private String title;
    private String description;
    private Set<String> tags;
    private String videoUrl;
    private VideoStatus videoStatus;
    private String thumbnailUrl;
    private Integer likeCount;
    private Integer dislikeCount;
    private Integer viewCount;
    private Instant createdAt;
    private Instant lastModifiedAt;
}