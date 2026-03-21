package moe.waifuvault.models;

import moe.waifuvault.WaifuException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

/**
 * Options for uploading a file or URL to WaifuVault.
 * <p>
 * Use the static factory methods to create an instance:
 * <ul>
 *   <li>{@link #fromFile(File)} or {@link #fromFile(String)} - upload a file from disk</li>
 *   <li>{@link #fromBytes(byte[], String)} - upload a file from a byte array</li>
 *   <li>{@link #fromStream(InputStream, String)} - upload a file from an input stream</li>
 *   <li>{@link #fromUrl(String)} - upload a file from a URL</li>
 * </ul>
 * Then chain optional settings using the builder methods.
 */
public class FileUpload {

    /**
     * The type of upload source.
     */
    public enum Type {
        /** Upload from a file path on disk. */
        FILE_PATH,
        /** Upload from raw bytes. */
        FILE_BYTES,
        /** Upload from a URL. */
        URL
    }

    private Type type;
    private File file;
    private byte[] bytes;
    private String filename;
    private String url;
    private String expires;
    private Boolean hideFilename;
    private String password;
    private Boolean oneTimeDownload;
    private String bucketToken;
    private String clientIP;

    private FileUpload() {
    }

    /**
     * Create an upload from a file on disk.
     *
     * @param file the file to upload
     * @return a new FileUpload instance
     */
    public static FileUpload fromFile(final File file) {
        final FileUpload fu = new FileUpload();
        fu.type = Type.FILE_PATH;
        fu.file = file;
        fu.filename = file.getName();
        return fu;
    }

    /**
     * Create an upload from a file path on disk.
     *
     * @param path the path to the file to upload
     * @return a new FileUpload instance
     */
    public static FileUpload fromFile(final String path) {
        return fromFile(new File(path));
    }

    /**
     * Create an upload from raw bytes.
     *
     * @param data     the file data as a byte array
     * @param filename the filename to use for the upload
     * @return a new FileUpload instance
     */
    public static FileUpload fromBytes(final byte[] data, final String filename) {
        final FileUpload fu = new FileUpload();
        fu.type = Type.FILE_BYTES;
        fu.bytes = data;
        fu.filename = filename;
        return fu;
    }

    /**
     * Create an upload from an input stream. The stream will be fully read into memory.
     *
     * @param stream   the input stream to read
     * @param filename the filename to use for the upload
     * @return a new FileUpload instance
     */
    public static FileUpload fromStream(final InputStream stream, final String filename) {
        try {
            return fromBytes(stream.readAllBytes(), filename);
        } catch (final IOException e) {
            throw new WaifuException(0, "IO Error", "Failed to read input stream: " + e.getMessage());
        }
    }

    /**
     * Create an upload from a URL to a file on the internet.
     *
     * @param url the URL to the file
     * @return a new FileUpload instance
     */
    public static FileUpload fromUrl(final String url) {
        final FileUpload fu = new FileUpload();
        fu.type = Type.URL;
        fu.url = url;
        return fu;
    }

    /**
     * Set the expiration time for the file.
     *
     * @param expires a string containing a number and a unit ({@code m} for minutes, {@code h} for hours, {@code d} for days).
     *                For example: {@code "1h"} would be 1 hour and {@code "1d"} would be 1 day.
     * @return this instance for chaining
     */
    public FileUpload expires(final String expires) {
        this.expires = expires;
        return this;
    }

    /**
     * Set whether the filename should be hidden in the URL.
     *
     * @param hide if true, the filename will not appear in the URL
     * @return this instance for chaining
     */
    public FileUpload hideFilename(final boolean hide) {
        this.hideFilename = hide;
        return this;
    }

    /**
     * Set a password to encrypt the file.
     *
     * @param password the password to use for encryption
     * @return this instance for chaining
     */
    public FileUpload password(final String password) {
        this.password = password;
        return this;
    }

    /**
     * Set whether the file should be deleted after first access.
     *
     * @param oneTimeDownload if true, the file will be deleted after it is first downloaded
     * @return this instance for chaining
     */
    public FileUpload oneTimeDownload(final boolean oneTimeDownload) {
        this.oneTimeDownload = oneTimeDownload;
        return this;
    }

    /**
     * Associate this file with a bucket.
     *
     * @param bucketToken the token of the bucket to associate with
     * @return this instance for chaining
     */
    public FileUpload bucketToken(final String bucketToken) {
        this.bucketToken = bucketToken;
        return this;
    }

    /**
     * Set a custom client IP to forward to WaifuVault.
     *
     * @param clientIP the IP address to forward
     * @return this instance for chaining
     */
    public FileUpload clientIP(final String clientIP) {
        this.clientIP = clientIP;
        return this;
    }

    /** @return the upload type */
    public Type getType() {
        return type;
    }

    /** @return true if this is a file upload (not a URL upload) */
    public boolean isFileUpload() {
        return type != Type.URL;
    }

    /**
     * Read and return the file bytes. For file path uploads, the file is read from disk.
     *
     * @return the file data as a byte array
     * @throws WaifuException if reading the file fails
     */
    public byte[] getFileBytes() {
        if (type == Type.FILE_BYTES) {
            return bytes;
        }
        if (type == Type.FILE_PATH) {
            try {
                return Files.readAllBytes(file.toPath());
            } catch (final IOException e) {
                throw new WaifuException(0, "IO Error", "Failed to read file: " + e.getMessage());
            }
        }
        throw new IllegalStateException("Not a file upload");
    }

    /** @return the filename for this upload */
    public String getFilename() {
        return filename;
    }

    /** @return the URL for URL-based uploads */
    public String getUrl() {
        return url;
    }

    /** @return the expiration string, or null if not set */
    public String getExpires() {
        return expires;
    }

    /** @return whether to hide the filename, or null if not set */
    public Boolean getHideFilename() {
        return hideFilename;
    }

    /** @return the encryption password, or null if not set */
    public String getPassword() {
        return password;
    }

    /** @return whether this is a one-time download, or null if not set */
    public Boolean getOneTimeDownload() {
        return oneTimeDownload;
    }

    /** @return the bucket token, or null if not set */
    public String getBucketToken() {
        return bucketToken;
    }

    /** @return the client IP to forward, or null if not set */
    public String getClientIP() {
        return clientIP;
    }
}
