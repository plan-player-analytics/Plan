/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.info.connection;

import com.djrapitops.plan.api.exceptions.connection.NotFoundException;
import com.djrapitops.plan.api.exceptions.connection.WebException;
import com.djrapitops.plan.system.info.request.InfoRequest;
import com.djrapitops.plan.system.locale.lang.ErrorPageLang;
import com.djrapitops.plan.system.webserver.Request;
import com.djrapitops.plan.system.webserver.pages.PageHandler;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.system.webserver.response.errors.BadRequestResponse;
import com.djrapitops.plan.system.webserver.response.errors.NotFoundResponse;
import com.djrapitops.plugin.utilities.Verify;

import javax.inject.Inject;
import java.util.List;

/**
 * PageHandler for /info/requestclassname pages.
 * <p>
 * Used for answering info requests by other servers.
 * <p>
 * requestclassname should be replaced with lowercase version of {@code Class.getSimpleName()}
 *
 * @author Rsl1122
 */
public class InfoRequestPageHandler implements PageHandler {

    private final ConnectionSystem connectionSystem;

    @Inject
    public InfoRequestPageHandler(ConnectionSystem connectionSystem) {
        this.connectionSystem = connectionSystem;
    }

    @Override
    public Response getResponse(Request request, List<String> target) throws WebException {
        int responseCode = 200;

        try {
            if (target.isEmpty()) {
                return new NotFoundResponse(request.getLocale().getString(ErrorPageLang.UNKNOWN_PAGE_404));
            }

            if (!request.getRequestMethod().equals("POST")) {
                return new BadRequestResponse("POST should be used for Info calls.");
            }

            String requestName = target.get(0);
            InfoRequest infoRequest = connectionSystem.getInfoRequest(requestName);

            Verify.nullCheck(infoRequest, () -> new NotFoundException("Info Request has not been registered."));

            return new ConnectionIn(request, infoRequest).handleRequest();
        } catch (WebException e) {
            responseCode = getResponseCodeFor(e);
            throw e;
        } finally {
            ConnectionLog.logConnectionFrom(request.getRemoteAddress(), request.getTarget(), responseCode);
        }
    }

    private int getResponseCodeFor(WebException e) {
        return e.getResponseCode().getCode();
    }
}
