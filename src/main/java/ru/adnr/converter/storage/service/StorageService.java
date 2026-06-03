package ru.adnr.converter.storage.service;

import java.io.InputStream;

public interface StorageService {

    InputStream download(String bucket, String objectKey);

    void upload(String bucket, String objectKey, byte[] content, String contentType);
}
