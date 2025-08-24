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
package com.djrapitops.plan.extension;

import com.djrapitops.plan.extension.builder.ExtensionDataBuilder;
import com.djrapitops.plan.extension.extractor.ExtensionExtractor;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Interface for registering {@link DataExtension}s.
 * <p>
 * Obtaining instance:
 * - Obtain instance with {@link ExtensionService#getInstance()}.
 * - Make sure to catch a possible NoClassDefFoundError in case Plan is not installed
 * - Catch IllegalStateException in case ExtensionService is not enabled
 * <p>
 * Registering {@link DataExtension}:
 * - Register your {@link DataExtension} with {@link ExtensionService#register(DataExtension)}
 * - Catch a possible IllegalArgumentException in case the DataExtension implementation is invalid.
 *
 * @author AuroraLS3
 */
public interface ExtensionService {

    /**
     * Obtain instance of ExtensionService.
     *
     * @return ExtensionService implementation.
     * @throws NoClassDefFoundError  If Plan is not installed and this class can not be found or if older Plan version is installed.
     * @throws IllegalStateException If Plan is installed, but not enabled.
     */
    static ExtensionService getInstance() {
        return Optional.ofNullable(Holder.service.get())
                .orElseThrow(() -> new IllegalStateException("ExtensionService has not been initialised yet."));
    }

    /**
     * Register your {@link DataExtension} implementation.
     * <p>
     * You can use {@link ExtensionExtractor#validateAnnotations()} in your Unit Tests to prevent IllegalArgumentExceptions here at runtime.
     *
     * @param extension Your DataExtension implementation, see {@link DataExtension} for requirements.
     * @return Optional {@link Caller} that can be used to call for data update in Plan database manually - If the Optional is not present the user has disabled the extension in Plan config.
     * @throws IllegalArgumentException If an implementation violation is found.
     */
    Optional<Caller> register(DataExtension extension);

    /**
     * Obtain a new {@link ExtensionDataBuilder}, it is recommended to use {@link DataExtension#newExtensionDataBuilder()}.
     * <p>
     * Requires Capability DATA_EXTENSION_BUILDER_API
     *
     * @param extension Extension for which this builder is.
     * @return a new builder.
     */
    ExtensionDataBuilder newExtensionDataBuilder(DataExtension extension);

    /**
     * Unregister your {@link DataExtension} implementation.
     * <p>
     * This method should be used if calling methods on the DataExtension suddenly becomes unavailable, due to
     * plugin disable for example.
     *
     * @param extension Your DataExtension implementation that was registered before.
     */
    void unregister(DataExtension extension);

    /**
     * Singleton holder for {@link ExtensionService}.
     */
    class Holder {
        static final AtomicReference<ExtensionService> service = new AtomicReference<>();

        private Holder() {
            /* Static variable holder */
        }

        static void set(ExtensionService service) {
            Holder.service.set(service);
        }
    }

}
