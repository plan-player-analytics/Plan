package com.djrapitops.plan.extension.implementation.results.player;

/**
 * Represents a data-point given by a Provider method of a DataExtension.
 *
 * @author Rsl1122
 */
public interface ExtensionData {

    /**
     * Get Descriptive information about the data point.
     *
     * @return a {@link ExtensionDescriptive}.
     */
    ExtensionDescriptive getDescriptive();

}
