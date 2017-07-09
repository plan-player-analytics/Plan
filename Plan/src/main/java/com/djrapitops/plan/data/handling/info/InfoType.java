package main.java.com.djrapitops.plan.data.handling.info;

/**
 * Enum class for the types of HandlingInfo to be processed.
 *
 * Type is only used for debugging.
 *
 * OTHER should be used when
 *
 * @author Rsl1122
 * @since 3.0.0
 */
public enum InfoType {

    /**
     *
     */
    CHAT,
    /**
     *
     */
    DEATH,
    /**
     *
     */
    KILL,
    /**
     *
     */
    GM,
    /**
     *
     */
    LOGIN,
    /**
     *
     */
    LOGOUT,
    /**
     *
     */
    KICK,
    /**
     *
     */
    RELOAD,
    /**
     * Used for events registered with the API.
     *
     * @since 3.1.1
     */
    OTHER;
}
