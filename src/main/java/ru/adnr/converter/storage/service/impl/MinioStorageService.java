package ru.adnr.converter.storage.service.impl;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.adnr.converter.storage.exception.StorageException;
import ru.adnr.converter.storage.service.StorageService;

@Service
@RequiredArgsConstructor
public class MinioStorageService implements StorageService {

    private final MinioClient minioClient;

    @Override
    public InputStream download(String bucket, String objectKey) {
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
        } catch (Exception ex) {
            throw new StorageException("Failed to download object '%s' from bucket '%s'"
                    .formatted(objectKey, bucket), ex);
        }
    }

    @Override
    public void upload(String bucket, String objectKey, byte[] content, String contentType) {
        try {
            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(new ByteArrayInputStream(content), (long) content.length, -1L)
                    .contentType(contentType)
                    .build());
        } catch (Exception ex) {
            throw new StorageException("Failed to upload object '%s' to bucket '%s'"
                    .formatted(objectKey, bucket), ex);
        }
    }
}
