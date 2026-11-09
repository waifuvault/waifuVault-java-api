package moe.waifuvault;

import moe.waifuvault.models.*;

import java.net.http.HttpClient;

/**
 * Main entry point for the WaifuVault Java SDK.
 * <p>
 * Create an instance and use it to interact with the WaifuVault API.
 * Implements {@link AutoCloseable} so the underlying HTTP client is properly released.
 * <pre>{@code
 * try (final Waifuvault api = new Waifuvault()) {
 *     final WaifuFile file = api.uploadFile(FileUpload.fromFile("./image.png"));
 *     System.out.println(file.url());
 * }
 * }</pre>
 *
 * @see <a href="https://waifuvault.moe">WaifuVault</a>
 */
public class Waifuvault implements AutoCloseable {

    private final HttpService http;
    private final FileApi fileApi;
    private final BucketApi bucketApi;
    private final AlbumApi albumApi;

    /**
     * Create a new WaifuVault API client.
     */
    public Waifuvault() {
        this(HttpClient.newHttpClient());
    }

    /**
     * Create a new WaifuVault API client with a custom base URL.
     *
     * @param baseUrl the base URL (e.g. {@code "https://waifuvault.moe"})
     */
    public Waifuvault(final String baseUrl) {
        this(baseUrl, HttpClient.newHttpClient());
    }

    /**
     * Create a new WaifuVault API client with a custom HTTP client.
     *
     * @param httpClient the HTTP client to use for requests
     */
    public Waifuvault(final HttpClient httpClient) {
        this.http = new HttpService(httpClient);
        this.fileApi = new FileApi(http);
        this.bucketApi = new BucketApi(http);
        this.albumApi = new AlbumApi(http);
    }

    /**
     * Create a new WaifuVault API client with a custom base URL and HTTP client.
     *
     * @param baseUrl    the base URL (e.g. {@code "https://waifuvault.moe"})
     * @param httpClient the HTTP client to use for requests
     */
    public Waifuvault(final String baseUrl, final HttpClient httpClient) {
        this(httpClient);
        this.http.setBaseUrl(baseUrl);
    }


    @Override
    public void close() {
        http.close();
    }

    /**
     * Upload a file to WaifuVault.
     *
     * @param options the upload options created via {@link FileUpload} factory methods
     * @return the uploaded file metadata
     * @throws WaifuException if the upload fails
     * @see FileUpload#fromFile(String)
     * @see FileUpload#fromBytes(byte[], String)
     */
    public WaifuFile uploadFile(final FileUpload options) {
        return fileApi.uploadFile(options);
    }

    /**
     * Get information about an uploaded file.
     *
     * @param token the file token from the upload response
     * @return the file metadata with {@code retentionPeriod} as an epoch number
     * @throws WaifuException if the request fails
     */
    public WaifuFile fileInfo(final String token) {
        return fileApi.fileInfo(token, false);
    }

    /**
     * Get information about an uploaded file.
     *
     * @param token     the file token from the upload response
     * @param formatted if true, {@code retentionPeriod} will be a human-readable string
     *                  (e.g. "332 days 7 hours 18 minutes 8 seconds");
     *                  if false, it will be an epoch timestamp
     * @return the file metadata
     * @throws WaifuException if the request fails
     */
    public WaifuFile fileInfo(final String token, final boolean formatted) {
        return fileApi.fileInfo(token, formatted);
    }

    /**
     * Delete a file by its token.
     *
     * @param token the token of the file to delete
     * @return a success response
     * @throws WaifuException if the token is invalid or the request fails
     */
    public GenericSuccess deleteFile(final String token) {
        return fileApi.deleteFile(token);
    }

    /**
     * Download a file by its token.
     *
     * @param token the file token
     * @return the file contents as a byte array
     * @throws WaifuException if the request fails
     */
    public byte[] getFile(final String token) {
        return fileApi.getFile(token, null);
    }

    /**
     * Download an encrypted file by its token.
     *
     * @param token    the file token
     * @param password the password to decrypt the file
     * @return the file contents as a byte array
     * @throws WaifuException if the password is incorrect or the request fails
     */
    public byte[] getFile(final String token, final String password) {
        return fileApi.getFile(token, password);
    }

    /**
     * Download a file by its unique filename identifier.
     * <p>
     * The filename is the epoch/filename (e.g. {@code "1710111505084/08.png"}).
     * For files with hidden filenames, it is just the epoch with extension (e.g. {@code "1710111505084.png"}).
     *
     * @param filename the unique filename identifier
     * @return the file contents as a byte array
     * @throws WaifuException if the request fails
     */
    public byte[] getFileByFilename(final String filename) {
        return fileApi.getFileByFilename(filename, null);
    }

    /**
     * Download an encrypted file by its unique filename identifier.
     *
     * @param filename the unique filename identifier
     * @param password the password to decrypt the file
     * @return the file contents as a byte array
     * @throws WaifuException if the password is incorrect or the request fails
     */
    public byte[] getFileByFilename(final String filename, final String password) {
        return fileApi.getFileByFilename(filename, password);
    }

