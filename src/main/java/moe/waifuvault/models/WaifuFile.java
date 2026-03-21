package moe.waifuvault.models;

/**
 * The response from the API for files and uploads.
 *
 * @param token           the token for the uploaded file
 * @param url             the URL to the uploaded file
 * @param options         the options for this upload
 * @param retentionPeriod when the file will expire, either as an epoch number or a human-readable string
 *                        depending on whether {@code formatted} was set to {@code true} in the request
 * @param bucket          the bucket token this file belongs to, or null
 * @param id              the public ID of this file
 * @param views           how many times this file has been downloaded
 * @param album           the album this file belongs to, or null
 */
public record WaifuFile(
        String token,
        String url,
        WaifuResponseOptions options,
        Object retentionPeriod,
        String bucket,
        int id,
        int views,
        WaifuAlbum album
) {

    /**
     * Get the retention period as a string regardless of whether it was returned as a number or formatted string.
     *
     * @return the retention period as a string, or null if not set
     */
    public String retentionPeriodString() {
        if (retentionPeriod == null) {
            return null;
        }
        if (retentionPeriod instanceof Number n) {
            return String.valueOf(n.longValue());
        }
        return retentionPeriod.toString();
    }
}
