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
package com.djrapitops.plan.delivery.rendering.pages;

import com.djrapitops.plan.delivery.rendering.BundleAddressCorrection;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.web.ResourceService;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.djrapitops.plan.identification.ServerInfo;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.queries.objects.ServerQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.PublicHtmlFiles;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.version.VersionChecker;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Factory for creating different {@link Page} objects.
 *
 * @author AuroraLS3
 */
@Singleton
public class PageFactory {

    private final Lazy<VersionChecker> versionChecker;
    private final Lazy<PlanFiles> files;
    private final Lazy<PublicHtmlFiles> publicHtmlFiles;
    private final Lazy<Theme> theme;
    private final Lazy<DBSystem> dbSystem;
    private final Lazy<BundleAddressCorrection> bundleAddressCorrection;
    private static final String ERROR_HTML_FILE = "error.html";

    @Inject
    public PageFactory(
            Lazy<VersionChecker> versionChecker,
            Lazy<PlanFiles> files,
            Lazy<PublicHtmlFiles> publicHtmlFiles,
            Lazy<Theme> theme,
            Lazy<DBSystem> dbSystem,
            Lazy<ServerInfo> serverInfo,
            Lazy<BundleAddressCorrection> bundleAddressCorrection
    ) {
        this.versionChecker = versionChecker;
        this.files = files;
        this.publicHtmlFiles = publicHtmlFiles;
        this.theme = theme;
        this.dbSystem = dbSystem;
        this.bundleAddressCorrection = bundleAddressCorrection;
    }

    public Page playersPage() throws IOException {
        return reactPage();
    }

    public Page reactPage() throws IOException {
        try {
            String fileName = "index.html";
            WebResource resource = ResourceService.getInstance().getResource(
                    "Plan", fileName, () -> getPublicHtmlOrJarResource(fileName)
            );
            return new ReactPage(bundleAddressCorrection.get(), resource);
        } catch (UncheckedIOException readFail) {
            throw readFail.getCause();
        }
    }

    /**
     * Create a server page.
     *
     * @param serverUUID UUID of the server
     * @return {@link Page} that matches the server page.
     * @throws NotFoundException If the server can not be found in the database.
     * @throws IOException       If the template files can not be read.
     */
    public Page serverPage(ServerUUID serverUUID) throws IOException {
        if (dbSystem.get().getDatabase().query(ServerQueries.fetchServerMatchingIdentifier(serverUUID)).isEmpty()) {
            throw new NotFoundException("Server not found in the database");
        }
        return reactPage();
    }

    public Page playerPage() throws IOException {
        return reactPage();
    }

    public Page networkPage() throws IOException {
        return reactPage();
    }

    public Page internalErrorPage(String message, @Untrusted Throwable error) {
        try {
            return new InternalErrorPage(
                    getResourceAsString(ERROR_HTML_FILE), message, error,
                    versionChecker.get());
        } catch (IOException noParse) {
            return () -> "Error occurred: " + error.toString() +
                    ", additional error occurred when attempting to render error page to user: " +
                    noParse;
        }
    }

    public Page errorPage(String title, String error) throws IOException {
        return new ErrorMessagePage(
                getResourceAsString(ERROR_HTML_FILE), title, error,
                versionChecker.get(), theme.get());
    }

    public Page errorPage(Icon icon, String title, String error) throws IOException {
        return new ErrorMessagePage(
                getResourceAsString(ERROR_HTML_FILE), icon, title, error, theme.get(), versionChecker.get());
    }

    public String getResourceAsString(String name) throws IOException {
        return getResource(name).asString();
    }

    public WebResource getResource(String resourceName) throws IOException {
        try {
            return ResourceService.getInstance().getResource("Plan", resourceName,
                    () -> files.get().getResourceFromJar("web/" + resourceName).asWebResource()
            );
        } catch (UncheckedIOException readFail) {
            throw readFail.getCause();
        }
    }

    public WebResource getPublicHtmlOrJarResource(String resourceName) {
        return publicHtmlFiles.get().findPublicHtmlResource(resourceName)
                .orElseGet(() -> files.get().getResourceFromJar("web/" + resourceName))
                .asWebResource();
    }

    public Page loginPage() throws IOException {
        return reactPage();
    }

    public Page registerPage() throws IOException {
        return reactPage();
    }

    public Page queryPage() throws IOException {
        return reactPage();
    }

    public Page errorsPage() throws IOException {
        return reactPage();
    }
}