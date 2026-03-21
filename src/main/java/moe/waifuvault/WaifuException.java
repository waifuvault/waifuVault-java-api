package moe.waifuvault;

/**
 * Exception thrown when the WaifuVault API returns an error response.
 */
public class WaifuException extends RuntimeException {

    private final int status;
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
     * @return the HTTP status code of the error
     */
    public int getStatus() {
        return status;
    }

    /**
     * @return the name of the HTTP status
     */
    public String getName() {
        return name;
    }
}
