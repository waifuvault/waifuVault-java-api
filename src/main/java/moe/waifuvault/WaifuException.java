package moe.waifuvault;

/**
 * Exception thrown when the WaifuVault API returns an error response.
 */
public class WaifuException extends RuntimeException {

    /** The HTTP status code. */
    private final int status;

    /** The HTTP status name. */
    private final String name;

    /**
     * Create a new WaifuException.
     *
     * @param status the HTTP status code
     * @param name   the name of the HTTP status
     * @param message the error message or reason
     */
    public WaifuException(final int status, final String name, final String message) {
        super("Error " + status + " (" + name + "): " + message);
        this.status = status;
        this.name = name;
    }

    /**
     * Get the HTTP status code of the error.
     *
     * @return the HTTP status code
     */
    public int getStatus() {
        return status;
    }

    /**
     * Get the name of the HTTP status.
     *
     * @return the HTTP status name
     */
    public String getName() {
        return name;
    }
}
