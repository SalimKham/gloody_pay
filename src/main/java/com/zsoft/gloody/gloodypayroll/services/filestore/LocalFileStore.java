package com.zsoft.gloody.gloodypayroll.services.filestore;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;

@Profile({"test", "dev"})
@Service
@RequiredArgsConstructor
public class LocalFileStore implements FileStoreService {

    private static final Logger log = LoggerFactory.getLogger(LocalFileStore.class);

    @Override
    public void save(String path, String filename, Optional<Map<String, String>> optionalMetadata, InputStream inputStream) {
        log.info("Requesting to upload a file {} to {} ", filename, path);
        try{
            path = prefixWithUserHomeDirectory(path);
            Files.createDirectories(Paths.get(path));
            String fullFilePath = String.format("%s/%s",path,filename);
            try(inputStream){
                Files.copy(inputStream,Paths.get(fullFilePath),StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException ex) {
            log.error("Failed to save file to Local storage {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public byte[] download(String path, String filename) {
        log.info("Requesting to download the file {} from {} ", filename, path);
        try {
            path = prefixWithUserHomeDirectory(path);
            Path fullDirectoryPath = Paths.get(String.format("%s/%s", path, filename));
            if (!fullDirectoryPath.toFile().exists()) {
                throw new Exception("File download failed : file doesn't exist or could not be accessed !");
            }
            return Files.readAllBytes(fullDirectoryPath);
        } catch (Exception ex) {
            log.error("Failed to retrieve file from Local storage : {}", ex.getMessage());
            throw new RuntimeException(ex.getMessage());
        }
    }

    @Override
    public void delete(String path, String filename) {
        log.info("Requesting to delete the file {} from {} ", filename, path);
        path = prefixWithUserHomeDirectory(path);
        if (filename != null) {
            String fullFilePath = String.format("%s/%s", path, filename);
            log.info(fullFilePath);
            try {
                Files.deleteIfExists(Paths.get(fullFilePath));
            } catch (IOException ex) {
                log.error("failed to delete file from local storage :{}",ex.getMessage());
                throw new RuntimeException(ex.getMessage());
            }
        }
    }

    private String prefixWithUserHomeDirectory(String path) {
        path = String.format("%s/%s", System.getProperty("user.home"), path);
        return path;
    }


}
