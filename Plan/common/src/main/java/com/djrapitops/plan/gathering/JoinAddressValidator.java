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
package com.djrapitops.plan.gathering;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.DataGatheringSettings;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Lists;
import org.apache.commons.lang3.StringUtils;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Utility for validating and sanitizing join addresses.
 *
 * @author AuroraLS3
 */
@Singleton
public class JoinAddressValidator {

    private final PlanConfig config;
    private List<Pattern> filteredAddresses;

    @Inject
    public JoinAddressValidator(PlanConfig config) {
        /* Dagger injection constructor */
        this.config = config;
    }

    private void prepareFilteredAddresses() {
        if (filteredAddresses != null) return;

        List<String> unCompiledFilteredAddresses = config.get(DataGatheringSettings.FILTER_JOIN_ADDRESSES);
        if (unCompiledFilteredAddresses != null) {
            filteredAddresses = Lists.map(unCompiledFilteredAddresses, Pattern::compile);
        } else {
            filteredAddresses = new ArrayList<>();
        }
    }

    @Untrusted
    public String sanitize(@Untrusted String address) {
        if (address == null || config.isFalse(DataGatheringSettings.JOIN_ADDRESSES)) return "";
        if (!address.isEmpty()) {
            // Remove port
            if (address.contains(":")) {
                address = address.substring(0, address.lastIndexOf(':'));
            }
            // Remove data added by Bungeecord/Velocity
            if (address.contains("\u0000")) {
                address = address.substring(0, address.indexOf('\u0000'));
            }
            // Remove data added by Forge Mod Loader
            if (address.contains("fml")) {
                address = address.substring(0, address.lastIndexOf("fml"));
            }
            if (config.isFalse(DataGatheringSettings.PRESERVE_JOIN_ADDRESS_CASE)) {
                address = StringUtils.lowerCase(address);
            }
            prepareFilteredAddresses();
            @Untrusted final String finalAddress = address;
            if (filteredAddresses.stream().anyMatch(pattern -> pattern.matcher(finalAddress).matches())) {
                address = "";
            }
        }
        return address;
    }

    public boolean isValid(@Untrusted String address) {
        if (address.isEmpty()) return false;
        if (config.isTrue(DataGatheringSettings.PRESERVE_INVALID_JOIN_ADDRESS)) return true;
        try {
            URI uri = new URI(address);
            String path = uri.getPath();
            return path != null && path.indexOf('.') != -1;
        } catch (URISyntaxException uriSyntaxException) {
            return false;
        }
    }

}
