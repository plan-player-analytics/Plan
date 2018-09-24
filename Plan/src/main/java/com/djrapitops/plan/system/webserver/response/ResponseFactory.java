package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.ErrorPageLang;
import com.djrapitops.plan.system.webserver.response.errors.*;
import com.djrapitops.plan.system.webserver.response.pages.*;
import com.djrapitops.plan.utilities.html.pages.PageFactory;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

import javax.inject.Inject;
import javax.inject.Named;
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

    private final String version;
    private final PlanFiles files;
    private final PageFactory pageFactory;
    private final Locale locale;
    private final Database database;
    private final ErrorHandler errorHandler;

    @Inject
    public ResponseFactory(
            @Named("currentVersion") String version,
            PlanFiles files,
            PageFactory pageFactory,
            Locale locale,
            Database database,
            ErrorHandler errorHandler
    ) {
        this.version = version;
        this.files = files;
        this.pageFactory = pageFactory;
        this.locale = locale;
        this.database = database;
        this.errorHandler = errorHandler;
    }

    public Response debugPageResponse() {
        try {
            return new DebugPageResponse(pageFactory.debugPage(), version, files);
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
            errorHandler.log(L.WARN, this.getClass(), e);
            return new InternalErrorResponse(s, e, version, files);
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
            return new NetworkPageResponse(pageFactory.networkPage());
        } catch (ParseException e) {
            return internalErrorResponse(e, "Failed to parse network page");
        }
    }

    public RawDataResponse rawPlayerPageResponse(UUID uuid) {
        return new RawPlayerDataResponse(database.fetch().getPlayerContainer(uuid));
    }

    public RawDataResponse rawServerPageResponse(UUID serverUUID) {
        return new RawServerDataResponse(database.fetch().getServerContainer(serverUUID));
    }

    public Response javaScriptResponse(String fileName) {
        try {
            return new JavaScriptResponse(fileName);
        } catch (IOException e) {
            return notFound404("JS File not found from jar: " + fileName + ", " + e.toString());
        }
    }

    public Response cssResponse(String fileName) {
        try {
            return new CSSResponse(fileName);
        } catch (IOException e) {
            return notFound404("CSS File not found from jar: " + fileName + ", " + e.toString());
        }
    }

    public Response redirectResponse(String location) {
        return new RedirectResponse(location);
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

    public ErrorResponse serverNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.NO_SERVERS_404));
    }

    public ErrorResponse notFound404(String message) {
        try {
            return new NotFoundResponse(message, version, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse 404 page");
        }
    }

    public ErrorResponse basicAuthFail(WebUserAuthException e) {
        try {
            return PromptAuthorizationResponse.getBasicAuthResponse(e, version, files);
        } catch (IOException e1) {
            return internalErrorResponse(e, "Failed to parse PromptAuthorizationResponse");
        }
    }

    public ErrorResponse forbidden403() {
        return forbidden403("Your user is not authorized to view this page.<br>"
                + "If you believe this is an error contact staff to change your access level.");
    }

    public ErrorResponse forbidden403(String message) {
        try {
            return new ForbiddenResponse(message, version, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse ForbiddenResponse");
        }
    }

    public ErrorResponse unauthorizedServer(String message) {
        try {
            return new UnauthorizedServerResponse(message, version, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse UnauthorizedServerResponse");
        }
    }

    public ErrorResponse gatewayError504(String message) {
        try {
            return new GatewayErrorResponse(message, version, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse GatewayErrorResponse");
        }
    }

    public ErrorResponse basicAuth() {
        try {
            return PromptAuthorizationResponse.getBasicAuthResponse(version, files);
        } catch (IOException e) {
            return internalErrorResponse(e, "Failed to parse PromptAuthorizationResponse");
        }
    }
}