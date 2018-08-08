package com.djrapitops.plan.system.locale;

import org.junit.Test;

public class LocaleSystemTest {

    @Test
    public void noIdentifierCollisions() {
        // No Exception wanted
        LocaleSystem.getIdentifiers();
    }
}