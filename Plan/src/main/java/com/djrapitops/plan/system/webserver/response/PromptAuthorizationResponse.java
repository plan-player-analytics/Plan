package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.webserver.auth.FailReason;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.Html;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PromptAuthorizationResponse extends ErrorResponse {

    private PromptAuthorizationResponse() {
        super.setTitle(Html.FONT_AWESOME_ICON.parse("lock") + " 401 Unauthorized");
    }

    public static PromptAuthorizationResponse getBasicAuthResponse() {
        PromptAuthorizationResponse response = new PromptAuthorizationResponse();
        response.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");
        response.setParagraph("Authentication Failed.<br>"
                + "- Ensure you have registered a user with <b>/plan register</b><br>"
                + "- Check that the username and password are correct<br>"
                + "- Username and password are case-sensitive<br>"
                + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.");
        response.replacePlaceholders();
        return response;
    }

    public static PromptAuthorizationResponse getBasicAuthResponse(WebUserAuthException e) {
        PromptAuthorizationResponse response = new PromptAuthorizationResponse();
        response.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");

        FailReason failReason = e.getFailReason();
        String reason = failReason.getReason();

        if (failReason == FailReason.ERROR) {
            StringBuilder errorBuilder = new StringBuilder("</p><pre>");
            for (String line : FormatUtils.getStackTrace(e.getCause())) {
                errorBuilder.append(line);
            }
            errorBuilder.append("</pre>");

            reason += errorBuilder.toString();
        }

        response.setParagraph("Authentication Failed.<br>Reason: " + reason);
        response.replacePlaceholders();
        return response;
    }
}
