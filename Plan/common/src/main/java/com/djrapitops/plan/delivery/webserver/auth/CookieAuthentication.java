package com.djrapitops.plan.delivery.webserver.auth;

import com.djrapitops.plan.delivery.domain.WebUser;
import com.djrapitops.plan.exceptions.WebUserAuthException;

public class CookieAuthentication implements Authentication {

    private final String cookie;

    public CookieAuthentication(String cookie) {
        this.cookie = cookie;
    }

    @Override
    public WebUser getWebUser() {
        return ActiveCookieStore.checkCookie(cookie)
                .orElseThrow(() -> new WebUserAuthException(FailReason.NO_USER_PRESENT));
    }
}
