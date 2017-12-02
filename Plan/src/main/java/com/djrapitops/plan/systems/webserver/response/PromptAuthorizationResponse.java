package main.java.com.djrapitops.plan.systems.webserver.response;

import main.java.com.djrapitops.plan.utilities.html.Html;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PromptAuthorizationResponse extends ErrorResponse {

    public PromptAuthorizationResponse() {
        super.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");
        super.setTitle(Html.FONT_AWESOME_ICON.parse("lock")+" 401 Unauthorized");
        super.setParagraph("Authentication Failed.<br>"
                + "- Ensure you have registered a user with <b>/plan register</b><br>"
                + "- Check that the username and password are correct<br>"
                + "- Username and password are case-sensitive<br>"
                + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.");
        super.replacePlaceholders();
    }
}
