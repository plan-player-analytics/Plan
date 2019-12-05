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

import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.webserver.response.errors.*;
import com.djrapitops.plan.delivery.webserver.response.pages.*;
import com.djrapitops.plan.exceptions.ParseException;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.exceptions.connection.NotFoundException;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.ErrorPageLang;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.version.VersionCheckSystem;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.UUID;

/**
 * Factory for creating different {@link Response} objects.
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
            return new DebugPageResponse(pageFactory.debugPage(), versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse debug page");
        }
    }

    public Response playersPageResponse() {
        try {
            return new PlayersPageResponse(pageFactory.playersPage());
        } catch (ParseException e) {
            return internalErrorResponse(e, "Failed to parse players page");
        }
    }

    public ErrorResponse internalErrorResponse(Throwable e, String s) {
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

    public Response networkPageResponse() {
        try {
            return new PageResponse(pageFactory.networkPage());
        } catch (ParseException e) {
            return internalErrorResponse(e, "Failed to parse network page");
        }
    }

    public Response serverPageResponse(UUID serverUUID) throws NotFoundException {
        try {
            return new PageResponse(pageFactory.serverPage(serverUUID));
        } catch (ParseException e) {
            return internalErrorResponse(e, "Failed to parse server page");
        }
    }

    public RawDataResponse rawPlayerPageResponse(UUID uuid) {
        return new RawPlayerDataResponse(dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(uuid)));
    }

    public Response javaScriptResponse(String fileName) {
        try {
            return new JavaScriptResponse(fileName, files, locale);
        } catch (IOException e) {
            return notFound404("JS File not found from jar: " + fileName + ", " + e.toString());
        }
    }

    public Response cssResponse(String fileName) {
        try {
            return new CSSResponse(fileName, files);
        } catch (IOException e) {
            return notFound404("CSS File not found from jar: " + fileName + ", " + e.toString());
        }
    }

    public Response imageResponse(String fileName) {
        return new ByteResponse(ResponseType.IMAGE, FileResponse.format(fileName), files);
    }

    /**
     * Redirect somewhere
     *
     * @param location Starts with '/'
     * @return Redirection response.
     */
    public Response redirectResponse(String location) {
        return new RedirectResponse(location);
    }

    public Response faviconResponse() {
        return new ByteResponse(ResponseType.X_ICON, "web/favicon.ico", files);
    }

    public ErrorResponse pageNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UNKNOWN_PAGE_404));
    }

    public ErrorResponse uuidNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UUID_404));
    }

    public ErrorResponse playerNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.NOT_PLAYED_404));
    }

    public ErrorResponse notFound404(String message) {
        try {
            return new NotFoundResponse(message, versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse 404 page");
        }
    }

    public ErrorResponse basicAuthFail(WebUserAuthException e) {
        try {
            return PromptAuthorizationResponse.getBasicAuthResponse(e, versionCheckSystem, files);
        } catch (IOException jarReadFailed) {
            return internalErrorResponse(e, "Failed to parse PromptAuthorizationResponse");
        }
    }

    public ErrorResponse forbidden403() {
        return forbidden403("Your user is not authorized to view this page.<br>"
                + "If you believe this is an error contact staff to change your access level.");
    }

    public ErrorResponse forbidden403(String message) {
        try {
            return new ForbiddenResponse(message, versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse ForbiddenResponse");
        }
    }

    public ErrorResponse basicAuth() {
        try {
            return PromptAuthorizationResponse.getBasicAuthResponse(versionCheckSystem, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse PromptAuthorizationResponse");
        }
    }

    public BadRequestResponse badRequest(String errorMessage, String target) {
        return new BadRequestResponse(errorMessage + " (when requesting '" + target + "')");
    }

    public Response playerPageResponse(UUID playerUUID) {
        try {
            return new PageResponse(pageFactory.playerPage(playerUUID));
        } catch (IllegalStateException e) {
            return playerNotFound404();
        } catch (ParseException e) {
            return internalErrorResponse(e, "Failed to parse player page");
        }
    }
}