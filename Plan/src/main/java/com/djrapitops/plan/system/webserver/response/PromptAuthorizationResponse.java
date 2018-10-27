package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.update.VersionCheckSystem;
import com.djrapitops.plan.system.webserver.auth.FailReason;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.html.icon.Icon;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PromptAuthorizationResponse extends ErrorResponse {

    private static final String TIPS = "<br>- Ensure you have registered a user with <b>/plan register</b><br>"
            + "- Check that the username and password are correct<br>"
            + "- Username and password are case-sensitive<br>"
            + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.";

    private PromptAuthorizationResponse(VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        super(versionCheckSystem, files);
        super.setTitle(Icon.called("lock").build() + " 401 Unauthorized");
    }

    public static PromptAuthorizationResponse getBasicAuthResponse(VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        PromptAuthorizationResponse response = new PromptAuthorizationResponse(versionCheckSystem, files);
        response.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");

        response.setParagraph("Authentication Failed." + TIPS);
        response.replacePlaceholders();
        return response;
    }

    public static PromptAuthorizationResponse getBasicAuthResponse(WebUserAuthException e, VersionCheckSystem versionCheckSystem, PlanFiles files) throws IOException {
        PromptAuthorizationResponse response = new PromptAuthorizationResponse(versionCheckSystem, files);
        response.setHeader("HTTP/1.1 401 Access Denied\r\n"
                + "WWW-Authenticate: Basic realm=\"/\";");

        FailReason failReason = e.getFailReason();
        String reason = failReason.getReason();

        if (failReason == FailReason.ERROR) {
            StringBuilder errorBuilder = new StringBuilder("</p><pre>");
            for (String line : getStackTrace(e.getCause())) {
                errorBuilder.append(line);
            }
            errorBuilder.append("</pre>");

            reason += errorBuilder.toString();
        }

        response.setParagraph("Authentication Failed.</p><p><b>Reason: " + reason + "</b></p><p>" + TIPS);
        response.replacePlaceholders();
        return response;
    }

    /**
     * Gets lines for stack trace recursively.
     *
     * @param throwable Throwable element
     * @return lines of stack trace.
     */
    private static List<String> getStackTrace(Throwable throwable) {
        List<String> stackTrace = new ArrayList<>();
        stackTrace.add(throwable.toString());
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.add("    " + element.toString());
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            List<String> causeTrace = getStackTrace(cause);
            if (!causeTrace.isEmpty()) {
                causeTrace.set(0, "Caused by: " + causeTrace.get(0));
                stackTrace.addAll(causeTrace);
            }
        }

        return stackTrace;
    }
}
