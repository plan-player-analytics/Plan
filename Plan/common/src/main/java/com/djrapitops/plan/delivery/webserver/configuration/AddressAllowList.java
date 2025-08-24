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
package com.djrapitops.plan.delivery.webserver.configuration;

import com.djrapitops.plan.TaskSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import net.playeranalytics.plugin.scheduling.RunnableFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Maintains Dynamic IP and other IPs in IP Allowlist.
 * <p>
 * If the allowlist is disabled this task is not registered.
 *
 * @author AuroraLS3
 */
@Singleton
public class AddressAllowList extends TaskSystem.Task {

    private final PlanConfig config;

    private List<String> addresses = new ArrayList<>();

    @Inject
    public AddressAllowList(PlanConfig config) {
        this.config = config;
    }

    private static List<String> resolveIpAddress(String host) {
        try {
            InetAddress[] foundIps = InetAddress.getAllByName(host);
            return Arrays.stream(foundIps)
                    .map(InetAddress::getHostAddress)
                    .collect(Collectors.toList());
        } catch (UnknownHostException e) {
            return Collections.emptyList();
        }
    }

    @Override
    public void register(RunnableFactory runnableFactory) {
        if (config.isFalse(WebserverSettings.IP_WHITELIST)) {
            return;
        }
        runnableFactory.create(this).runTaskTimerAsynchronously(0, 60, TimeUnit.SECONDS);
    }

    @Override
    public void run() {
        updateDnsEntries();
    }

    private void updateDnsEntries() {
        List<String> configuredAddresses = config.get(WebserverSettings.WHITELIST);
        List<String> allowedAddresses = new ArrayList<>();

        for (String configuredAddress : configuredAddresses) {
            if (configuredAddress.startsWith("dns:")) {
                allowedAddresses.addAll(resolveIpAddress(configuredAddress.substring(4)));
            } else {
                allowedAddresses.add(configuredAddress);
            }
        }

        addresses = allowedAddresses;
    }

    public List<String> getAllowedAddresses() {
        return addresses;
    }
}
