package com.djrapitops.plan.system.update;

import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;

/**
 * Shutdown hook that updates the plugin on server shutdown.
 * <p>
 * Does not perform update on force close.
 *
 * @author Rsl1122
 */
public class ShutdownUpdateHook extends Thread {

    private static boolean activated = false;

    private static boolean isActivated() {
        return activated;
    }

    private static void activate(ShutdownUpdateHook hook) {
        activated = true;
        Runtime.getRuntime().addShutdownHook(hook);
    }

    public static void deActivate() {
        activated = false;
        Log.infoColor("§aUpdate has been cancelled.");
    }

    public void register() {
        if (isActivated()) {
            return;
        }
        Log.infoColor("§aUpdate has been scheduled, The new jar will be downloaded on server shutdown.");
        activate(this);
    }

    @Override
    public void run() {
        if (!activated) {
            return;
        }
        activated = false;
        VersionInfo available = VersionCheckSystem.getInstance().getNewVersionAvailable();

        if (!Version.isNewVersionAvailable(new Version(VersionCheckSystem.getCurrentVersion()), available.getVersion())) {
            return;
        }

        File dataFolder = PlanPlugin.getInstance().getDataFolder();
        File pluginsFolder = Check.isSpongeAvailable()
                ? dataFolder.getParentFile()
                : new File(dataFolder.getParentFile().getParentFile(), "mods");
        if (pluginsFolder == null || !pluginsFolder.isDirectory()) {
            System.out.println("Could not get plugin folder for Plan.");
            return;
        }
        File newFileLocation = new File(pluginsFolder, "Plan-" + available.getVersion() + ".jar");

        try {
            downloadNewJar(available, newFileLocation);
            deleteOldJar(pluginsFolder, newFileLocation);
        } catch (IOException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    private void deleteOldJar(File pluginsFolder, File newFileLocation) {
        File[] files = pluginsFolder.listFiles();
        if (files == null) {
            System.out.println("Could not delete old jar.");
            return;
        }
        for (File file : files) {
            String fileName = file.getName();
            boolean isPlanJar = (fileName.startsWith("Plan-")
                    && fileName.endsWith(".jar"))
                    || fileName.equals("Plan.jar");
            boolean isNewJar = fileName.equals(newFileLocation.getName());
            if (isPlanJar && !isNewJar) {
                file.deleteOnExit();
            }
        }
    }

    private void downloadNewJar(VersionInfo available, File newFileLocation) throws IOException {
        URL downloadFrom = new URL(available.getDownloadUrl());

        ReadableByteChannel rbc = Channels.newChannel(downloadFrom.openStream());
        FileOutputStream fos = new FileOutputStream(newFileLocation);
        fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
    }

}