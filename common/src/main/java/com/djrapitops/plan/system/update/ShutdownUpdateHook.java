package com.djrapitops.plan.system.update;

import com.djrapitops.plan.PlanHelper;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plugin.api.Check;
import com.djrapitops.plugin.api.utility.Version;
import com.djrapitops.plugin.api.utility.log.Log;

import java.io.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.HashSet;
import java.util.Set;

/**
 * Shutdown hook that updates the plugin on server shutdown.
 * <p>
 * Does not perform update on force close.
 *
 * @author Rsl1122
 */
public class ShutdownUpdateHook extends Thread {

    private static boolean activated = false;
    private static File newJar;

    private static Set<File> toDelete = new HashSet<>();

    private static boolean isActivated() {
        return activated;
    }

    public static void activate() {
        activated = true;
        VersionInfo available = VersionCheckSystem.getInstance().getNewVersionAvailable();

        if (!Version.isNewVersionAvailable(new Version(VersionCheckSystem.getCurrentVersion()), available.getVersion())) {
            return;
        }
        try {
            File pluginsFolder = getPluginsFolder();
            newJar = new File(pluginsFolder, "Plan-" + available.getVersion() + ".jar");

            downloadNewJar(available, newJar);
            registerOldJarForDeletion(pluginsFolder, newJar);
        } catch (IOException e) {
            Log.toLog(ShutdownUpdateHook.class, e);
        }
    }

    private static File getPluginsFolder() throws FileNotFoundException {
        File dataFolder = PlanHelper.getInstance().getDataFolder();
        File pluginsFolder = Check.isSpongeAvailable()
                ? new File(dataFolder.getParentFile().getParentFile().getPath(), "mods")
                : new File(dataFolder.getParentFile().getPath());
        if (!pluginsFolder.isDirectory()) {
            throw new FileNotFoundException("Could not get plugin folder for Plan.");
        }
        return pluginsFolder;
    }

    public static void deActivate() {
        activated = false;
        Log.infoColor("§aUpdate has been cancelled.");

        if (newJar != null && newJar.exists()) {
            if (!newJar.delete()) {
                newJar.deleteOnExit();
            }
        }
        toDelete.clear();
    }

    public static void registerOldJarForDeletion(File pluginsFolder, File newFileLocation) throws FileNotFoundException {
        File[] files = pluginsFolder.listFiles();
        if (files == null) {
            throw new FileNotFoundException("Could not delete old jar.");
        }
        for (File file : files) {
            String fileName = file.getName();
            boolean isPlanJar = (fileName.startsWith("Plan-") && fileName.endsWith(".jar"))
                    || fileName.equals("Plan.jar");
            boolean isNewJar = fileName.equals(newFileLocation.getName());
            if (isPlanJar && !isNewJar) {
                toDelete.add(file);
            }
        }
    }

    public static void downloadNewJar(VersionInfo available, File newFileLocation) throws IOException {
        URL downloadFrom = new URL(available.getDownloadUrl());
        try (
                BufferedInputStream in = new BufferedInputStream(downloadFrom.openStream());
                FileOutputStream fout = new FileOutputStream(newFileLocation)
        ) {
            final byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        }
    }

    public void register() {
        if (isActivated()) {
            return;
        }
        Log.infoColor("§aUpdate has been scheduled, Downloading new jar.. Restart server to take effect.");
        activate();
        Runtime.getRuntime().addShutdownHook(this);
    }

    @Override
    public void run() {
        if (!(Check.isBukkitAvailable() && Check.isSpongeAvailable() && Check.isBungeeAvailable())) {
            unloadJar();
        }

        for (File f : toDelete) {
            if (!f.delete()) {
                f.deleteOnExit();
            }
        }
    }

    private void unloadJar() {
        ClassLoader classLoader = PlanPlugin.class.getClassLoader();

        if (classLoader instanceof URLClassLoader) {
            try {
                ((URLClassLoader) classLoader).close();
            } catch (IOException e) {
                // Loggers may be unavailable.
                e.printStackTrace();
            }
        }

        System.gc();
    }

}