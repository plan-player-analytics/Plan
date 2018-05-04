package com.djrapitops.plan.system.update;

import com.djrapitops.plugin.api.utility.Version;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Test for ShutdownUpdateHook functionality.
 *
 * @author Rsl1122
 * @see ShutdownUpdateHook
 */
public class ShutdownUpdateHookTest {

    @ClassRule
    public static TemporaryFolder temporaryFolder = new TemporaryFolder();

    @Test
    public void downloadNewJar() throws IOException {
        File newJar = new File(temporaryFolder.getRoot(), "Plan-4.2.0.jar");
        ShutdownUpdateHook.downloadNewJar(new VersionInfo(
                        true,
                        new Version("4.2.0"),
                        "https://github.com/Rsl1122/Plan-PlayerAnalytics/releases/download/4.2.0/Plan-4.2.0.jar",
                        ""
                ), newJar
        );

        assertTrue(newJar.exists());
    }

    @Test
    public void deleteOldJarAndKeepNewJar() throws IOException {
        File newJar = new File(temporaryFolder.getRoot(), "Plan-4.2.0.jar");
        File oldJar = new File(temporaryFolder.getRoot(), "Plan.jar");

        assertTrue(newJar.createNewFile());
        assertTrue(oldJar.createNewFile());

        System.out.println(Arrays.toString(temporaryFolder.getRoot().listFiles()));
        ShutdownUpdateHook.registerOldJarForDeletion(temporaryFolder.getRoot(), new File(temporaryFolder.getRoot(), "Plan-4.2.0.jar"));

        new ShutdownUpdateHook().run();

        System.out.println(Arrays.toString(temporaryFolder.getRoot().listFiles()));
        assertTrue(newJar.exists());
        assertFalse(oldJar.exists());
    }
}