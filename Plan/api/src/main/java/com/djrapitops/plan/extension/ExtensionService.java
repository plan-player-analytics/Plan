package com.djrapitops.plan.extension;

import com.djrapitops.plan.extension.extractor.ExtensionExtractor;

import java.util.Optional;

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
 * @author Rsl1122
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
        return Optional.ofNullable(ExtensionServiceHolder.API)
                .orElseThrow(() -> new IllegalStateException("ExtensionService has not been initialised yet."));
    }

    /**
     * Register your {@link DataExtension} implementation.
     * <p>
     * You can use {@link ExtensionExtractor#validateAnnotations()} in your Unit Tests to prevent IllegalArgumentExceptions here at runtime.
     *
     * @param extension Your DataExtension implementation, see {@link DataExtension} for requirements.
     * @throws IllegalArgumentException If an implementation violation is found.
     */
    void register(DataExtension extension);

    class ExtensionServiceHolder {
        static ExtensionService API;

        private ExtensionServiceHolder() {
            /* Static variable holder */
        }

        static void set(ExtensionService api) {
            ExtensionServiceHolder.API = api;
        }
    }

}
