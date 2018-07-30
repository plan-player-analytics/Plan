package com.djrapitops.plan.system.locale.lang;

/**
 * {@link Lang} implementation for all error pages.
 *
 * @author Rsl1122
 */
public enum ErrorPageLang implements Lang {
    UUID_404("Player UUID was not found in the database."),
    NO_SERVERS_404("No Servers online to perform the request."),
    NOT_PLAYED_404("Player has not played on this server."),
    UNKNOWN_PAGE_404("Make sure you're accessing a link given by a command, Examples:</p><p>/player/PlayerName<br>/server/ServerName</p>"),
    UNAUTHORIZED_401("Unauthorized"),
    AUTHENTICATION_FAIlED_401("Authentication Failed."),
    AUTH_FAIL_TIPS_401("- Ensure you have registered a user with <b>/plan register</b><br>- Check that the username and password are correct<br>- Username and password are case-sensitive<br><br>If you have forgotten your password, ask a staff member to delete your old user and re-register."),
    FORBIDDEN_403("Forbidden"),
    ACCESS_DENIED_403("Access Denied"),
    NOT_FOUND_404("Not Found"),
    PAGE_NOT_FOUND_404("Page does not exist."),
    ANALYSIS_REFRESH("Analysis is being refreshed.."),
    ANALYSIS_REFRESH_LONG("Analysis is being run, refresh the page after a few seconds.."),

    ;

    private final String defaultValue;

    ErrorPageLang(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Override
    public String getIdentifier() {
        return "HTML ERRORS - " + name();
    }

    @Override
    public String getDefault() {
        return defaultValue;
    }
}
