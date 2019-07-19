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
package com.djrapitops.plan.system.json;

import com.djrapitops.plan.api.exceptions.WebUserAuthException;
import com.djrapitops.plan.extension.implementation.results.server.ExtensionServerData;
import com.djrapitops.plan.extension.implementation.storage.queries.ExtensionServerDataQuery;
import com.djrapitops.plan.system.Identifiers;
import com.djrapitops.plan.system.database.DBSystem;
import com.djrapitops.plan.system.webserver.RequestTarget;
import com.djrapitops.plan.system.webserver.auth.Authentication;
import com.djrapitops.plan.system.webserver.pages.json.ServerTabJSONHandler;
import com.djrapitops.plan.utilities.formatting.Formatters;
import com.djrapitops.plan.utilities.html.pages.AnalysisPluginTabs;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * Parses JSON for with different plugin data html in it.
 */
@Singleton
public class PluginTabJSONParser extends ServerTabJSONHandler<Object> {

    @Inject
    public PluginTabJSONParser(
            DBSystem dbSystem,
            Formatters formatters,
            Identifiers identifiers
    ) {
        super(identifiers, serverUUID -> {
            List<ExtensionServerData> extensionData = dbSystem.getDatabase()
                    .query(new ExtensionServerDataQuery(serverUUID));

            AnalysisPluginTabs pluginTabs = new AnalysisPluginTabs(extensionData, formatters);
            return new ExtensionTabs(pluginTabs.getNav(), pluginTabs.getTabs());
        });
    }

    @Override
    public boolean isAuthorized(Authentication auth, RequestTarget target) throws WebUserAuthException {
        return auth.getWebUser().getPermLevel() <= 0;
    }

    public static class ExtensionTabs {
        private final String navigation;
        private final String content;

        public ExtensionTabs(String navigation, String content) {
            this.navigation = navigation;
            this.content = content;
        }

        public String getNavigation() {
            return navigation;
        }

        public String getContent() {
            return content;
        }
    }
}
