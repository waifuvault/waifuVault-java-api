package moe.waifuvault.models;

/**
 * A lightweight album reference without its file list.
 * Returned as part of bucket responses.
 *
 * @param token       the private token of this album
 * @param bucket      the token of the bucket this album belongs to
 * @param publicToken the public token used to share the album, or null if not shared
 * @param name        the name of the album
 * @param dateCreated the unix timestamp of when this album was created
 */
public record AlbumStub(
        String token,
        String bucket,
        String publicToken,
        String name,
        long dateCreated
) {
}
