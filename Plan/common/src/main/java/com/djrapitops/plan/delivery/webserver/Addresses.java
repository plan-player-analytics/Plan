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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.webserver.http.WebServer;
import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.properties.ServerProperties;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.ExportSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import dagger.Lazy;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Optional;

/**
 * In charge of address management.
 * <p>
 * Possible scenarios (problem domain):
 * - IP in server properties is 0.0.0.0 which is invalid
 * - IP in server properties is empty string, which is invalid
 * - Alternative IP is set in the Plan config
 * - Proxy server is used as main webserver, this address has highest priority
 * - Webserver is disabled, external webserver address in config should be used
 * - Webserver uses either http or https
 * - Webserver always has a port, but Alternative IP can be used to remove the port from the address
 * <p>
 * What are the addresses used in:
 * - Given as links to commands
 * - Redirection
 * - Storing proxy server address in database
 *
 * @author AuroraLS3
 */
@Singleton
public class Addresses {

    private final PlanConfig config;
    private final DBSystem dbSystem;
    private final Lazy<ServerProperties> serverProperties;
    private final Lazy<WebServer> webserver;

    @Inject
    public Addresses(
            PlanConfig config,
            DBSystem dbSystem,
            Lazy<ServerProperties> serverProperties,
            Lazy<WebServer> webserver
    ) {
        this.config = config;
        this.dbSystem = dbSystem;
        this.serverProperties = serverProperties;
        this.webserver = webserver;
    }

    public Optional<String> getMainAddress() {
        Optional<String> proxyServerAddress = getAnyValidServerAddress();
        return proxyServerAddress.isPresent() ? proxyServerAddress : getAccessAddress();
    }

    public Optional<String> getAccessAddress() {
        WebServer webServer = this.webserver.get();
        if (!webServer.isEnabled()) {
            if (config.isTrue(ExportSettings.SERVER_PAGE)) {
                return Optional.of(getFallbackExternalAddress());
            } else {
                return Optional.empty();
            }
        }
        return getIP().map(ip -> webServer.getProtocol() + "://" + ip);
    }

    private Optional<String> getIP() {
        int port = webserver.get().getPort();
        return config.isTrue(WebserverSettings.SHOW_ALTERNATIVE_IP)
                ? Optional.of(config.get(WebserverSettings.ALTERNATIVE_IP).replace("%port%", String.valueOf(port)))
                : getServerPropertyIP().map(ip -> ip + ":" + port);
    }

    private String getFallbackExternalAddress() {
        return config.get(WebserverSettings.EXTERNAL_LINK);
    }

    public String getFallbackLocalhostAddress() {
        WebServer webServer = this.webserver.get();
        return webServer.getProtocol() + "://localhost:" + webServer.getPort();
    }

    public Optional<String> getProxyServerAddress() {
        return dbSystem.getDatabase().query(ServerQueries.fetchProxyServers())
                .stream()
                .map(Server::getWebAddress)
                .filter(this::isValidAddress)
                .findAny();
    }

    public Optional<String> getAnyValidServerAddress() {
        return dbSystem.getDatabase().query(ServerQueries.fetchPlanServerInformationCollection())
                .stream()
                .map(Server::getWebAddress)
                .filter(this::isValidAddress)
                .findAny();
    }

    private boolean isValidAddress(String address) {
        return address != null
                && !address.isEmpty()
                && !"0.0.0.0".equals(address)
                && !"https://www.example.address".equals(address)
                && !"http://www.example.address".equals(address)
                && !"http://localhost:0".equals(address);
    }

    public Optional<String> getServerPropertyIP() {
        String ip = serverProperties.get().getIp();
        return isValidAddress(ip) ? Optional.of(ip) : Optional.empty();
    }

    public boolean isWebserverEnabled() {
        return webserver.get().isEnabled();
    }

    public String getBasePath(String address) {
        String basePath = address
                .replace("http://", "")
                .replace("https://", "");
        if (StringUtils.contains(basePath, '/')) {
            return basePath.substring(StringUtils.indexOf(basePath, '/'));
        } else {
            return "";
        }
    }
}
