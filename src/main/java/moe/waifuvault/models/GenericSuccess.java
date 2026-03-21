package moe.waifuvault.models;

/**
 * A generic success response from the API.
 *
 * @param success     whether the operation was successful
 * @param description a description of the result
 */
public record GenericSuccess(
        boolean success,
        String description
) {
}
