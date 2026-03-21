package moe.waifuvault.models;

import java.util.List;

/**
 * An album is a shareable collection of files. It can be shared with others in a read-only fashion.
 *
 * @param token       the private token of this album
 * @param bucketToken the token of the bucket this album belongs to
 * @param publicToken the public token used to share the album, or null if not shared
 * @param name        the name of the album
 * @param files       the files contained in this album
 * @param dateCreated the unix timestamp of when this album was created
 */
public record WaifuAlbum(
        String token,
        String bucketToken,
        String publicToken,
        String name,
        List<WaifuFile> files,
        long dateCreated
) {
}
