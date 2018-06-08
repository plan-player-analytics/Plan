package com.djrapitops.plan.utilities.html.structure;

/**
 * Utility for creating Session accordion html and javascript from Session objects.
 *
 * @author Rsl1122
 * @see com.djrapitops.plan.data.container.Session for object
 */
public class SessionAccordion extends AbstractAccordion {

    private final boolean forPlayer;

    private final StringBuilder viewScript;

    private SessionAccordion(boolean forPlayer) {
        super("session_accordion");

        this.forPlayer = forPlayer;
        viewScript = new StringBuilder();

        addElements();
    }

    public static SessionAccordion forServer() {
        return new SessionAccordion(false);
    }

    public static SessionAccordion forPlayer() {
        return new SessionAccordion(true);
    }

    public String toViewScript() {
        return viewScript.toString();
    }

    private void addElements() {
        // Requires refactoring of Session object to contain information about player and server
    }

}