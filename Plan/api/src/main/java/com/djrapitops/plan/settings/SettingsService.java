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
package com.djrapitops.plan.settings;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Service for defining plugin specific settings to the Plan config.
 * <p>
 * All given paths will be prepended with "Plugins." to place the settings in the plugins config section.
 * It is recommended to use setting paths {@code "<Plugin_name>.Some_setting"}
 * <p>
 * Requires Capability SETTINGS_API
 *
 * @author Rsl1122
 */
public interface SettingsService {

    static SettingsService getInstance() {
        return Optional.ofNullable(Holder.service)
                .orElseThrow(() -> new IllegalStateException("SettingsService has not been initialised yet."));
    }

    /**
     * Get a String from the config or the default value.
     *
     * @param path         Path in the config
     * @param defaultValue Supplier for the default value, {@code () -> "Example"}.
     * @return value in the config
     */
    String getString(String path, Supplier<String> defaultValue);

    /**
     * Get a Integer from the config or the default value.
     *
     * @param path         Path in the config
     * @param defaultValue Supplier for the default value, {@code () -> 500}.
     * @return value in the config
     */
    Integer getInteger(String path, Supplier<Integer> defaultValue);

    /**
     * Get a String list from the config or the default value.
     *
     * @param path         Path in the config
     * @param defaultValue Supplier for the default value, {@code () -> Arrays.asList("Example", "Another")}.
     * @return value in the config
     */
    List<String> getStringList(String path, Supplier<List<String>> defaultValue);

    class Holder {
        volatile static SettingsService service;

        private Holder() {
            /* Static variable holder */
        }

        static void set(SettingsService service) {
            Holder.service = service;
        }
    }

}
