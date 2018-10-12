package com.djrapitops.plan.system.webserver.response.pages;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.utilities.html.pages.PlayersPage;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class PlayersPageResponse extends PageResponse {

    public PlayersPageResponse(PlayersPage playersPage) throws ParseException {
        setHeader("HTTP/1.1 200 OK");
        setContent(playersPage.toHtml());
    }
}
