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
import com.djrapitops.plan.delivery.rendering.pages.Page;
import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.webserver.response.errors.*;
import com.djrapitops.plan.delivery.webserver.response.pages.PageResponse;
import com.djrapitops.plan.delivery.webserver.response.pages.RawDataResponse;
import com.djrapitops.plan.delivery.webserver.response.pages.RawPlayerDataResponse;
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
            return forInternalError("Failed to generate debug page", e);
        }
    }

    private Response forPage(Page page) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(page.toHtml())
                .build();
    }

    private Response forInternalError(String cause, Throwable error) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(pageFactory.internalErrorPage(cause, error).toHtml())
                .setStatus(500)
                .build();
    }

    public Response playersPageResponse() {
        try {
            Optional<Response> error = checkIfDBIsOpen();
            if (error.isPresent()) return error.get();
            return forPage(pageFactory.playersPage());
        } catch (IOException e) {
            return forInternalError("Failed to generate players page", e);
        }
    }

    private Optional<Response> checkIfDBIsOpen() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            try {
                return Optional.of(buildDBNotOpenResponse(dbState));
            } catch (IOException e) {
                return Optional.of(forInternalError("Database was not open, additionally failed to generate error page for that", e));
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

    @Deprecated
    public Response_old networkPageResponse_old() {
        try {
            return new PageResponse(pageFactory.networkPage());
        } catch (IOException e) {
            return internalErrorResponse_old(e, "Failed to generate network page");
        }
    }

    @Deprecated
    public Response_old serverPageResponse_old(UUID serverUUID) throws NotFoundException {
        try {
            return new PageResponse(pageFactory.serverPage(serverUUID));
        } catch (IOException e) {
            return internalErrorResponse_old(e, "Failed to generate server page");
        }
    }

    @Deprecated
    public RawDataResponse rawPlayerPageResponse_old(UUID uuid) {
        return new RawPlayerDataResponse(dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(uuid)));
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

    /**
     * Redirect somewhere
     *
     * @param location Starts with '/'
     * @return Redirection response.
     */
    @Deprecated
    public Response_old redirectResponse_old(String location) {
        return new RedirectResponse(location);
    }

    @Deprecated
    public Response_old faviconResponse_old() {
        return new ByteResponse(ResponseType.X_ICON, "web/favicon.ico", files);
    }

    @Deprecated
    public ErrorResponse pageNotFound404_old() {
        return notFound404_old(locale.getString(ErrorPageLang.UNKNOWN_PAGE_404));
    }

    @Deprecated
    public ErrorResponse uuidNotFound404_old() {
        return notFound404_old(locale.getString(ErrorPageLang.UUID_404));
    }

    public Response uuidNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UUID_404));
    }

    @Deprecated
    public ErrorResponse playerNotFound404_old() {
        return notFound404_old(locale.getString(ErrorPageLang.NOT_PLAYED_404));
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
                    .setContent(pageFactory.errorPage("404 " + message, message).toHtml())
                    .setStatus(404)
                    .build();
        } catch (IOException e) {
            return forInternalError("Failed to generate 404 page with message '" + message + "'", e);
        }
    }

    @Deprecated
    public ErrorResponse basicAuthFail_old(WebUserAuthException e) {
        try {
            return PromptAuthorizationResponse.getBasicAuthResponse(e, versionCheckSystem, files);
        } catch (IOException jarReadFailed) {
            return internalErrorResponse_old(e, "Failed to generate PromptAuthorizationResponse");
        }
    }

    @Deprecated
    public ErrorResponse forbidden403_old() {
        return forbidden403_old("Your user is not authorized to view this page.<br>"
                + "If you believe this is an error contact staff to change your access level.");
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

    @Deprecated
    public BadRequestResponse badRequest_old(String errorMessage, String target) {
        return new BadRequestResponse(errorMessage + " (when requesting '" + target + "')");
    }

    public Response playerPageResponse(UUID playerUUID) {
        try {
            return forPage(pageFactory.playerPage(playerUUID));
        } catch (IllegalStateException e) {
            return playerNotFound404();
        } catch (IOException e) {
            return forInternalError("Failed to generate player page", e);
        }
    }

    @Deprecated
    public Response_old playerPageResponse_old(UUID playerUUID) {
        try {
            return new PageResponse(pageFactory.playerPage(playerUUID));
        } catch (IllegalStateException e) {
            return playerNotFound404_old();
        } catch (IOException e) {
            return internalErrorResponse_old(e, "Failed to generate player page");
        }
    }
}