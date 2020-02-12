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
package com.djrapitops.plan.capability;

import java.util.Optional;
import java.util.function.Consumer;

/**
 * Service for figuring out provided API capabilities.
 * <p>
 * {@link CapabilityService#registerEnableListener(Consumer)} to be notified of Plan reloads
 * {@link CapabilityService#hasCapability(String)} to check if a capability is available.
 * <p>
 * See {@link Capability} for list of capabilities provided by the current version.
 *
 * @author Rsl1122
 */
public interface CapabilityService {

    /**
     * Obtain instance of CapabilityService.
     *
     * @return CapabilityService implementation.
     * @throws NoClassDefFoundError  If Plan is not installed and this class can not be found or if older Plan version is installed.
     * @throws IllegalStateException If Plan is installed, but not enabled.
     */
    static CapabilityService getInstance() {
        return Optional.ofNullable(Holder.service)
                .orElseThrow(() -> new IllegalStateException("CapabilityService has not been initialised yet."));
    }

    /**
     * Register a method to be called when Plan reloads.
     *
     * @param isEnabledListener The boolean given to the method tells if Plan has enabled successfully.
     */
    void registerEnableListener(Consumer<Boolean> isEnabledListener);

    /**
     * Check if the API on the current version provides a capability.
     *
     * @param capabilityName Name of a capability
     * @return true if the capability is available.
     * @see Capability for different capabilityNames.
     */
    default boolean hasCapability(String capabilityName) {
        return Capability.getByName(capabilityName).isPresent();
    }

    class Holder {
        static CapabilityService service;

        private Holder() {
            /* Static variable holder */
        }

        static void set(CapabilityService service) {
            Holder.service = service;
        }
    }

}
