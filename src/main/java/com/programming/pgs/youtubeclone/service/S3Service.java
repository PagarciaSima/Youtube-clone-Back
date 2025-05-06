package com.programming.pgs.youtubeclone.service;

import java.io.IOException;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3Service implements FileService {

    // Injected AmazonS3 client to interact with S3 bucket
    private final AmazonS3  awS3Client;

    // Name of the S3 bucket (Remove public access block"
    private final static String BUCKET_NAME = "youtubeclone-102426687139";

    /**
     * Uploads a file to the configured S3 bucket and returns the public URL.
     *
     * @param file Multipart file to be uploaded.
     * @return Public URL of the uploaded file.
     * @throws ResponseStatusException if an error occurs during file upload.
     */
    @Override
    public String uploadFile(MultipartFile file) {
        // Extract the file extension (e.g., "jpg", "mp4")
        var filenameExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());

        // Generate a unique key for the file to avoid collisions in the bucket
        var key = UUID.randomUUID().toString() + "." + filenameExtension;

        // Prepare metadata for the file (e.g., size, content type)
        var metadata = new ObjectMetadata();
        metadata.setContentLength(file.getSize());
        metadata.setContentType(file.getContentType());

        try {
            // Upload the file to S3 using the bucket name, key, input stream, and metadata
            awS3Client.putObject(BUCKET_NAME, key, file.getInputStream(), metadata);
        } catch (IOException ioException) {
            // Throw HTTP 500 if any error occurs while reading the file stream
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "An Exception occurred while uploading the file");
        }

        // Make the uploaded file publicly accessible
        awS3Client.setObjectAcl(BUCKET_NAME, key, CannedAccessControlList.PublicRead);

        // Return the public URL of the uploaded file
        return "https://" + BUCKET_NAME + ".s3.eu-north-1.amazonaws.com/" + key;

    }
}
