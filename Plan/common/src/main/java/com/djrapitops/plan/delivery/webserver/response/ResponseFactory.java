/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.response;

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.rendering.html.icon.Family;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.rendering.pages.Page;
import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.delivery.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.delivery.webserver.response.errors.ForbiddenResponse;
import com.djrapitops.plan.delivery.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.delivery.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.delivery.webserver.response.pages.RawDataResponse;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.ErrorPageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.version.VersionCheckSystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Factory for creating different {@link Response_old} objects.
 *
 * @author Rsl1122
 */
@Singleton
public class ResponseFactory {

    private final VersionCheckSystem versionCheckSystem;
    private final PlanFiles files;
    private final PageFactory pageFactory;
    private final Locale locale;
    private final DBSystem dbSystem;

    @Inject
    public ResponseFactory(
            VersionCheckSystem versionCheckSystem,
            PlanFiles files,
            PageFactory pageFactory,
            Locale locale,
            DBSystem dbSystem
    ) {
        this.versionCheckSystem = versionCheckSystem;
        this.files = files;
        this.pageFactory = pageFactory;
        this.locale = locale;
        this.dbSystem = dbSystem;
    }

    public Response debugPageResponse() {
        try {
            return forPage(pageFactory.debugPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate debug page");
        }
    }

    private Response forPage(Page page) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(page.toHtml())
                .build();
    }

