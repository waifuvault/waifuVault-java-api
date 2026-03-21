package moe.waifuvault.models;

import java.util.List;

/**
 * A bucket is a virtual collection of files linked to your IP and a token.
 *
 * @param token  the token of the bucket
 * @param files  the files contained in this bucket
 * @param albums the albums contained in this bucket
 */
public record WaifuBucket(
        String token,
        List<WaifuFile> files,
        List<AlbumStub> albums
) {
}
