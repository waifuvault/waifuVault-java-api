package moe.waifuvault.models;

import com.google.gson.annotations.SerializedName;

/**
 * Options associated with an uploaded file.
 *
 * @param hideFilename   whether the filename is hidden in the URL
 * @param oneTimeDownload whether the file will be deleted after it is first accessed
 * @param fileProtected  whether the file is password-protected/encrypted
 */
public record WaifuResponseOptions(
        boolean hideFilename,
        boolean oneTimeDownload,
        @SerializedName("protected") boolean fileProtected
) {

    /**
     * Check if this file is password-protected/encrypted.
     *
     * @return true if this file is protected
     */
    public boolean isProtected() {
        return fileProtected;
    }
}
