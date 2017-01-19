package com.djrapitops.plan.api;

public enum Gender {
    MALE, FEMALE, OTHER, UNKNOWN;

    /**
     * Gets the Enum that corresponds to the name.
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
