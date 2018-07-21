/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system;


import com.djrapitops.plan.api.exceptions.EnableException;

/**
 * Represents a system that can be enabled and disabled.
 *
 * @author Rsl1122
 */
public interface SubSystem {

    /**
     * Performs enable actions for the subsystem.
     *
     * @throws EnableException If an error occurred during enable and it is fatal to the subsystem.
     */
    void enable() throws EnableException;

    /**
     * Performs disable actions for the subsystem
     */
    void disable();

}
