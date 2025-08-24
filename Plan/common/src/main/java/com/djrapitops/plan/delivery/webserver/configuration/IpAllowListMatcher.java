/*
 *  This file is part of Player Analytics (Plan).
 *
 *  Plan is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License v3 as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  Plan is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with Plan. If not, see <https://www.gnu.org/licenses/>.
 */
package com.djrapitops.plan.delivery.webserver.configuration;

import com.djrapitops.plan.exceptions.EnableException;
import com.djrapitops.plan.exceptions.LibraryLoadingException;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.utilities.dev.Untrusted;
import dev.vankka.dependencydownload.ApplicationDependencyManager;
import dev.vankka.dependencydownload.DependencyManager;
import dev.vankka.dependencydownload.classloader.IsolatedClassLoader;
import dev.vankka.dependencydownload.repository.MavenRepository;
import dev.vankka.dependencydownload.resource.DependencyDownloadResource;
import net.playeranalytics.plugin.server.PluginLogger;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Uses ipaddress library to support multiple formats of IP address ranges.
 *
 * @author AuroraLS3
 */
@Singleton
public class IpAllowListMatcher {

    private static final boolean DOWNLOAD_LIBRARY = true;
    protected final ApplicationDependencyManager applicationDependencyManager;
    private final PluginLogger logger;
    private final PlanFiles files;
    private final AddressAllowList addressAllowList;
    private final AtomicBoolean failedDownload = new AtomicBoolean(false);
    private ClassLoader libraryClassLoader;

    @Inject
    public IpAllowListMatcher(
            PluginLogger logger,
            PlanFiles files,
            AddressAllowList addressAllowList,
            ApplicationDependencyManager applicationDependencyManager
    ) {
        this.logger = logger;
        this.files = files;
        this.addressAllowList = addressAllowList;
        this.applicationDependencyManager = applicationDependencyManager;
    }

    public synchronized void prepare() {
        if (libraryClassLoader == null && !failedDownload.get()) {
            try {
                downloadLibrary();
            } catch (ExecutionException e) {
                logger.error("Failed to download IP address parser for IP Allowlist, only exact IP matches will be supported.", e);
                failedDownload.set(true);
            }
        }
    }

    private void downloadLibrary() throws ExecutionException {
        if (DOWNLOAD_LIBRARY) {
            logger.info("Downloading IP Address parsing library for Allowlist checking, this may take a while...");
            DependencyManager dependencyManager = new DependencyManager(
                    applicationDependencyManager.getDependencyPathProvider(),
                    applicationDependencyManager.getLogger()
            );
            dependencyManager.loadResource(DependencyDownloadResource.parse(getDependencyResource()));

            try {
                dependencyManager.downloadAll(null, List.of(
                        new MavenRepository("https://repo1.maven.org/maven2")
                )).get();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            IsolatedClassLoader classLoader = new IsolatedClassLoader();
            dependencyManager.load(null, classLoader);

            // Include this dependency manager in the application dependency manager for library cleaning purposes
            applicationDependencyManager.include(dependencyManager);

            this.libraryClassLoader = classLoader;
        } else {
            libraryClassLoader = getClass().getClassLoader();
        }
    }

    public boolean isAllowed(@Untrusted String accessAddress) {
        if (failedDownload.get()) {
            return exactMatchAllowCheck(accessAddress);
        }

        try {
            List<String> addresses = addressAllowList.getAllowedAddresses();
            IPLibraryAccessor libraryAccessor = new IPLibraryAccessor(libraryClassLoader);
            return libraryAccessor.isAllowed(accessAddress, addresses);
        } catch (LibraryLoadingException e) {
            logger.error(e.toString());
            return exactMatchAllowCheck(accessAddress);
        }
    }

    private boolean exactMatchAllowCheck(@Untrusted String accessAddress) {
        List<String> allowed = addressAllowList.getAllowedAddresses();
        return allowed.isEmpty() || allowed.contains(accessAddress);
    }

    protected List<String> getDependencyResource() {
        try {
            return files.getResourceFromJar("dependencies/ipAddressMatcher.txt").asLines();
        } catch (IOException e) {
            throw new EnableException("Failed to read ipAddressMatcher dependency information from jar", e);
        }
    }
}
