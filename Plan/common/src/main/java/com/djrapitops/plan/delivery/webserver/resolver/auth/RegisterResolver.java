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
package com.djrapitops.plan.delivery.webserver.resolver.auth;

import com.djrapitops.plan.delivery.web.resolver.NoAuthResolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.exception.BadRequestException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.webserver.auth.RegistrationBin;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.WebUserQueries;
import com.djrapitops.plan.utilities.PassEncryptUtil;
import com.djrapitops.plan.utilities.java.Maps;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Collections;
import java.util.Optional;

@Singleton
public class RegisterResolver implements NoAuthResolver {

    private final DBSystem dbSystem;

    @Inject
    public RegisterResolver(DBSystem dbSystem) {this.dbSystem = dbSystem;}

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse(request));
    }

    public Response getResponse(Request request) {
        URIQuery query = request.getQuery();
        Optional<String> checkCode = query.get("code");
        if (checkCode.isPresent()) {
            return Response.builder()
                    .setStatus(200)
                    .setJSONContent(Collections.singletonMap("success", !RegistrationBin.contains(checkCode.get())))
                    .build();
        }

        URIQuery form = request.getFormBody();
        String username = form.get("user").orElseThrow(() -> new BadRequestException("'user' parameter not defined"));

        boolean alreadyExists = dbSystem.getDatabase().query(WebUserQueries.fetchUser(username)).isPresent();
        if (alreadyExists) throw new BadRequestException("User already exists!");

        String password = form.get("password").orElseThrow(() -> new BadRequestException("'password' parameter not defined"));
        try {
            String code = RegistrationBin.addInfoForRegistration(username, password);
            return Response.builder()
                    .setStatus(200)
                    .setJSONContent(Maps.builder(String.class, Object.class)
                            .put("success", true)
                            .put("code", code)
                            .build())
                    .build();
        } catch (PassEncryptUtil.CannotPerformOperationException e) {
            throw new IllegalStateException(e);
        }
    }

}
