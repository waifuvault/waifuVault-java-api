package moe.waifuvault.mocks;

import moe.waifuvault.models.GenericSuccess;
import moe.waifuvault.models.WaifuAlbum;
import moe.waifuvault.models.WaifuBucket;
import moe.waifuvault.models.WaifuFile;
import moe.waifuvault.models.WaifuResponseOptions;

import java.util.List;

public final class MockData {

    public static final String TOKEN = "123-fake-street";
    public static final String BUCKET_TOKEN = "123-fake-street-bucket";
    public static final String ALBUM_TOKEN = "3ed6dafa-b56a-4207-b004-852abb6fea11";
    public static final String ALBUM_BUCKET = "8c3d4527-4cea-4cb8-8171-002b158693ab";
    public static final String PUBLIC_TOKEN = "c18ff3cb-442d-44e7-b5d1-c43a83b3a1a4";
    public static final String FILE_URL = "https://waifuvault.moe/f/1710111505084/08.png";

    public static final WaifuFile FILE_UNPROTECTED = new WaifuFile(
            TOKEN, FILE_URL,
            new WaifuResponseOptions(false, false, false),
            1234L, BUCKET_TOKEN, 1, 0, null);

    public static final WaifuFile FILE_PROTECTED = new WaifuFile(
            TOKEN, FILE_URL,
            new WaifuResponseOptions(false, false, true),
            "1234", null, 2, 0, null);

    public static final WaifuBucket BUCKET = new WaifuBucket(
            BUCKET_TOKEN, List.of(FILE_UNPROTECTED), List.of());

    public static final WaifuAlbum ALBUM_WITH_FILES = new WaifuAlbum(
            ALBUM_TOKEN, ALBUM_BUCKET, PUBLIC_TOKEN,
            "album1", List.of(FILE_UNPROTECTED), 0);

    public static final WaifuAlbum ALBUM_EMPTY = new WaifuAlbum(
            ALBUM_TOKEN, ALBUM_BUCKET, PUBLIC_TOKEN,
            "album2", List.of(), 0);

    public static final GenericSuccess SUCCESS_DELETED = new GenericSuccess(true, "deleted");
    public static final GenericSuccess SUCCESS_SHARED = new GenericSuccess(true, "sharedAlbum.foo");

    public record ErrorBody(int status, String name, String message) {
    }

    public static final ErrorBody ERROR_BAD_REQUEST = new ErrorBody(400, "Bad Request", "invalid");

    private MockData() {
    }
}
