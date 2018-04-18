package com.djrapitops.plan.system.update;

import com.djrapitops.plugin.api.utility.Version;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertTrue;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
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

        System.out.println(Arrays.toString(temporaryFolder.getRoot().listFiles()));
        ShutdownUpdateHook.registerOldJarForDeletion(temporaryFolder.getRoot(), new File(temporaryFolder.getRoot(), "Plan-4.2.0.jar"));

        new ShutdownUpdateHook().run();

        assertTrue(newJar.exists());
    }
}