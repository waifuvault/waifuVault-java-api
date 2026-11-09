package moe.waifuvault;

import moe.waifuvault.mocks.MockData;
import moe.waifuvault.mocks.MockResponses;
import moe.waifuvault.models.FileUpload;
import moe.waifuvault.models.GenericSuccess;
import moe.waifuvault.models.ModifyEntryPayload;
import moe.waifuvault.models.WaifuAlbum;
import moe.waifuvault.models.WaifuBucket;
import moe.waifuvault.models.WaifuFile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class WaifuvaultTest {

    private static final String BASE_URL = "https://waifuvault.moe/rest";

    @Mock
    private HttpClient mockClient;

    @Captor
    private ArgumentCaptor<HttpRequest> requestCaptor;

    private Waifuvault api;

    @BeforeEach
    void setUp() {
        api = new Waifuvault(mockClient);
    }

    private HttpRequest captureRequest() throws Exception {
        verify(mockClient).send(requestCaptor.capture(), any());
        return requestCaptor.getValue();
    }

    private List<HttpRequest> captureRequests(final int count) throws Exception {
        verify(mockClient, times(count)).send(requestCaptor.capture(), any());
        return requestCaptor.getAllValues();
    }

    @Nested
    class UploadFile {

        @Test
        void shouldUploadFromBytes() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            final WaifuFile result = api.uploadFile(
                    FileUpload.fromBytes("test data".getBytes(), "test.txt"));

            assertEquals(MockData.TOKEN, result.token());
            final HttpRequest req = captureRequest();
            assertEquals("PUT", req.method());
            assertTrue(req.uri().toString().startsWith(BASE_URL));
        }

        @Test
        void shouldIncludeQueryParams() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_PROTECTED)).when(mockClient).send(any(), any());

            api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")
                    .password("foo").hideFilename(true).expires("2d"));

            final String uri = captureRequest().uri().toString();
            assertTrue(uri.contains("expires=2d"));
            assertTrue(uri.contains("hide_filename=true"));
        }

        @Test
        void shouldHandleEmptyByteArray() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            final WaifuFile result = api.uploadFile(
                    FileUpload.fromBytes(new byte[0], "empty.txt"));

            assertEquals(MockData.TOKEN, result.token());
            assertEquals("PUT", captureRequest().method());
        }

        @Test
        void shouldHandleLargeByteArray() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            final byte[] largeData = new byte[1024 * 1024];
            final WaifuFile result = api.uploadFile(
                    FileUpload.fromBytes(largeData, "large-file.bin"));

            assertEquals(MockData.TOKEN, result.token());
            assertEquals("PUT", captureRequest().method());
        }

        @Test
        void shouldHandleBinaryData() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            final byte[] pngHeader = new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A};
            api.uploadFile(FileUpload.fromBytes(pngHeader, "test.png"));

            assertEquals("PUT", captureRequest().method());
        }

        @Test
        void shouldHandleSpecialCharactersInData() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            api.uploadFile(FileUpload.fromBytes("Hello \u00e9mojis \u7279\u6b8a".getBytes(), "special-chars.txt"));

            assertEquals("PUT", captureRequest().method());
        }

        @Test
        void shouldExtractFilenameFromPath() {
            final FileUpload upload = FileUpload.fromFile("/very/deep/nested/path/final-file.jpg");
            assertEquals("final-file.jpg", upload.getFilename());
        }

        @Test
        void shouldThrowOnApiError() throws Exception {
            doReturn(MockResponses.json(400, MockData.ERROR_BAD_REQUEST)).when(mockClient).send(any(), any());

            assertThrows(WaifuException.class, () ->
                    api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")));
        }

        @Nested
        class IpAddress {

            @Test
            void shouldSendClientIPHeaders() throws Exception {
                doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

                api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")
                        .clientIP("192.168.1.100"));

                final HttpRequest req = captureRequest();
                assertEquals("192.168.1.100", req.headers().firstValue("X-Forwarded-For").orElse(null));
                assertEquals("192.168.1.100", req.headers().firstValue("X-Real-IP").orElse(null));
            }

            @Test
            void shouldHandleIPv6() throws Exception {
                doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

                api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")
                        .clientIP("2001:db8::1"));

                final HttpRequest req = captureRequest();
                assertEquals("2001:db8::1", req.headers().firstValue("X-Forwarded-For").orElse(null));
                assertEquals("2001:db8::1", req.headers().firstValue("X-Real-IP").orElse(null));
            }

            @Test
            void shouldSendClientIPWithOtherParams() throws Exception {
                doReturn(MockResponses.json(200, MockData.FILE_PROTECTED)).when(mockClient).send(any(), any());

                api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")
                        .password("secret").expires("1d").hideFilename(true)
                        .clientIP("172.16.0.1"));

                final HttpRequest req = captureRequest();
                assertEquals("172.16.0.1", req.headers().firstValue("X-Forwarded-For").orElse(null));
                assertTrue(req.uri().toString().contains("expires=1d"));
                assertTrue(req.uri().toString().contains("hide_filename=true"));
            }

            @Test
            void shouldNotSendIPHeadersWhenNotProvided() throws Exception {
                doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

                api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt"));

                final HttpRequest req = captureRequest();
                assertTrue(req.headers().firstValue("X-Forwarded-For").isEmpty());
                assertTrue(req.headers().firstValue("X-Real-IP").isEmpty());
            }

            @Test
            void shouldHandleLocalhostIP() throws Exception {
                doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

                api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")
                        .clientIP("127.0.0.1"));

                final HttpRequest req = captureRequest();
                assertEquals("127.0.0.1", req.headers().firstValue("X-Forwarded-For").orElse(null));
                assertEquals("127.0.0.1", req.headers().firstValue("X-Real-IP").orElse(null));
            }

            @Test
            void shouldIncludeBucketTokenInPath() throws Exception {
                doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

                api.uploadFile(FileUpload.fromBytes("data".getBytes(), "file.txt")
                        .clientIP("203.0.113.1").bucketToken("bucket123"));

                assertTrue(captureRequest().uri().toString().contains(BASE_URL + "/bucket123"));
            }
        }
    }

    @Nested
    class FileInfo {

        @Test
        void shouldReturnFileMetadata() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            final WaifuFile result = api.fileInfo(MockData.TOKEN, false);

            assertEquals(MockData.TOKEN, result.token());
            final HttpRequest req = captureRequest();
            assertTrue(req.uri().toString().contains(BASE_URL + "/" + MockData.TOKEN));
            assertEquals("GET", req.method());
        }

        @Test
        void shouldIncludeFormattedParam() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_PROTECTED)).when(mockClient).send(any(), any());

            final WaifuFile result = api.fileInfo(MockData.TOKEN, true);

            assertEquals("1234", result.retentionPeriodString());
            assertTrue(captureRequest().uri().toString().contains("formatted=true"));
        }

        @Test
        void shouldThrowOnApiError() throws Exception {
            doReturn(MockResponses.json(400, MockData.ERROR_BAD_REQUEST)).when(mockClient).send(any(), any());

            assertThrows(WaifuException.class, () -> api.fileInfo(MockData.TOKEN, false));
        }
    }

    @Nested
    class DeleteFile {

        @Test
        void shouldReturnSuccess() throws Exception {
            doReturn(MockResponses.json(200, "true")).when(mockClient).send(any(), any());

            final GenericSuccess result = api.deleteFile(MockData.TOKEN);

            assertTrue(result.success());
            assertEquals("deleted", result.description());
            assertEquals("DELETE", captureRequest().method());
        }

        @Test
        void shouldThrowOnApiError() throws Exception {
            doReturn(MockResponses.json(400, MockData.ERROR_BAD_REQUEST)).when(mockClient).send(any(), any());

            assertThrows(WaifuException.class, () -> api.deleteFile(MockData.TOKEN));
        }
    }

    @Nested
    class GetFile {

        @Test
        void shouldFetchInfoThenDownloadByToken() throws Exception {
            final byte[] fileData = "file content".getBytes();
            final Object infoResp = MockResponses.json(200, MockData.FILE_UNPROTECTED);
            final Object dataResp = MockResponses.bytes(200, fileData);
            doReturn(infoResp).doReturn(dataResp).when(mockClient).send(any(), any());

            final byte[] result = api.getFile(MockData.TOKEN, null);

            assertArrayEquals(fileData, result);
            final List<HttpRequest> requests = captureRequests(2);
            assertEquals("GET", requests.get(0).method());
            assertEquals("GET", requests.get(1).method());
        }

        @Test
        void shouldDownloadByFilename() throws Exception {
            final byte[] fileData = "file content".getBytes();
            doReturn(MockResponses.bytes(200, fileData)).when(mockClient).send(any(), any());

            final byte[] result = api.getFileByFilename("1710111505084/08.png", null);

            assertArrayEquals(fileData, result);
            assertTrue(captureRequest().uri().toString().contains("/f/1710111505084/08.png"));
        }

        @Test
        void shouldSendPasswordHeader() throws Exception {
            doReturn(MockResponses.bytes(200, "encrypted".getBytes())).when(mockClient).send(any(), any());

            api.getFileByFilename("1710111505084/08.png", "secret");

            assertEquals("secret", captureRequest().headers().firstValue("x-password").orElse(null));
        }

        @Test
        void shouldThrowOnIncorrectPassword() throws Exception {
            doReturn(MockResponses.bytes(403, "Forbidden".getBytes())).when(mockClient).send(any(), any());

            final WaifuException ex = assertThrows(WaifuException.class, () ->
                    api.getFileByFilename("1710111505084/08.png", "wrong"));

            assertEquals(403, ex.getStatus());
            assertTrue(ex.getMessage().contains("Password is incorrect"));
        }

        @Test
        void shouldThrowOnApiError() throws Exception {
            doReturn(MockResponses.bytes(400, "{\"status\":400,\"name\":\"Bad Request\",\"message\":\"invalid\"}".getBytes()))
                    .when(mockClient).send(any(), any());

            final WaifuException ex = assertThrows(WaifuException.class, () ->
                    api.getFileByFilename("1710111505084/08.png", "wrong"));

            assertEquals(400, ex.getStatus());
        }
    }

    @Nested
    class ModifyEntry {

        @Test
        void shouldSendPatchWithJson() throws Exception {
            doReturn(MockResponses.json(200, MockData.FILE_UNPROTECTED)).when(mockClient).send(any(), any());

            final WaifuFile result = api.modifyEntry(MockData.TOKEN,
                    new ModifyEntryPayload().password("foo").customExpiry("2d"));

            assertEquals(MockData.TOKEN, result.token());
            final HttpRequest req = captureRequest();
            assertEquals("PATCH", req.method());
            assertTrue(req.headers().firstValue("Content-Type").orElse("").contains("application/json"));
        }

        @Test
        void shouldThrowOnApiError() throws Exception {
            doReturn(MockResponses.json(400, MockData.ERROR_BAD_REQUEST)).when(mockClient).send(any(), any());

            assertThrows(WaifuException.class, () ->
                    api.modifyEntry(MockData.TOKEN, new ModifyEntryPayload().password("x")));
        }
    }

    @Nested
    class Buckets {

        @Nested
        class CreateBucket {

            @Test
            void shouldCreateBucket() throws Exception {
                doReturn(MockResponses.json(200, MockData.BUCKET)).when(mockClient).send(any(), any());

                final WaifuBucket result = api.createBucket();

                assertEquals(MockData.BUCKET_TOKEN, result.token());
                final HttpRequest req = captureRequest();
                assertEquals("GET", req.method());
                assertTrue(req.uri().toString().contains("/bucket/create"));
            }
        }

        @Nested
        class GetBucket {

            @Test
            void shouldGetBucket() throws Exception {
                doReturn(MockResponses.json(200, MockData.BUCKET)).when(mockClient).send(any(), any());

                final WaifuBucket result = api.getBucket(MockData.BUCKET_TOKEN);

                assertEquals(MockData.BUCKET_TOKEN, result.token());
                final HttpRequest req = captureRequest();
                assertEquals("POST", req.method());
                assertTrue(req.uri().toString().contains("/bucket/get"));
            }
        }

        @Nested
        class DeleteBucket {

            @Test
            void shouldDeleteBucket() throws Exception {
                doReturn(MockResponses.json(200, "true")).when(mockClient).send(any(), any());

                final GenericSuccess result = api.deleteBucket(MockData.BUCKET_TOKEN);

                assertTrue(result.success());
                final HttpRequest req = captureRequest();
                assertEquals("DELETE", req.method());
                assertTrue(req.uri().toString().contains("/bucket/" + MockData.BUCKET_TOKEN));
            }

            @Test
            void shouldThrowOnApiError() throws Exception {
                doReturn(MockResponses.json(400, MockData.ERROR_BAD_REQUEST)).when(mockClient).send(any(), any());

                assertThrows(WaifuException.class, () -> api.deleteBucket(MockData.BUCKET_TOKEN));
            }
        }
    }

    @Nested
    class Albums {

        @Nested
        class CreateAlbum {

            @Test
            void shouldCreateAlbumWithFiles() throws Exception {
                doReturn(MockResponses.json(200, MockData.ALBUM_WITH_FILES)).when(mockClient).send(any(), any());

                final WaifuAlbum result = api.createAlbum("album1", MockData.BUCKET_TOKEN);

                assertEquals("album1", result.name());
                final HttpRequest req = captureRequest();
                assertEquals("POST", req.method());
                assertTrue(req.uri().toString().contains("/album/" + MockData.BUCKET_TOKEN));
            }

            @Test
            void shouldCreateEmptyAlbum() throws Exception {
                doReturn(MockResponses.json(200, MockData.ALBUM_EMPTY)).when(mockClient).send(any(), any());

                final WaifuAlbum result = api.createAlbum("album2", MockData.BUCKET_TOKEN);

                assertEquals("album2", result.name());
                assertTrue(result.files().isEmpty());
            }
        }

        @Nested
        class Associations {

            @Test
            void shouldAssociateFiles() throws Exception {
                doReturn(MockResponses.json(200, MockData.ALBUM_WITH_FILES)).when(mockClient).send(any(), any());

                api.associateFiles(MockData.ALBUM_TOKEN, MockData.TOKEN);

                final HttpRequest req = captureRequest();
                assertEquals("POST", req.method());
                assertTrue(req.uri().toString().contains("/associate"));
            }

            @Test
            void shouldDisassociateFiles() throws Exception {
                doReturn(MockResponses.json(200, MockData.ALBUM_EMPTY)).when(mockClient).send(any(), any());

                api.disassociateFiles(MockData.ALBUM_TOKEN, MockData.TOKEN);

                final HttpRequest req = captureRequest();
                assertEquals("POST", req.method());
                assertTrue(req.uri().toString().contains("/disassociate"));
            }
        }

        @Nested
        class GetAlbum {

            @Test
            void shouldGetAlbum() throws Exception {
                doReturn(MockResponses.json(200, MockData.ALBUM_EMPTY)).when(mockClient).send(any(), any());

                final WaifuAlbum result = api.getAlbum(MockData.ALBUM_TOKEN);

                assertEquals("album2", result.name());
                assertEquals("GET", captureRequest().method());
            }
        }

        @Nested
        class DeleteAlbum {

            @Test
            void shouldDeleteAlbumKeepingFiles() throws Exception {
                doReturn(MockResponses.json(200, MockData.SUCCESS_DELETED)).when(mockClient).send(any(), any());

                final GenericSuccess result = api.deleteAlbum(MockData.ALBUM_TOKEN, false);

                assertTrue(result.success());
                assertTrue(captureRequest().uri().toString().contains("deleteFiles=false"));
            }

            @Test
            void shouldDeleteAlbumWithFiles() throws Exception {
                doReturn(MockResponses.json(200, MockData.SUCCESS_DELETED)).when(mockClient).send(any(), any());

                api.deleteAlbum(MockData.ALBUM_TOKEN, true);

                assertTrue(captureRequest().uri().toString().contains("deleteFiles=true"));
            }
        }

        @Nested
        class SharingAlbums {

            @Test
            void shouldShareAlbum() throws Exception {
                doReturn(MockResponses.json(200, MockData.SUCCESS_SHARED)).when(mockClient).send(any(), any());

                final String result = api.shareAlbum(MockData.ALBUM_TOKEN);

                assertEquals("sharedAlbum.foo", result);
                assertTrue(captureRequest().uri().toString().contains("/album/share/" + MockData.ALBUM_TOKEN));
            }

            @Test
            void shouldRevokeAlbum() throws Exception {
                doReturn(MockResponses.json(200, MockData.SUCCESS_SHARED)).when(mockClient).send(any(), any());

                final GenericSuccess result = api.revokeAlbum(MockData.ALBUM_TOKEN);

                assertTrue(result.success());
                assertTrue(captureRequest().uri().toString().contains("/album/revoke/" + MockData.ALBUM_TOKEN));
            }
        }

        @Nested
        class DownloadAlbum {

            @Test
            void shouldDownloadSelectiveFiles() throws Exception {
                final byte[] zipData = "zip content".getBytes();
                doReturn(MockResponses.bytes(200, zipData)).when(mockClient).send(any(), any());

                final byte[] result = api.downloadAlbum(MockData.ALBUM_TOKEN, 1);

                assertArrayEquals(zipData, result);
                final HttpRequest req = captureRequest();
                assertEquals("POST", req.method());
                assertTrue(req.uri().toString().contains("/album/download/" + MockData.ALBUM_TOKEN));
            }

            @Test
            void shouldDownloadWholeAlbum() throws Exception {
                final byte[] zipData = "zip content".getBytes();
                doReturn(MockResponses.bytes(200, zipData)).when(mockClient).send(any(), any());

                final byte[] result = api.downloadAlbum(MockData.ALBUM_TOKEN);

                assertArrayEquals(zipData, result);
                assertEquals("POST", captureRequest().method());
            }
        }
    }
}
