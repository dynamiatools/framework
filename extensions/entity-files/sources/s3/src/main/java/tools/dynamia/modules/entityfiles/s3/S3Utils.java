package tools.dynamia.modules.entityfiles.s3;

import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.core.async.AsyncRequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import tools.dynamia.commons.StringUtils;

import java.io.File;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Utility class to work AWS S3 Buckets
 */
public class S3Utils {


    /**
     * Get credentials from access key and secret key
     *
     * @param accessKey the access key
     * @param secretKey the secret key
     * @return the aws credentials
     */
    public static AwsCredentials getCredentials(String accessKey, String secretKey) {
        return AwsBasicCredentials.create(accessKey, secretKey);
    }

    /**
     * Get credentials provider from access key and secret key
     *
     * @param accessKey the access key
     * @param secretKey the secret key
     * @return the aws credentials provider
     */
    public static AwsCredentialsProvider credentialsProvider(String accessKey, String secretKey) {
        return () -> getCredentials(accessKey, secretKey);
    }

    /**
     * Build S3AsyncClient
     *
     * @param accessKey the access key
     * @param secretKey the secret key
     * @param region    the region
     * @return the S3AsyncClient
     */
    public static S3AsyncClientBuilder buildS3AsyncClient(String accessKey, String secretKey, String region) {
        return S3AsyncClient.builder()
                .credentialsProvider(() -> getCredentials(accessKey, secretKey))
                .region(Region.of(region));

    }

    /**
     * Upload file to S3 bucket
     *
     * @param s3Client   the S3AsyncClient
     * @param bucketName the bucket name
     * @param key        the key
     * @param file       the file
     * @return the CompletableFuture
     */
    public static CompletableFuture<PutObjectResponse> uploadFile(S3AsyncClient s3Client, String bucketName, String key, File file) {
        return s3Client.putObject(PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build(), AsyncRequestBody.fromFile(file));
    }

    /**
     * Delete file from S3 bucket
     *
     * @param s3Cliente  the S3AsyncClient
     * @param bucketName the bucket name
     * @param key        the key
     * @return the CompletableFuture
     */
    public static CompletableFuture<DeleteObjectResponse> deleteFile(S3AsyncClient s3Cliente, String bucketName, String key) {
        DeleteObjectRequest request = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build();

        return s3Cliente.deleteObject(request);
    }

    /**
     * Generate presigned URL for S3 object
     *
     * @param bucketName the bucket name
     * @param key        the key
     * @param duration   the duration
     * @param accessKey  the access key
     * @param secretKey  the secret key
     * @param region     the region
     * @return the presigned URL
     */
    public static PresignedGetObjectRequest generatePresignedObjetRequest(String bucketName, String key, Duration duration,
                                                                          String accessKey, String secretKey, String region) {

        try (S3Presigner presigner = S3Presigner.builder()
                .credentialsProvider(credentialsProvider(accessKey, secretKey))
                .region(Region.of(region))
                .build()) {

            GetObjectRequest objectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                    .signatureDuration(duration)
                    .getObjectRequest(objectRequest)
                    .build();

            return presigner.presignGetObject(presignRequest);
        }
    }

    /**
     * Generate static URL for S3 object
     *
     * @param bucketName the bucket name
     * @param key        the key
     * @param region     the region
     * @return the static URL
     */
    public static String generateStaticURL(String bucketName, String key, String region) {
        String endpoint = "s3." + region + ".amazonaws.com";
        key = key.replace(" ", "%20");
        return String.format("https://%s.%s/%s", bucketName, endpoint, key);
    }

    /**
     * Get regions
     *
     * @return the list of regions
     */
    public static List<String> getRegions() {
        return Region.regions().stream().map(Region::id).toList();
    }

    /**
     * Check if object exists in S3 bucket
     *
     * @param client     the S3AsyncClient
     * @param bucketName the bucket name
     * @param key        the key
     * @return true if object exists, false otherwise
     */
    public static boolean objectExists(S3AsyncClient client, String bucketName, String key) {
        try {
            getObjectAttributes(client, bucketName, key).wait();
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    /**
     * Get object attributes
     *
     * @param client     the S3AsyncClient
     * @param bucketName the bucket name
     * @param key        the key
     * @return the CompletableFuture
     */
    public static CompletableFuture<GetObjectAttributesResponse> getObjectAttributes(S3AsyncClient client, String bucketName, String key) {
        return client.getObjectAttributes(builder -> builder.bucket(bucketName).key(key));
    }

    /**
     * Head object
     *
     * @param client     the S3AsyncClient
     * @param bucketName the bucket name
     * @param key        the key
     * @return the CompletableFuture
     */
    public static CompletableFuture<HeadObjectResponse> headObject(S3AsyncClient client, String bucketName, String key) {
        return client.headObject(builder -> builder.bucket(bucketName).key(key));
    }

    /**
     * Download file
     *
     * @param client      the S3AsyncClient
     * @param bucketName  the bucket name
     * @param key         the key
     * @param destination the destination
     * @return the CompletableFuture
     */
    public static CompletableFuture<GetObjectResponse> downloadFile(S3AsyncClient client, String bucketName, String key, File destination) {
        return downloadFile(client, bucketName, key, destination.toPath());
    }

    /**
     * Download file
     *
     * @param client      the S3AsyncClient
     * @param bucketName  the bucket name
     * @param key         the key
     * @param destination the destination
     * @return the CompletableFuture
     */
    public static CompletableFuture<GetObjectResponse> downloadFile(S3AsyncClient client, String bucketName, String key, Path destination) {
        GetObjectRequest request = GetObjectRequest
                .builder()
                .bucket(bucketName)
                .key(key)
                .build();
        return client.getObject(request, destination);
    }

    /**
     * Download file
     *
     * @param client     the S3AsyncClient
     * @param bucketName the bucket name
     * @param key        the key
     * @return the File
     */
    public static File downloadFile(S3AsyncClient client, String bucketName, String key) {
        try {
            File tmpFile = File.createTempFile("tmpFile", StringUtils.getFilenameExtension(key));
            downloadFile(client, bucketName, key, tmpFile).wait();
            return tmpFile;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
