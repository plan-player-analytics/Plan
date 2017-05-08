package main.java.com.djrapitops.plan.api;

/**
 * This class contains Genders used by the plugin.
 *
 * @author Rsl1122
 * @since 2.0.0
 */
public enum Gender {
    MALE, FEMALE, OTHER, UNKNOWN;

    /**
     * Gets the Enum that corresponds to the name.
     *
     * @param name name of the gender enum.
     * @return Gender Enum
     */
    public static Gender parse(String name) {
        switch (name) {
            case "female":
                return Gender.FEMALE;
            case "male":
                return Gender.MALE;
            case "other":
                return Gender.OTHER;
            default:
                break;
        }
        return Gender.UNKNOWN;
    }
}
