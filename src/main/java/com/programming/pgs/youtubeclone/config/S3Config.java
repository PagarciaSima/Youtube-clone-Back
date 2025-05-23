package com.programming.pgs.youtubeclone.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

	@Value("${cloud.aws.credentials.access-key}")
	private String accessKey;

	@Value("${cloud.aws.credentials.secret-key}")
	private String secretKey;

	@Value("${cloud.aws.region.static}")
	private String region;

	
	/*
	 * Creates and returns an instance of AmazonS3 client using the provided AWS
	 * credentials and region. <p> This method initializes the AWS SDK client for
	 * Amazon S3 using the access key, secret key, and region that are provided. It
	 * uses the `BasicAWSCredentials` for authentication and sets up the client with
	 * static credentials. </p>
	 *
	 * @return An instance of {@link AmazonS3} configured with the provided
	 * credentials and region.
	 */
	@Bean
	AmazonS3 amazonS3() {
		var credentials = new BasicAWSCredentials(accessKey, secretKey);

		return AmazonS3ClientBuilder.standard().withRegion(region)
				.withCredentials(new AWSStaticCredentialsProvider(credentials)).build();
	}
}
