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
import com.djrapitops.plan.delivery.formatting.Formatter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.rendering.html.icon.Family;
import com.djrapitops.plan.delivery.rendering.html.icon.Icon;
import com.djrapitops.plan.delivery.rendering.pages.Page;
import com.djrapitops.plan.delivery.rendering.pages.PageFactory;
import com.djrapitops.plan.delivery.web.ResourceService;
import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.ResponseBuilder;
import com.djrapitops.plan.delivery.web.resolver.exception.NotFoundException;
import com.djrapitops.plan.delivery.web.resource.WebResource;
import com.djrapitops.plan.delivery.webserver.auth.FailReason;
import com.djrapitops.plan.exceptions.WebUserAuthException;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.locale.Locale;
import com.djrapitops.plan.settings.locale.lang.ErrorPageLang;
import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.storage.database.DBSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.database.queries.containers.ContainerFetchQueries;
import com.djrapitops.plan.storage.file.PlanFiles;
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
import java.util.ArrayList;
import java.util.List;
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

    private final PlanFiles files;
    private final PageFactory pageFactory;
    private final Locale locale;
    private final DBSystem dbSystem;
    private final Theme theme;
    private final Lazy<Addresses> addresses;
    private final Formatter<Long> httpLastModifiedFormatter;

    @Inject
    public ResponseFactory(
            PlanFiles files,
            PageFactory pageFactory,
            Locale locale,
            DBSystem dbSystem,
            Formatters formatters,
            Theme theme,
            Lazy<Addresses> addresses
    ) {
        this.files = files;
        this.pageFactory = pageFactory;
        this.locale = locale;
        this.dbSystem = dbSystem;
        this.theme = theme;
        this.addresses = addresses;

        httpLastModifiedFormatter = formatters.httpLastModifiedLong();
    }

    public WebResource getResource(@Untrusted String resourceName) {
        return ResourceService.getInstance().getResource("Plan", resourceName,
                () -> files.getResourceFromJar("web/" + resourceName).asWebResource());
    }

    private Response forPage(Page page) {
        long now = System.currentTimeMillis(); // TODO implement use of last modified date.
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(page.toHtml())
                .setHeader(HttpHeader.CACHE_CONTROL.asString(), CacheStrategy.CHECK_ETAG)
                .setHeader(HttpHeader.LAST_MODIFIED.asString(), httpLastModifiedFormatter.apply(now))
                .setHeader(HttpHeader.ETAG.asString(), now)
                .build();
    }

    private Response forInternalError(@Untrusted Throwable error, String cause) {
        return Response.builder()
                .setMimeType(MimeType.HTML)
                .setContent(pageFactory.internalErrorPage(cause, error).toHtml())
                .setStatus(500)
                .build();
    }

    public Response playersPageResponse() {
        try {
            Optional<Response> error = checkDbClosedError();
            if (error.isPresent()) return error.get();
            return forPage(pageFactory.playersPage());
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
        WebResource resource = getResource(fileName);
        Optional<Long> lastModified = resource.getLastModified();
        if (lastModified.isPresent() && modified == lastModified.get()) {
            return Response.builder()
                    .setStatus(304)
                    .setContent(new byte[0])
                    .build();
        } else {
            return newResponseFunction.apply(fileName);
        }
    }

    public Response internalErrorResponse(Throwable e, String cause) {
        return forInternalError(e, cause);
    }

    public Response networkPageResponse() {
        Optional<Response> error = checkDbClosedError();
        if (error.isPresent()) return error.get();
        try {
            return forPage(pageFactory.networkPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate network page");
        }
    }

    public Response serverPageResponse(ServerUUID serverUUID) {
        Optional<Response> error = checkDbClosedError();
        if (error.isPresent()) return error.get();
        try {
            return forPage(pageFactory.serverPage(serverUUID));
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
            WebResource resource = getResource(fileName);
            String content = UnaryChain.of(resource.asString())
                    .chain(this::replaceMainAddressPlaceholder)
                    .chain(theme::replaceThemeColors)
                    .chain(contents -> {
                        if (fileName.startsWith("vendor/") || fileName.startsWith("/vendor/")) {return contents;}
                        return locale.replaceLanguageInJavascript(contents);
                    })
                    .chain(contents -> StringUtils.replace(contents, "n.p=\"/\"",
                            "n.p=\"" + getBasePath() + "/\""))
                    .apply();
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(MimeType.JS)
                    .setContent(content)
                    .setStatus(200);

            if (fileName.contains("static")) {
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
            WebResource resource = getResource(fileName);
            String content = UnaryChain.of(resource.asString())
                    .chain(theme::replaceThemeColors)
                    .chain(contents -> StringUtils.replace(contents, "/static", getBasePath() + "/static"))
                    .apply();

            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(MimeType.CSS)
                    .setContent(content)
                    .setStatus(200);

            if (fileName.contains("static")) {
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
            WebResource resource = getResource(fileName);
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(MimeType.IMAGE)
                    .setContent(resource)
                    .setStatus(200);

            if (fileName.contains("static")) {
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
            WebResource resource = getResource(fileName);
            ResponseBuilder responseBuilder = Response.builder()
                    .setMimeType(type)
                    .setContent(resource);

            if (fileName.contains("static")) {
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

    public Response redirectResponse(String location) {
        return Response.builder().redirectTo(location).build();
    }

    public Response faviconResponse() {
        try {
            return Response.builder()
                    .setMimeType(MimeType.FAVICON)
                    .setContent(getResource("favicon.ico"))
                    .build();
        } catch (UncheckedIOException e) {
            return forInternalError(e, "Could not read favicon");
        }
    }

    public Response robotsResponse() {
        try {
            WebResource resource = getResource("robots.txt");
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

    public Response basicAuthFail(WebUserAuthException e) {
        try {
            FailReason failReason = e.getFailReason();
            String reason = failReason.getReason();
            if (failReason == FailReason.ERROR) {
                StringBuilder errorBuilder = new StringBuilder("</p><pre>");
                for (String line : getStackTrace(e.getCause())) {
                    errorBuilder.append(line);
                }
                errorBuilder.append("</pre>");

                reason += errorBuilder.toString();
            }
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("lock").build(), "401 Unauthorized", "Authentication Failed.</p><p><b>Reason: " + reason + "</b></p><p>").toHtml())
                    .setStatus(401)
                    .setHeader("WWW-Authenticate", "Basic realm=\"" + failReason.getReason() + "\"")
                    .build();
        } catch (IOException jarReadFailed) {
            return forInternalError(e, "Failed to generate PromptAuthorizationResponse");
        }
    }

    private List<String> getStackTrace(Throwable throwable) {
        List<String> stackTrace = new ArrayList<>();
        stackTrace.add(throwable.toString());
        for (StackTraceElement element : throwable.getStackTrace()) {
            stackTrace.add("    " + element.toString());
        }

        Throwable cause = throwable.getCause();
        if (cause != null) {
            List<String> causeTrace = getStackTrace(cause);
            if (!causeTrace.isEmpty()) {
                causeTrace.set(0, "Caused by: " + causeTrace.get(0));
                stackTrace.addAll(causeTrace);
            }
        }

        return stackTrace;
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

    public Response basicAuth() {
        try {
            String tips = "<br>- Ensure you have registered a user with <b>/plan register</b><br>"
                    + "- Check that the username and password are correct<br>"
                    + "- Username and password are case-sensitive<br>"
                    + "<br>If you have forgotten your password, ask a staff member to delete your old user and re-register.";
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.errorPage(Icon.called("lock").build(), "401 Unauthorized", "Authentication Failed." + tips).toHtml())
                    .setStatus(401)
                    .setHeader("WWW-Authenticate", "Basic realm=\"Plan WebUser (/plan register)\"")
                    .build();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate PromptAuthorizationResponse");
        }
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

    public Response playerPageResponse(UUID playerUUID) {
        try {
            return forPage(pageFactory.playerPage(playerUUID));
        } catch (IllegalStateException e) {
            return playerNotFound404();
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate player page");
        }
    }

    public Response loginPageResponse() {
        try {
            return forPage(pageFactory.loginPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate player page");
        }
    }

    public Response registerPageResponse() {
        try {
            return forPage(pageFactory.registerPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate player page");
        }
    }

    public Response queryPageResponse() {
        try {
            return forPage(pageFactory.queryPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate query page");
        }
    }

    public Response errorsPageResponse() {
        try {
            return forPage(pageFactory.errorsPage());
        } catch (IOException e) {
            return forInternalError(e, "Failed to generate errors page");
        }
    }

    public Response jsonFileResponse(String file) {
        try {
            return Response.builder()
                    .setMimeType(MimeType.JSON)
                    .setContent(getResource(file))
                    .build();
        } catch (UncheckedIOException e) {
            return forInternalError(e, "Could not read " + file);
        }
    }

    public Response reactPageResponse() {
        try {
            return Response.builder()
                    .setMimeType(MimeType.HTML)
                    .setContent(pageFactory.reactPage().toHtml())
                    .build();
        } catch (UncheckedIOException | IOException e) {
            return forInternalError(e, "Could not read index.html");
        }
    }
}
