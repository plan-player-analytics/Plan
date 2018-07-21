package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.webserver.auth.FailReason;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.FormatUtils;
import com.djrapitops.plan.utilities.html.icon.Icon;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PromptAuthorizationResponse extends ErrorResponse {

    private static final String TIPS = "<br>- Ensure you have registered a user with <b>/plan register</b><br>"
            + "- Check that the username and password are correct<br>"
            + "- Username and password are case-sensitive<br>"
            + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.";

    private PromptAuthorizationResponse() {
        super.setTitle(Icon.called("lock").build() + " 401 Unauthorized");
    }

    public static PromptAuthorizationResponse getBasicAuthResponse() {
        PromptAuthorizationResponse response = new PromptAuthorizationResponse();
        response.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");

        response.setParagraph("Authentication Failed." + TIPS);
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

        response.setParagraph("Authentication Failed.</p><p><b>Reason: " + reason + "</b></p><p>" + TIPS);
        response.replacePlaceholders();
        return response;
    }
}
