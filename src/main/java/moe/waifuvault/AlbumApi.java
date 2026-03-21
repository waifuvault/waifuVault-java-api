package moe.waifuvault;

import moe.waifuvault.models.GenericSuccess;
import moe.waifuvault.models.WaifuAlbum;

import java.util.Arrays;
import java.util.Map;

class AlbumApi extends BaseApi {

    public AlbumApi(final HttpService config) {
        super(config);
    }

    protected WaifuAlbum createAlbum(final String name, final String bucketToken) {
        return postJson("album/" + bucketToken,
                Map.of("name", name, "bucketToken", bucketToken),
                WaifuAlbum.class);
    }

    protected WaifuAlbum getAlbum(final String albumToken) {
        return get("album/" + albumToken, null, WaifuAlbum.class);
    }

    protected GenericSuccess deleteAlbum(final String albumToken, final boolean deleteFiles) {
        return delete("album/" + albumToken,
                Map.of("deleteFiles", String.valueOf(deleteFiles)),
                GenericSuccess.class);
    }

    protected WaifuAlbum associateFiles(final String albumToken, final String[] fileTokens) {
        return postJson("album/" + albumToken + "/associate",
                Map.of("fileTokens", Arrays.asList(fileTokens)),
                WaifuAlbum.class);
    }

    protected WaifuAlbum disassociateFiles(final String albumToken, final String[] fileTokens) {
        return postJson("album/" + albumToken + "/disassociate",
                Map.of("fileTokens", Arrays.asList(fileTokens)),
                WaifuAlbum.class);
    }

    protected String shareAlbum(final String albumToken) {
        return get("album/share/" + albumToken, null, GenericSuccess.class).description();
    }

    protected GenericSuccess revokeAlbum(final String albumToken) {
        return get("album/revoke/" + albumToken, null, GenericSuccess.class);
    }

    protected byte[] downloadAlbum(final String albumToken, final int[] fileIds) {
        final Object body = (fileIds != null && fileIds.length > 0) ? fileIds : new int[0];
        return postJsonBytes("album/download/" + albumToken, body);
    }
}
