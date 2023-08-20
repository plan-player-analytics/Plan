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
package com.djrapitops.plan.delivery.webserver;

import com.djrapitops.plan.delivery.domain.container.PlayerContainer;
import com.djrapitops.plan.delivery.domain.keys.PlayerKeys;
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.icon.Family;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.rendering.pages.Page;
import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.ResponseBuilder;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.djrapitops.plan.identification.Identifiers;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.ErrorPageLang;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.PublicHtmlFiles;
import com.djrapitops.plan.storage.file.Resource;
import com.djrapitops.plan.utilities.dev.Untrusted;
import com.djrapitops.plan.utilities.java.Maps;
import com.djrapitops.plan.utilities.java.UnaryChain;
import dagger.Lazy;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.eclipse.jetty.http.HttpHeader;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

/**
 * Factory for creating different {@link Response} objects.
 *
 * @author AuroraLS3
 */
@Singleton
public class ResponseFactory {

    private static final String STATIC_BUNDLE_FOLDER = "static";

    private final PlanFiles files;
    private final PublicHtmlFiles publicHtmlFiles;
    private final PageFactory pageFactory;
    private final Locale locale;
    private final DBSystem dbSystem;
    private final Theme theme;
    private final Lazy<Addresses> addresses;
    private final Formatter<Long> httpLastModifiedFormatter;

    @Inject
    public ResponseFactory(
            PlanFiles files,
            PlanConfig config, PublicHtmlFiles publicHtmlFiles,
            PageFactory pageFactory,
            Locale locale,
            DBSystem dbSystem,
            Formatters formatters,
            Theme theme,
            Lazy<Addresses> addresses
    ) {
        this.files = files;
        this.publicHtmlFiles = publicHtmlFiles;
        this.pageFactory = pageFactory;
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.theme = theme;
        this.addresses = addresses;

        httpLastModifiedFormatter = formatters.httpLastModifiedLong();
    }

    /**
     * @throws UncheckedIOException If reading the resource fails
     */
    private WebResource getPublicOrJarResource(@Untrusted String resourceName) {
        return publicHtmlFiles.findPublicHtmlResource(resourceName)
                .orElseGet(() -> files.getResourceFromJar("web/" + resourceName))
                .asWebResource();
    }

    private static Response browserCachedNotChangedResponse() {
        return Response.builder()
                .setStatus(304)
                .setContent(new byte[0])
                .build();
    }

    private Response forPage(@Untrusted Request request, Page page) {
        return forPage(request, page, 200);
    }

