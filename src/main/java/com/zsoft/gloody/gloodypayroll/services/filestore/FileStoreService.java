package com.zsoft.gloody.gloodypayroll.services.filestore;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

public interface FileStoreService {
    void save(String path, String filename, Optional<Map<String,String>> optionalMetadata, InputStream inputStream);
    byte[] download(String path,String filename);
    void delete(String path,String filename);
}
