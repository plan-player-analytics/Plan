package com.djrapitops.plan.common;

/**
 * Really dirty temporary hack.
 */
public class PlanHelper {
    private static PlanPlugin instance;

    public static PlanPlugin getInstance() {
        return instance;
    }

    public static void setInstance(PlanPlugin planPlugin) {
        instance = planPlugin;
    }
}
