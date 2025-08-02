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
package com.djrapitops.plan.delivery.webserver.resolver.json.theme;

import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.URIQuery;
import com.djrapitops.plan.delivery.webserver.ResponseFactory;
import com.google.gson.Gson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import utilities.TestResources;

import java.io.IOException;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * @author AuroraLS3
 */
@ExtendWith(MockitoExtension.class)
class SaveThemeJSONResolverTest {

    @Mock
    ResponseFactory responseFactory;
    @Spy
    Gson gson = new Gson();
    @InjectMocks
    SaveThemeJSONResolver saveThemeJSONResolver;

    @Mock
    Request request;

    @Test
    void defaultThemeIsDeemedValid() throws IOException {
        when(request.getQuery()).thenReturn(new URIQuery(Map.of("theme", "default")));
        when(request.getRequestBody()).thenReturn(TestResources.getJarResourceAsBytes("/assets/plan/themes/default.json"));
        lenient().when(responseFactory.badRequest(any(), any())).thenReturn(mock(Response.class));
        when(responseFactory.successResponse()).thenReturn(mock(Response.class));

        saveThemeJSONResolver.resolve(request);

        verify(responseFactory, times(0)).badRequest(any(), any());
        verify(responseFactory, times(1)).successResponse();
    }
}