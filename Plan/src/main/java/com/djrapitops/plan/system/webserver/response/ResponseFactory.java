package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.system.database.databases.Database;
import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.locale.lang.ErrorPageLang;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.InternalErrorResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plan.system.webserver.response.pages.*;
import com.djrapitops.plan.utilities.html.pages.PageFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

/**
 * Factory for creating different {@link Response} objects.
 *
 * @author Rsl1122
 */
@Singleton
public class ResponseFactory {

    private final PageFactory pageFactory;
    private final Locale locale;
    private final Database database;

    @Inject
    public ResponseFactory(
            PageFactory pageFactory,
            Locale locale,
            Database database
    ) {
        this.pageFactory = pageFactory;
        this.locale = locale;
        this.database = database;
    }

    public Response debugPageResponse() {
        return new DebugPageResponse(pageFactory.debugPage());
    }

    public Response playersPageResponse() {
        try {
            return new PlayersPageResponse(pageFactory.playersPage());
        } catch (ParseException e) {
            return new InternalErrorResponse("Failed to parse players page", e);
        }
    }

    public Response networkPageResponse() {
        try {
            return new NetworkPageResponse(pageFactory.networkPage());
        } catch (ParseException e) {
            return new InternalErrorResponse("Failed to parse network page", e);
        }
    }

    public RawDataResponse rawPlayerPageResponse(UUID uuid) {
        return new RawPlayerDataResponse(database.fetch().getPlayerContainer(uuid));
    }

    public RawDataResponse rawServerPageResponse(UUID serverUUID) {
        return new RawServerDataResponse(database.fetch().getServerContainer(serverUUID));
    }

    public Response javaScriptResponse(String fileName) {
        return new JavaScriptResponse(fileName);
    }

    public Response cssResponse(String fileName) {
        return new CSSResponse(fileName);
    }

    public Response redirectResponse(String location) {
        return new RedirectResponse(location);
    }

    public ErrorResponse pageNotFound404() {
        return new NotFoundResponse(locale.getString(ErrorPageLang.UNKNOWN_PAGE_404));
    }

    public ErrorResponse uuidNotFound404() {
        return new NotFoundResponse(locale.getString(ErrorPageLang.UUID_404));
    }

    public ErrorResponse playerNotFound404() {
        return new NotFoundResponse(locale.getString(ErrorPageLang.NOT_PLAYED_404));
    }

    public ErrorResponse serverNotFound404() {
        return new NotFoundResponse(locale.getString(ErrorPageLang.NO_SERVERS_404));
    }
}