    private Response forPage(@Untrusted Request request, Page page, int responseCode) {
        long modified = page.lastModified();
        Optional<Long> etag = Identifiers.getEtag(request);

        if (etag.isPresent() && modified == etag.get()) {
            return browserCachedNotChangedResponse();
        }

        return Response.builder()
                .setStatus(responseCode)
                .setMimeType(MimeType.HTML)
                .setContent(page.toHtml())
                .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(modified))
                .setHeader(HttpHeader.ETAG.asString(), modified)
                .build();
    }

    private Response forInternalError(@Untrusted Throwable error, String cause) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(pageFactory.internalErrorPage(cause, error).toHtml())
                .setStatus(500)
                .build();
    }

    public Response playersPageResponse(@Untrusted Request request) {
        try {
            Optional<Response> error = checkDbClosedError();
            if (error.isPresent()) return error.get();
            return forPage(request, pageFactory.playersPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate players page");
        }
    }

    private Optional<Response> checkDbClosedError() {
        Database.State dbState = dbSystem.getDatabase().getState();
        if (dbState != Database.State.OPEN) {
            try {
                return Optional.of(buildDBNotOpenResponse(dbState));
            } catch (IOException e) {
                return Optional.of(forInternalError(e, "Database was not open, additionally failed to generate error page for that"));
            }
        }
        return Optional.empty();
    }

    private Response buildDBNotOpenResponse(Database.State dbState) throws IOException {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(pageFactory.errorPage(
                        "503 Resources Unavailable",
                        "Database is " + dbState.name() + " - Please try again later. You can check database status with /plan info"
                ).toHtml())
                .setStatus(503)
                .build();
    }

    private Response getCachedOrNew(long modified, String fileName, Function<String, Response> newResponseFunction) {
        WebResource resource = getPublicOrJarResource(fileName);
        Optional<Long> lastModified = resource.getLastModified();
        if (lastModified.isPresent() && modified == lastModified.get()) {
            return browserCachedNotChangedResponse();
        } else {
            return newResponseFunction.apply(fileName);
        }
    }

    public Response internalErrorResponse(Throwable e, String cause) {
        return forInternalError(e, cause);
    }

    public Response networkPageResponse(@Untrusted Request request) {
        Optional<Response> error = checkDbClosedError();
        if (error.isPresent()) return error.get();
        try {
            return forPage(request, pageFactory.networkPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate network page");
        }
    }

    public Response serverPageResponse(@Untrusted Request request, ServerUUID serverUUID) {
        Optional<Response> error = checkDbClosedError();
        if (error.isPresent()) return error.get();
        try {
            return forPage(request, pageFactory.serverPage(serverUUID));
        } catch (NotFoundException e) {
            return notFound404(e.getMessage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate server page");
        }
    }

    public Response rawPlayerPageResponse(UUID playerUUID) {
        PlayerContainer player = dbSystem.getDatabase().query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(player.mapToNormalMap())
                .build();
    }

    public Response javaScriptResponse(long modified, @Untrusted String fileName) {
        return getCachedOrNew(modified, fileName, this::javaScriptResponse);
    }

    public Response javaScriptResponse(@Untrusted String fileName) {
        try {
            WebResource resource = getPublicOrJarResource(fileName);
            String content = UnaryChain.of(resource.asString())
                    .chain(this::replaceMainAddressPlaceholder)
                    .chain(theme::replaceThemeColors)
                    .chain(contents -> {
                        if (fileName.contains(STATIC_BUNDLE_FOLDER) || fileName.startsWith("vendor/") || fileName.startsWith("/vendor/")) {
                            return contents;
                        }
                        return locale.replaceLanguageInJavascript(contents);
                    })
                    .chain(contents -> StringUtils.replace(contents,
                            ".p=\"/\"",
                            ".p=\"" + getBasePath() + "/\""))
                    .apply();
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(MimeType.JS)
                    .setContent(content)
                    .setStatus(200);

            if (fileName.contains(STATIC_BUNDLE_FOLDER)) {
                resource.getLastModified().ifPresent(lastModified -> responseBuilder
                        // Can't cache main bundle in browser since base path might change
                        .setHeader(HttpHeader.CACHE_CONTROL.asString(), fileName.contains("main") ? CacheStrategy.CHECK_ETAG : CacheStrategy.CACHE_IN_BROWSER)
                        .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                        .setHeader(HttpHeader.ETAG.asString(), lastModified));
            }
            return responseBuilder.build();
        } catch (UncheckedIOException e) {
            return notFound404("Javascript File not found");
        }
    }

    private String getBasePath() {
        String address = addresses.get().getMainAddress()
                .orElseGet(addresses.get()::getFallbackLocalhostAddress);
        return addresses.get().getBasePath(address);
    }

    private String replaceMainAddressPlaceholder(String resource) {
        String address = addresses.get().getAccessAddress()
                .orElseGet(addresses.get()::getFallbackLocalhostAddress);
        return StringUtils.replace(resource, "PLAN_BASE_ADDRESS", address);
    }

    public Response cssResponse(long modified, @Untrusted String fileName) {
        return getCachedOrNew(modified, fileName, this::cssResponse);
    }

    public Response cssResponse(@Untrusted String fileName) {
        try {
            WebResource resource = getPublicOrJarResource(fileName);
            String content = UnaryChain.of(resource.asString())
                    .chain(theme::replaceThemeColors)
                    .chain(contents -> StringUtils.replace(contents, "/static", getBasePath() + "/static"))
                    .apply();

            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(MimeType.CSS)
                    .setContent(content)
                    .setStatus(200);

            if (fileName.contains(STATIC_BUNDLE_FOLDER)) {
                resource.getLastModified().ifPresent(lastModified -> responseBuilder
                        // Can't cache css bundles in browser since base path might change
                        .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                        .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                        .setHeader(HttpHeader.ETAG.asString(), lastModified));
            }
            return responseBuilder.build();
        } catch (UncheckedIOException e) {
            return notFound404("CSS File not found");
        }
    }

    public Response imageResponse(long modified, @Untrusted String fileName) {
        return getCachedOrNew(modified, fileName, this::imageResponse);
    }

    public Response imageResponse(@Untrusted String fileName) {
        try {
            WebResource resource = getPublicOrJarResource(fileName);
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(MimeType.IMAGE)
                    .setContent(resource)
                    .setStatus(200);

            if (fileName.contains(STATIC_BUNDLE_FOLDER)) {
                resource.getLastModified().ifPresent(lastModified -> responseBuilder
                        .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CACHE_IN_BROWSER)
                        .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                        .setHeader(HttpHeader.ETAG.asString(), lastModified));
            }
            return responseBuilder.build();
        } catch (UncheckedIOException e) {
            return notFound404("Image File not found");
        }
    }

    public Response fontResponse(long modified, @Untrusted String fileName) {
        return getCachedOrNew(modified, fileName, this::fontResponse);
    }

    public Response fontResponse(@Untrusted String fileName) {
        String type;
        if (fileName.endsWith(".woff")) {
            type = MimeType.FONT_WOFF;
        } else if (fileName.endsWith(".woff2")) {
            type = MimeType.FONT_WOFF2;
        } else if (fileName.endsWith(".eot")) {
            type = MimeType.FONT_EOT;
        } else if (fileName.endsWith(".ttf")) {
            type = MimeType.FONT_TTF;
        } else {
            type = MimeType.FONT_BYTESTREAM;
        }
        try {
            WebResource resource = getPublicOrJarResource(fileName);
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(type)
                    .setContent(resource);

            if (fileName.contains(STATIC_BUNDLE_FOLDER)) {
                resource.getLastModified().ifPresent(lastModified -> responseBuilder
                        .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CACHE_IN_BROWSER)
                        .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                        .setHeader(HttpHeader.ETAG.asString(), lastModified));
            }
            return responseBuilder.build();
        } catch (UncheckedIOException e) {
            return notFound404("Font File not found");
        }
    }

    public Response publicHtmlResourceResponse(long modified, @Untrusted String fileName, String mimeType) {
        // Slightly different from getCachedOrNew
        WebResource resource = publicHtmlFiles.findPublicHtmlResource(fileName)
                .map(Resource::asWebResource)
                .orElse(null);
        if (resource == null) return null;

        Optional<Long> lastModified = resource.getLastModified();
        if (lastModified.isPresent() && modified == lastModified.get()) {
            return browserCachedNotChangedResponse();
        } else {
            return publicHtmlResourceResponse(fileName, mimeType);
        }
    }

    public Response publicHtmlResourceResponse(@Untrusted String fileName, String mimeType) {
        try {
            WebResource resource = publicHtmlFiles.findPublicHtmlResource(fileName)
                    .map(Resource::asWebResource)
                    .orElse(null);
            if (resource == null) return null;

            byte[] content = resource.asBytes();
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(mimeType)
                    .setContent(content)
                    .setStatus(200);

            if (fileName.contains(STATIC_BUNDLE_FOLDER)) {
                resource.getLastModified().ifPresent(lastModified -> responseBuilder
                        // Can't cache css bundles in browser since base path might change
                        .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                        .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                        .setHeader(HttpHeader.ETAG.asString(), lastModified));
            }
            return responseBuilder.build();
        } catch (UncheckedIOException e) {
            return notFound404("CSS File not found");
        }
    }

    public Response redirectResponse(String location) {
        return Response.builder().redirectTo(location).build();
    }

    public Response faviconResponse() {
        try {
            return Response.builder()
                    .setMimeType(MimeType.FAVICON)
                    .setContent(getPublicOrJarResource("favicon.ico"))
                    .build();
        } catch (UncheckedIOException e) {
            return forInternalError(e, "Could not read favicon");
        }
    }

    public Response robotsResponse() {
        try {
            WebResource resource = getPublicOrJarResource("robots.txt");
            Long lastModified = resource.getLastModified().orElseGet(System::currentTimeMillis);
            return Response.builder()
                    .setMimeType("text/plain")
                    .setContent(resource)
                    .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CACHE_IN_BROWSER)
                    .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(lastModified))
                    .setHeader(HttpHeader.ETAG.asString(), lastModified)
                    .build();
        } catch (UncheckedIOException e) {
            return forInternalError(e, "Could not read robots.txt");
        }
    }

    public Response pageNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UNKNOWN_PAGE_404));
    }

    public Response uuidNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.UUID_404));
    }

    public Response playerNotFound404() {
        return notFound404(locale.getString(ErrorPageLang.NOT_PLAYED_404));
    }

    public Response notFound404(String message) {
        try {
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("map-signs").build(), "404 " + message, message).toHtml())
                    .setStatus(404)
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate 404 page with message '" + message + "'");
        }
    }

    public Response forbidden403() {
        return forbidden403("Your user is not authorized to view this page.<br>"
                + "If you believe this is an error contact staff to change your access level.");
    }

    public Response forbidden403(String message) {
        try {
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("hand-paper").of(Family.REGULAR).build(), "403 Forbidden", message).toHtml())
                    .setStatus(403)
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate 403 page");
        }
    }

    public Response failedLoginAttempts403() {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent("<h1>403 Forbidden</h1>" +
                        "<p>You have too many failed login attempts. Please wait 2 minutes until attempting again.</p>" +
                        "<script>setTimeout(() => location.reload(), 120500);\" +\n" +
                        "</script>")
                .setStatus(403)
                .build();
    }

    public Response ipWhitelist403(@Untrusted String accessor) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent("<h1>403 Forbidden</h1>" +
                        "<p>IP-whitelist enabled, \"" + StringEscapeUtils.escapeHtml4(accessor) + "\" is not on the list!</p>")
                .setStatus(403)
                .build();
    }

    public Response badRequest(String errorMessage, String target) {
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(Maps.builder(String.class, Object.class)
                        .put("status", 400)
                        .put("error", errorMessage)
                        .put("requestedTarget", target)
                        .build())
                .setStatus(400)
                .build();
    }

    public Response playerPageResponse(@Untrusted Request request, UUID playerUUID) {
        try {
            Database db = dbSystem.getDatabase();
            PlayerContainer player = db.query(ContainerFetchQueries.fetchPlayerContainer(playerUUID));
            if (player.getValue(PlayerKeys.REGISTERED).isPresent()) {
                return forPage(request, pageFactory.playerPage());
            } else {
                return forPage(request, pageFactory.reactPage(), 404);
            }
        } catch (IllegalStateException notFoundLegacy) {
            return playerNotFound404();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate player page");
        }
    }

    public Response loginPageResponse(@Untrusted Request request) {
        try {
            return forPage(request, pageFactory.loginPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate login page");
        }
    }

    public Response registerPageResponse(@Untrusted Request request) {
        try {
            return forPage(request, pageFactory.registerPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate register page");
        }
    }

    public Response queryPageResponse(@Untrusted Request request) {
        try {
            return forPage(request, pageFactory.queryPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate query page");
        }
    }

    public Response errorsPageResponse(@Untrusted Request request) {
        try {
            return forPage(request, pageFactory.errorsPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate errors page");
        }
    }

    public Response jsonFileResponse(String file) {
        try {
            return Response.builder()
                    .setMimeType(MimeType.JSON)
                    .setContent(getPublicOrJarResource(file))
                    .build();
        } catch (UncheckedIOException e) {
            return forInternalError(e, "Could not read " + file);
        }
    }

    public Response reactPageResponse(Request request) {
        try {
            return forPage(request, pageFactory.reactPage());
        } catch (UncheckedIOException | IOException e) {
            return forInternalError(e, "Could not read index.html");
        }
    }
}
