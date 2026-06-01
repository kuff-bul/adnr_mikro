package ru.adnr.converter.storage.service;

import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.errors.ErrorResponseException;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.adnr.converter.storage.exception.StorageException;

@Service
@RequiredArgsConstructor
public class MinioStorageService {

    private final MinioClient minioClient;

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

    public boolean exists(String bucket, String objectKey) {
        try {
            minioClient.statObject(StatObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .build());
            return true;
        } catch (ErrorResponseException ex) {
            if ("NoSuchKey".equals(ex.errorResponse().code())
                    || "NoSuchObject".equals(ex.errorResponse().code())
                    || "NoSuchBucket".equals(ex.errorResponse().code())) {
                return false;
            }
            throw new StorageException("Failed to check object '%s' in bucket '%s'"
                    .formatted(objectKey, bucket), ex);
        } catch (Exception ex) {
            throw new StorageException("Failed to check object '%s' in bucket '%s'"
                    .formatted(objectKey, bucket), ex);
        }
    }
}
