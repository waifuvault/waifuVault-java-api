# waifuvault-java-api

This contains the official API bindings for uploading, deleting and obtaining files
with [waifuvault.moe](https://waifuvault.moe/). Contains a full up to date API for interacting with the service.

## Installation

### Maven

```xml

<dependency>
    <groupId>moe.waifuvault</groupId>
    <artifactId>waifuvault-java-api</artifactId>
    <version>1.0.4</version>
</dependency>
```

### Gradle

```groovy
implementation 'moe.waifuvault:waifuvault-java-api:1.0.4'
```

## Usage

This API contains 16 interactions:

1. [Upload File](#upload-file)
2. [Get File Info](#get-file-info)
3. [Delete File](#delete-file)
4. [Get File](#get-file)
5. [Modify Entry](#modify-entry)
6. [Create Bucket](#create-bucket)
7. [Get Bucket](#get-bucket)
8. [Delete Bucket](#delete-bucket)
9. [Create Album](#create-album)
10. [Delete Album](#delete-album)
11. [Get Album](#get-album)
12. [Associate Files](#associate-files)
13. [Disassociate Files](#disassociate-files)
14. [Share Album](#share-album)
15. [Revoke Album](#revoke-album)
16. [Download Album](#download-album)

Create an instance of `Waifuvault` to get started. It implements `AutoCloseable`, so you can use
try-with-resources to ensure the underlying HTTP client is properly released:

```java
import moe.waifuvault.Waifuvault;

try(final Waifuvault api = new Waifuvault()){
        // use the api
        }
```

You can also pass a custom `HttpClient` or base URL:

```java
final Waifuvault api = new Waifuvault("https://waifuvault.moe");
final Waifuvault api = new Waifuvault(myHttpClient);
final Waifuvault api = new Waifuvault("https://waifuvault.moe", myHttpClient);
```

### Upload File<a id="upload-file"></a>

To upload a file, use the `uploadFile` method. Create an upload using one of the `FileUpload` factory methods:

| Factory Method                               | Description                                 |
|----------------------------------------------|---------------------------------------------|
| `FileUpload.fromFile(String path)`           | Upload a file from a path on disk           |
| `FileUpload.fromFile(File file)`             | Upload a file from a `File` object          |
| `FileUpload.fromBytes(byte[], String)`       | Upload from a byte array with a filename    |
| `FileUpload.fromStream(InputStream, String)` | Upload from an input stream with a filename |
| `FileUpload.fromUrl(String url)`             | Upload a file from a URL on the internet    |

Optional settings can be chained:

| Method                      | Type      | Description                                                       |
|-----------------------------|-----------|-------------------------------------------------------------------|
| `.expires(String)`          | `String`  | Expiration time: number + unit (`m`=minutes, `h`=hours, `d`=days) |
| `.hideFilename(boolean)`    | `boolean` | If true, the filename won't appear in the URL                     |
| `.password(String)`         | `String`  | Encrypt the file with a password                                  |
| `.oneTimeDownload(boolean)` | `boolean` | If true, the file is deleted after first access                   |
| `.bucketToken(String)`      | `String`  | Associate the file with a bucket                                  |
| `.clientIP(String)`         | `String`  | Forward a custom client IP                                        |

Using a URL:

```java
final WaifuFile resp = api.uploadFile(
        FileUpload.fromUrl("https://waifuvault.moe/assets/custom/images/08.png"));
System.out.

println(resp.url());
```

Using a byte array:

```java
final WaifuFile resp = api.uploadFile(
        FileUpload.fromBytes("someData".getBytes(), "aCoolFile.jpg"));
System.out.

println(resp.url());
```

Using a file path:

```java
final WaifuFile resp = api.uploadFile(
        FileUpload.fromFile("./files/aCoolFile.jpg"));
System.out.

println(resp.url());
```

### Get File Info<a id="get-file-info"></a>

If you have a token from your upload, you can get file info. This returns the following info:

* token
* url
* options (protected, hideFilename, oneTimeDownload)
* retentionPeriod

| Parameter   | Type      | Description                                                   | Required |
|-------------|-----------|---------------------------------------------------------------|----------|
| `token`     | `String`  | The token of the upload                                       | true     |
| `formatted` | `boolean` | If true, `retentionPeriod` is human-readable; otherwise epoch | false    |

```java
final WaifuFile info = api.fileInfo("someToken");
System.out.

println(info.retentionPeriodString()); // epoch as string
        System.out.

println(info.url());
```

Human-readable timestamp:

```java
final WaifuFile info = api.fileInfo("someToken", true);
System.out.

println(info.retentionPeriodString()); // "328 days 18 hours 51 minutes 31 seconds"
```

### Delete File<a id="delete-file"></a>

To delete a file, supply the token to `deleteFile`.

| Parameter | Type     | Description                              | Required |
|-----------|----------|------------------------------------------|----------|
| `token`   | `String` | The token of the file you wish to delete | true     |

> **NOTE:** `deleteFile` will only ever either return a success or throw a `WaifuException`

```java
final GenericSuccess result = api.deleteFile("someToken");
System.out.

println(result.success()); // true
```

### Get File<a id="get-file"></a>

Download a file as a byte array by supplying either the token or the unique filename identifier.

By token:

```java
final byte[] file = api.getFile("someToken");
```

By token with password:

```java
final byte[] file = api.getFile("someToken", "myPassword");
```

By unique filename identifier (epoch/filename):

```java
final byte[] file = api.getFileByFilename("1710111505084/08.png");
```

> **Important:** The unique identifier is `epoch/filename` for standard uploads, or just `epoch.ext` for hidden filename
> uploads.

### Modify Entry<a id="modify-entry"></a>

Modify aspects of your entry such as password, expiry, or filename visibility.

| Parameter | Type     | Description                              | Required |
|-----------|----------|------------------------------------------|----------|
| `token`   | `String` | The token of the file you want to modify | true     |

Options on `ModifyEntryPayload`:

| Method                      | Description                                         |
|-----------------------------|-----------------------------------------------------|
| `.password(String)`         | New password to encrypt, or empty string to decrypt |
| `.previousPassword(String)` | Required when changing an existing password         |
| `.customExpiry(String)`     | New expiry (same format as `expires` in upload)     |
| `.hideFilename(boolean)`    | Hide the filename in the URL                        |

Set a password on a non-encrypted file:

```java
final WaifuFile result = api.modifyEntry("token",
        new ModifyEntryPayload().password("apple"));
System.out.

println(result.options().

isProtected()); // true
```

Change a password:

```java
api.modifyEntry("token",
                        new ModifyEntryPayload().

password("newPass").

previousPassword("apple"));
```

Change expiry:

```java
api.modifyEntry("token",
                        new ModifyEntryPayload().

customExpiry("1d"));
```

Decrypt a file:

```java
final WaifuFile result = api.modifyEntry("token",
        new ModifyEntryPayload().password("").previousPassword("apple"));
System.out.

println(result.options().

isProtected()); // false
```

### Create Bucket<a id="create-bucket"></a>

Buckets are virtual collections linked to your IP and a token.

```java
final WaifuBucket bucket = api.createBucket();
System.out.

println(bucket.token());
```

### Get Bucket<a id="get-bucket"></a>

Get a bucket and all its files.

| Parameter | Type     | Description             | Required |
|-----------|----------|-------------------------|----------|
| `token`   | `String` | The token of the bucket | true     |

```java
final WaifuBucket bucket = api.getBucket("someToken");
System.out.

println(bucket.token());
        System.out.

println(bucket.files()); // list of files
```

### Delete Bucket<a id="delete-bucket"></a>

Delete a bucket and all its files.

| Parameter | Type     | Description                       | Required |
|-----------|----------|-----------------------------------|----------|
| `token`   | `String` | The token of the bucket to delete | true     |

```java
final GenericSuccess result = api.deleteBucket("someToken");
System.out.

println(result.success()); // true
```

### Create Album<a id="create-album"></a>

Albums are shareable collections of files within a bucket.

| Parameter     | Type     | Description             | Required |
|---------------|----------|-------------------------|----------|
| `name`        | `String` | The name of the album   | true     |
| `bucketToken` | `String` | The token of the bucket | true     |

```java
final WaifuAlbum album = api.createAlbum("myAlbum", "someBucketToken");
System.out.

println(album.token());
        System.out.

println(album.name());
```

### Delete Album<a id="delete-album"></a>

Delete an album with an option to delete its files.

| Parameter     | Type      | Description                         | Required |
|---------------|-----------|-------------------------------------|----------|
| `albumToken`  | `String`  | The private album token             | true     |
| `deleteFiles` | `boolean` | Whether to permanently delete files | false    |

```java
api.deleteAlbum("albumToken");        // keep files
api.

deleteAlbum("albumToken",true);  // delete files
```

### Get Album<a id="get-album"></a>

Get an album and its files.

```java
final WaifuAlbum album = api.getAlbum("someToken");
System.out.

println(album.name());
        System.out.

println(album.files());
```

### Associate Files<a id="associate-files"></a>

Add files to an album. Files must be in the same bucket.

```java
final WaifuAlbum album = api.associateFiles("albumToken", "fileToken1", "fileToken2");
System.out.

println(album.files());
```

### Disassociate Files<a id="disassociate-files"></a>

Remove files from an album. Files remain in the bucket.

```java
final WaifuAlbum album = api.disassociateFiles("albumToken", "fileToken1");
System.out.

println(album.files());
```

### Share Album<a id="share-album"></a>

Share an album so its contents can be accessed from a public URL.

```java
final String publicUrl = api.shareAlbum("privateAlbumToken");
System.out.

println(publicUrl);
```

### Revoke Album<a id="revoke-album"></a>

Revoke public sharing of an album. The public URL is destroyed.

```java
final GenericSuccess result = api.revokeAlbum("privateAlbumToken");
System.out.

println(result.success()); // true
```

> **NOTE:** Once revoked, the URL is destroyed. If shared again later, a new URL will be issued.

### Download Album<a id="download-album"></a>

Download album contents as a ZIP file.

| Parameter    | Type     | Description                       | Required |
|--------------|----------|-----------------------------------|----------|
| `albumToken` | `String` | The private or public album token | true     |
| `fileIds`    | `int...` | Specific file IDs to download     | false    |

Download all files:

```java
final byte[] zip = api.downloadAlbum("someAlbumToken");
```

Selective files:

```java
final byte[] zip = api.downloadAlbum("someAlbumToken", 1, 3);
```

Get a file ID from token:

```java
final WaifuFile fileInfo = api.fileInfo("someToken");
final byte[] zip = api.downloadAlbum(fileInfo.album().token(), fileInfo.id());
```
