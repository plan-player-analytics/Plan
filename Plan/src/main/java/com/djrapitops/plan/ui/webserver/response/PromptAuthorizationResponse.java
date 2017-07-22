package main.java.com.djrapitops.plan.ui.webserver.response;

import java.io.OutputStream;

/**
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class PromptAuthorizationResponse extends Response {

    public PromptAuthorizationResponse(OutputStream output) {
        super(output);
        super.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"Analysis\";");
        super.setContent("<h1>401 Unauthorized</h1><p>Authentication Failed.<br>"
                + "- Ensure you have registered a user with <b>/plan register</b><br>"
                + "- Check that the username and password are correct<br>"
                + "- Username and password are case-sensitive<br>"
                + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.</p>");
    }
}