    private Response forInternalError(Throwable error, String cause) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(pageFactory.internalErrorPage(cause, error).toHtml())
                .setStatus(500)
                .build();
    }

    public Response playersPageResponse() {
        try {
            Optional<Response> error = checkDbClosedError();
            if (error.isPresent()) return error.get();
            return forPage(pageFactory.playersPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate players page");
        }
    }

    private Optional<Response> checkDbClosedError() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            try {
                return Optional.of(buildDBNotOpenResponse(dbState));
            } catch (IOException e) {
                return Optional.of(forInternalError(e, "Database was not open, additionally failed to generate error page for that"));
            }
        }
        return Optional.empty();
    }

    private Response buildDBNotOpenResponse(Database.State dbState) throws IOException {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(pageFactory.errorPage(
                        "503 Resources Unavailable",
                        "Database is " + dbState.name() + " - Please try again later. You can check database status with /plan info"
                ).toHtml())
                .setStatus(503)
                .build();
    }

    @Deprecated
    public ErrorResponse internalErrorResponse_old(Throwable e, String s) {
        try {
            return new InternalErrorResponse(s, e, versionCheckSystem, files);
        } catch (IOException improperRestartException) {
            return new ErrorResponse(
                    "Error occurred: " + e.toString() +
                            ", additional error occurred when attempting to send error page to user: " +
                            improperRestartException.toString()
            );
        }
    }

    public Response internalErrorResponse(Throwable e, String s) {
        return forInternalError(e, s);
    }

    public Response networkPageResponse() {
        Optional<Response> error = checkDbClosedError();
        if (error.isPresent()) return error.get();
        try {
            return forPage(pageFactory.networkPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate network page");
        }
    }

    public Response serverPageResponse(UUID serverUUID) {
        Optional<Response> error = checkDbClosedError();
        if (error.isPresent()) return error.get();
        try {
            return forPage(pageFactory.serverPage(serverUUID));
        } catch (NotFoundException e) {
            return notFound404(e.getMessage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate server page");
        }
    }

    public Response rawPlayerPageResponse(UUID playerUUID) {
        PlayerContainer player = dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(RawDataResponse.mapToNormalMap(player))
                .build();
    }

    @Deprecated
    public Response_old javaScriptResponse_old(String fileName) {
        try {
            return new JavaScriptResponse(fileName, files, locale);
        } catch (IOException e) {
            return notFound404_old("JS File not found from jar: " + fileName + ", " + e.toString());
        }
    }

    @Deprecated
    public Response_old cssResponse_old(String fileName) {
        try {
            return new CSSResponse(fileName, files);
        } catch (IOException e) {
            return notFound404_old("CSS File not found from jar: " + fileName + ", " + e.toString());
        }
    }

    @Deprecated
    public Response_old imageResponse_old(String fileName) {
        return new ByteResponse(ResponseType.IMAGE, FileResponse.format(fileName), files);
    }

    @Deprecated
    public Response_old fontResponse_old(String fileName) {
        ResponseType type = ResponseType.FONT_BYTESTREAM;
        if (fileName.endsWith(".woff")) {
            type = ResponseType.FONT_WOFF;
        } else if (fileName.endsWith(".woff2")) {
            type = ResponseType.FONT_WOFF2;
        } else if (fileName.endsWith(".eot")) {
            type = ResponseType.FONT_EOT;
        } else if (fileName.endsWith(".ttf")) {
            type = ResponseType.FONT_TTF;
        }
        return new ByteResponse(type, FileResponse.format(fileName), files);
    }

    public Response redirectResponse(String location) {
        return Response.builder().redirectTo(location).build();
    }

    public Response faviconResponse() {
        try {
            return Response.builder()
                    .setMimeType(MimeType.FAVICON)
                    .setContent(files.getCustomizableResourceOrDefault("web/favicon.ico").asBytes())
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Could not read favicon");
        }
    }

    @Deprecated
    public ErrorResponse pageNotFound404_old() {
        return notFound404_old(locale.getString(ErrorPageLang.UNKNOWN_PAGE_404));
    }

    public Response pageNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UNKNOWN_PAGE_404));
    }

    public Response uuidNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UUID_404));
    }

    public Response playerNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.NOT_PLAYED_404));
    }

    @Deprecated
    public ErrorResponse notFound404_old(String message) {
        try {
            return new NotFoundResponse(message, versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse_old(e, "Failed to generate 404 page");
        }
    }

    public Response notFound404(String message) {
        try {
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("map-signs").build(), "404 " + message, message).toHtml())
                    .setStatus(404)
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate 404 page with message '" + message + "'");
        }
    }

    public Response basicAuthFail(WebUserAuthException e) {
        try {
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
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("lock").build(), "401 Unauthorized", "Authentication Failed.</p><p><b>Reason: " + reason + "</b></p><p>").toHtml())
                    .setStatus(401)
                    .setHeader("WWW-Authenticate", "Basic realm=\"" + failReason.getReason() + "\"")
                    .build();
        } catch (IOException jarReadFailed) {
            return forInternalError(e, "Failed to generate PromptAuthorizationResponse");
        }
    }

    private List<String> getStackTrace(Throwable throwable) {
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

    @Deprecated
    public ErrorResponse forbidden403_old() {
        return forbidden403_old("Your user is not authorized to view this page.<br>"
                + "If you believe this is an error contact staff to change your access level.");
    }

    public Response forbidden403() {
        return forbidden403("Your user is not authorized to view this page.<br>"
                + "If you believe this is an error contact staff to change your access level.");
    }

    public Response forbidden403(String message) {
        try {
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("hand-paper").of(Family.REGULAR).build(), "403 Forbidden", message).toHtml())
                    .setStatus(403)
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate 403 page");
        }
    }

    @Deprecated
    public ErrorResponse forbidden403_old(String message) {
        try {
            return new ForbiddenResponse(message, versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse_old(e, "Failed to generate ForbiddenResponse");
        }
    }

    @Deprecated
    public ErrorResponse basicAuth_old() {
        try {
            return PromptAuthorizationResponse.getBasicAuthResponse(versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse_old(e, "Failed to generate PromptAuthorizationResponse");
        }
    }

    public Response basicAuth() {
        try {
            String tips = "<br>- Ensure you have registered a user with <b>/plan register</b><br>"
                    + "- Check that the username and password are correct<br>"
                    + "- Username and password are case-sensitive<br>"
                    + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.";
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("lock").build(), "401 Unauthorized", "Authentication Failed." + tips).toHtml())
                    .setStatus(401)
                    .setHeader("WWW-Authenticate", "Basic realm=\"Plan WebUser (/plan register)\"")
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate PromptAuthorizationResponse");
        }
    }

    public Response badRequest(String errorMessage, String target) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent("400 Bad Request: " + errorMessage + " (when requesting '" + target + "')")
                .setStatus(400)
                .build();
    }

    public Response playerPageResponse(UUID playerUUID) {
        try {
            return forPage(pageFactory.playerPage(playerUUID));
        } catch (IllegalStateException e) {
            return playerNotFound404();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate player page");
        }
    }
}