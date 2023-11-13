package com.zsoft.gloody.gloodypayroll.services;

import java.io.InputStream;
import java.util.Map;

public interface FileStoreService {
    void save(String path, String filename, Map<String, String> metadata, InputStream inputStream, long size);

    byte[] download(String path, String filename);

    void delete(String path, String filename);
}
