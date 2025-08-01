package com.zsoft.gloody.gloodypayroll.services.filestore;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.amazonaws.util.IOUtils;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

@Profile({"staging","preprod", "uat","prod"})
@Service
@RequiredArgsConstructor
public class S3FileStore implements FileStoreService {
    private static final Logger log = LoggerFactory.getLogger(S3FileStore.class);
    private final AmazonS3 s3;

    /**
     * Upload a file to S3 bucket
     *
     * @param path        the path in the bucket to where to upload the file
     * @param filename    the key of the file on the bucket
     * @param optionalMetadata    the file metadata options
     * @param inputStream the file as inputStream
     */
    @Override
    public void save(String path, String filename, Optional<Map<String, String>> optionalMetadata, InputStream inputStream) {
        log.debug("Requesting to upload a file {} to {} ", filename, path);
        try {
            ObjectMetadata objectMetadata = new ObjectMetadata();
            optionalMetadata.ifPresent(map -> {
                if (!map.isEmpty()) {
                    map.forEach(objectMetadata::addUserMetadata);
                }
            });
            s3.putObject(path, filename, inputStream, objectMetadata);
        } catch (Exception ex) {
            log.error("Failed to save file to S3 bucket storage {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    /**
     * Retrieve file from the S3 bucket
     *
     * @param path     the path to the file
     * @param filename the key of the file on the bucket
     * @return the file as byte of array
     */
    @Override
    public byte[] download(String path, String filename) {
        log.debug("Requesting to download the file {} from {} ", filename, path);
        try {
            S3Object object = s3.getObject(path, filename);
            S3ObjectInputStream inputStream = object.getObjectContent();
            return IOUtils.toByteArray(inputStream);
        } catch (AmazonServiceException | IOException ex) {
            log.error("Failed to retrieve file from S3 bucket : {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }


    @Override
    public void delete(String path, String filename) {
        log.debug("Requesting to delete from S3 the file {} from {} ", filename, path);
        try {
            if (filename != null) {
                if (s3.doesObjectExist(path, filename)) {
                    s3.deleteObject(path, filename);
                }
            }
        } catch (AmazonServiceException ex) {
            log.error("Failed to delete file from S3 bucket : {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }
}