    /**
     * Modify aspects of an uploaded file such as password, expiry, or filename visibility.
     *
     * @param token the token of the file to modify
     * @param opts  the modifications to apply
     * @return the updated file metadata
     * @throws WaifuException if the request fails
     */
    public WaifuFile modifyEntry(final String token, final ModifyEntryPayload opts) {
        return fileApi.modifyEntry(token, opts);
    }

    /**
     * Create a new bucket. Only one bucket per IP is allowed.
     *
     * @return the created bucket with its token
     * @throws WaifuException if the request fails
     */
    public WaifuBucket createBucket() {
        return bucketApi.createBucket();
    }

    /**
     * Get a bucket and all the files it contains.
     *
     * @param bucketToken the token of the bucket to retrieve
     * @return the bucket with its files and albums
     * @throws WaifuException if the request fails
     */
    public WaifuBucket getBucket(final String bucketToken) {
        return bucketApi.getBucket(bucketToken);
    }

    /**
     * Delete a bucket and all files it contains.
     *
     * @param bucketToken the token of the bucket to delete
     * @return a success response
     * @throws WaifuException if the token is invalid or the request fails
     */
    public GenericSuccess deleteBucket(final String bucketToken) {
        return bucketApi.deleteBucket(bucketToken);
    }

    /**
     * Create a new album in a bucket.
     *
     * @param name        the name of the album
     * @param bucketToken the token of the bucket to create the album in
     * @return the created album
     * @throws WaifuException if the request fails
     */
    public WaifuAlbum createAlbum(final String name, final String bucketToken) {
        return albumApi.createAlbum(name, bucketToken);
    }

    /**
     * Get an album and all of its files.
     *
     * @param albumToken the private album token
     * @return the album with its files
     * @throws WaifuException if the request fails
     */
    public WaifuAlbum getAlbum(final String albumToken) {
        return albumApi.getAlbum(albumToken);
    }

    /**
     * Delete an album. Files remain in the bucket.
     *
     * @param albumToken the private album token
     * @return a success response
     * @throws WaifuException if the request fails
     */
    public GenericSuccess deleteAlbum(final String albumToken) {
        return albumApi.deleteAlbum(albumToken, false);
    }

    /**
     * Delete an album with an option to also delete its files.
     *
     * @param albumToken  the private album token
     * @param deleteFiles if true, permanently deletes the files; if false, files remain in the bucket
     * @return a success response
     * @throws WaifuException if the request fails
     */
    public GenericSuccess deleteAlbum(final String albumToken, final boolean deleteFiles) {
        return albumApi.deleteAlbum(albumToken, deleteFiles);
    }

    /**
     * Add files to an album. Files must be in the same bucket as the album.
     *
     * @param albumToken the private album token
     * @param fileTokens the file tokens to add to the album
     * @return the updated album with the new files
     * @throws WaifuException if the request fails
     */
    public WaifuAlbum associateFiles(final String albumToken, final String... fileTokens) {
        return albumApi.associateFiles(albumToken, fileTokens);
    }

    /**
     * Remove files from an album. The files remain in the bucket.
     *
     * @param albumToken the private album token
     * @param fileTokens the file tokens to remove from the album
     * @return the updated album without the removed files
     * @throws WaifuException if the request fails
     */
    public WaifuAlbum disassociateFiles(final String albumToken, final String... fileTokens) {
        return albumApi.disassociateFiles(albumToken, fileTokens);
    }

    /**
     * Share an album so its contents can be accessed from a public URL.
     *
     * @param albumToken the private album token
     * @return the public URL of the shared album
     * @throws WaifuException if the request fails
     */
    public String shareAlbum(final String albumToken) {
        return albumApi.shareAlbum(albumToken);
    }

    /**
     * Revoke public sharing of an album. The public URL is destroyed.
     * If the album is shared again later, a new URL will be generated.
     *
     * @param albumToken the private album token
     * @return a success response
     * @throws WaifuException if the request fails
     */
    public GenericSuccess revokeAlbum(final String albumToken) {
        return albumApi.revokeAlbum(albumToken);
    }

    /**
     * Download all files from an album as a ZIP file.
     *
     * @param albumToken the private or public album token
     * @return the ZIP file as a byte array
     * @throws WaifuException if the request fails
     */
    public byte[] downloadAlbum(final String albumToken) {
        return albumApi.downloadAlbum(albumToken, null);
    }

    /**
     * Download selected files from an album as a ZIP file.
     *
     * @param albumToken the private or public album token
     * @param fileIds    the IDs of the files to download (from {@link WaifuFile#id()})
     * @return the ZIP file as a byte array
     * @throws WaifuException if the request fails
     */
    public byte[] downloadAlbum(final String albumToken, final int... fileIds) {
        return albumApi.downloadAlbum(albumToken, fileIds);
    }
}
