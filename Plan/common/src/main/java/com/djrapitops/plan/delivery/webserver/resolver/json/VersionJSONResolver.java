package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.version.VersionChecker;
import com.djrapitops.plan.version.VersionInfo;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Resolves requests for /v1/version.
 *
 * @author Kopo942
 */
public class VersionJSONResolver implements Resolver {

    private final VersionChecker versionChecker;
    private final String currentVersion;

    @Inject
    public VersionJSONResolver(
            @Named("currentVersion") String currentVersion,
            VersionChecker versionChecker
    ) {
        this.currentVersion = currentVersion;
        this.versionChecker = versionChecker;
    }

    @Override
    public boolean canAccess(Request request) {
        return true;
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        Map<String, Object> json = new HashMap<>();
        Optional<VersionInfo> newVersion = versionChecker.getNewVersionAvailable();
        boolean updateAvailable = newVersion.isPresent();

        json.put("currentVersion", this.currentVersion);
        json.put("updateAvailable", updateAvailable);

        if (updateAvailable) {
            json.put("newVersion", newVersion.get().getVersion().asString());
            json.put("downloadUrl", newVersion.get().getDownloadUrl());
            json.put("changelogUrl", newVersion.get().getChangeLogUrl());
            json.put("isRelease", newVersion.get().isRelease());
        }

        return Response.builder().setJSONContent(json).build();
    }
}
