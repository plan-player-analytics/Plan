package com.djrapitops.plan.identification;

import org.junit.jupiter.api.Test;
import org.junit.platform.runner.JUnitPlatform;
import org.junit.runner.RunWith;
import utilities.OptionalAssert;

import java.util.UUID;

/**
 * Tests for {@link UUIDUtility}.
 *
 * @author Rsl1122
 */
@RunWith(JUnitPlatform.class)
class UUIDUtilityTest {

    @Test
    void stringUUIDIsParsed() {
        String test = "f3cc3e96-1bc9-35ad-994f-d894e9764b93";
        OptionalAssert.equals(UUID.fromString(test), UUIDUtility.parseFromString(test));
    }
}