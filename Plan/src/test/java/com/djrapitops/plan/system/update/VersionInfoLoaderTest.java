package com.djrapitops.plan.system.update;

import com.djrapitops.plugin.api.utility.Version;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VersionInfoLoaderTest {

    @Test
    public void versionLoaderTest() throws IOException {
        List<VersionInfo> versions = VersionInfoLoader.load();

        VersionInfo oldest = versions.get(versions.size() - 1);
        assertEquals(new Version("4.1.7"), oldest.getVersion());
        assertTrue(oldest.isRelease());
        assertTrue(oldest.isTrusted());
    }

}