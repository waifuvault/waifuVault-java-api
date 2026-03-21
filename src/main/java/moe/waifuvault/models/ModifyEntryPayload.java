package moe.waifuvault.models;

/**
 * Payload for modifying aspects of an uploaded file.
 * <p>
 * Use the builder-style methods to set the desired modifications, then pass
 * this object to {@link moe.waifuvault.Waifuvault#modifyEntry(String, ModifyEntryPayload)}.
 */
public class ModifyEntryPayload {

    /** Create a new empty ModifyEntryPayload. */
    public ModifyEntryPayload() {
    }

    private String password;
    private String previousPassword;
    private String customExpiry;
    private Boolean hideFilename;

    /**
     * Set a new password to encrypt the file, or change the existing password.
     * If the file is not currently encrypted, this will encrypt it.
     * If the file is already encrypted, {@link #previousPassword(String)} must also be set.
     *
     * @param password the new password, or empty string to decrypt
     * @return this instance for chaining
     */
    public ModifyEntryPayload password(final String password) {
        this.password = password;
        return this;
    }

    /**
     * Set the previous password when changing an existing password.
     * Required when the file is already encrypted and you want to change or remove the password.
     *
     * @param previousPassword the current password on the file
     * @return this instance for chaining
     */
    public ModifyEntryPayload previousPassword(final String previousPassword) {
        this.previousPassword = previousPassword;
        return this;
    }

    /**
     * Set a new custom expiry for the file.
     *
     * @param customExpiry a string containing a number and a unit ({@code m} for minutes, {@code h} for hours, {@code d} for days)
     * @return this instance for chaining
     */
    public ModifyEntryPayload customExpiry(final String customExpiry) {
        this.customExpiry = customExpiry;
        return this;
    }

    /**
     * Set whether to hide the filename in the URL.
     * Check the response URL for the new URL to use.
     *
     * @param hideFilename true to hide the filename
     * @return this instance for chaining
     */
    public ModifyEntryPayload hideFilename(final boolean hideFilename) {
        this.hideFilename = hideFilename;
        return this;
    }

    /**
     * Get the new password.
     *
     * @return the new password, or null if not set
     */
    public String getPassword() {
        return password;
    }

    /**
     * Get the previous password.
     *
     * @return the previous password, or null if not set
     */
    public String getPreviousPassword() {
        return previousPassword;
    }

    /**
     * Get the custom expiry.
     *
     * @return the custom expiry, or null if not set
     */
    public String getCustomExpiry() {
        return customExpiry;
    }

    /**
     * Get whether to hide the filename.
     *
     * @return whether to hide the filename, or null if not set
     */
    public Boolean getHideFilename() {
        return hideFilename;
    }
}
