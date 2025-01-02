package com.zsoft.gloody.gloodypayroll.services;

import com.zsoft.gloody.gloodypayroll.configs.properties.ServerProperties;
import com.zsoft.gloody.gloodypayroll.exceptions.ApiRuntimeException;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Profile({"dev"})
public class FileStoreLocalService implements FileStoreService {
    private static final String FILE_PATH_FORMAT = "%s/%s";
    private final ServerProperties serverProperties;

    @Override
    public void save(String path, String filename, Map<String, String> metadata, InputStream inputStream, long size) {
        try {
            path = createFileDirectoryOrThrow(path);
            String fullFilePath = String.format(FILE_PATH_FORMAT, path, filename);
            Files.copy(inputStream, Paths.get(fullFilePath), StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception ex) {
            throw new ApiRuntimeException("file.upload.failed", ex.getCause());
        }
    }

    @Override
    public byte[] download(String path, String filename) {
        try {
            path = prefixWithUserHomeDirectory(path);
            Path fullDirectoryPath = Paths.get(String.format(FILE_PATH_FORMAT, path, filename));
            if (!fullDirectoryPath.toFile().exists()) {
                return new byte[0];
            }
            return Files.readAllBytes(fullDirectoryPath);
        } catch (IOException ex) {
            throw new ApiRuntimeException("file.download.failed", ex.getCause());
        }
    }

    @Override
    public void delete(String path, String filename) {
        path = prefixWithUserHomeDirectory(path);
        if (filename != null) {
            String fullFilePath = String.format(FILE_PATH_FORMAT, path, filename);
            try {
                if (Files.exists(Paths.get(fullFilePath))) {
                    Files.delete(Paths.get(fullFilePath));
                }
            } catch (IOException ex) {
                throw new ApiRuntimeException("file.delete.failed", ex.getCause());
            }
        }
    }

    private String createFileDirectoryOrThrow(String path) {
        try {
            path = prefixWithUserHomeDirectory(path);
            Path fullDirectoryPath = Paths.get(path);
            if (!fullDirectoryPath.toFile().exists()) {
                Files.createDirectories(fullDirectoryPath);
            }
            return path;
        } catch (Exception ex) {
            throw new ApiRuntimeException("file.download.failed", ex.getCause());
        }
    }

    private String prefixWithUserHomeDirectory(String path) {
        String uploadPath = serverProperties.getConfiguration().getFileStorePath();
        path = uploadPath + "/" + path;
        return path;
    }
}
