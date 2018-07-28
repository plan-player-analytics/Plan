package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for Manage command related subcommand language.
 *
 * @author Rsl1122
 */
public enum ManageLang implements Lang {

    HOTSWAP_REMINDER("Manage - Remind HotSwap", "§eRemember to swap to the new database (/plan m hotswap ${0}) & reload the plugin."),
    PROGRESS_START("Manage - Start", "> §2Processing data.."),
    PROGRESS_SUCCESS("Manage - Success", "> §aSuccess!"),
    PROGRESS_FAIL("Manage - Fail", "> §cSomething went wrong: ${0}"),

    CONFIRMATION("Manage - Fail, Confirmation", "> §cAdd '-a' argument to confirm execution: ${0}"),

    CONFIRM_OVERWRITE("Manage - Confirm Overwrite", "Data in ${0} will be overwritten!"),
    CONFIRM_REMOVAL("Manage - Confirm Removal", "Data in ${0} will be removed!"),

    FAIL_SAME_DB("Manage - Fail Same Database", "> §cCan not operate on to and from the same database!"),
    FAIL_INCORRECT_DB("Manage - Fail Incorrect Database", "> §c'${0}' is not a supported database."),
    FAIL_FILE_NOT_FOUND("Manage - Fail File not found", "> §cNo File found at ${0}");

    private final String identifier;
    private final String defaultValue;

    ManageLang(String identifier, String defaultValue) {
        this.identifier = identifier;
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return identifier;
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}