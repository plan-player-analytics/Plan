package com.djrapitops.plan.api;

public enum Gender {
    MALE, FEMALE, OTHER, UNKNOWN;

    public static Gender parse(String string) {
        switch (string) {
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
