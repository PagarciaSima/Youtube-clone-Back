package com.programming.pgs.youtubeclone.model;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(value = "Video")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Video {

	@Id
	private String id;
	private String title;
	private String description;
	private String userId;
	private AtomicInteger likes = new AtomicInteger(0);
	private AtomicInteger disLikes = new AtomicInteger(0);
	private Set<String> tags;
	private String videoUrl;
	private VideoStatus videoStatus;
	private AtomicInteger viewCount = new AtomicInteger(0);
	private String thumbnailUrl;
	private List<Comment> commentList = new CopyOnWriteArrayList<>();
	@CreatedDate
    private Instant createdAt;
	@LastModifiedDate
    private Instant lastModifiedAt;

	public void incrementLikes() {
		likes.incrementAndGet();
	}

	public void decrementLikes() {
		if (likes.get() > 0) {
			likes.decrementAndGet();
		}
	}

	public void incrementDisLikes() {
		disLikes.incrementAndGet();

	}

	public void decrementDisLikes() {
		if (disLikes.get() > 0) {
			disLikes.decrementAndGet();
		}
	}

	public void incrementViewCount() {
		viewCount.incrementAndGet();
	}

	public void addComment(Comment comment) {
		commentList.add(comment);
	}
}