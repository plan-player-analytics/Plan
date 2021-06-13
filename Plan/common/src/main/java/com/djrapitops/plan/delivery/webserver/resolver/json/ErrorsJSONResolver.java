package com.djrapitops.plan.delivery.webserver.resolver.json;

import com.djrapitops.plan.delivery.web.resolver.MimeType;
import com.djrapitops.plan.delivery.web.resolver.Resolver;
import com.djrapitops.plan.delivery.web.resolver.Response;
import com.djrapitops.plan.delivery.web.resolver.request.Request;
import com.djrapitops.plan.delivery.web.resolver.request.WebUser;
import com.djrapitops.plan.storage.file.PlanFiles;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Singleton
public class ErrorsJSONResolver implements Resolver {

    private final PlanFiles files;

    @Inject
    public ErrorsJSONResolver(PlanFiles files) {
        this.files = files;
    }

    @Override
    public boolean canAccess(Request request) {
        return request.getUser().orElse(new WebUser("")).hasPermission("page.server");
    }

    @Override
    public Optional<Response> resolve(Request request) {
        return Optional.of(getResponse());
    }

    private Response getResponse() {
        return Response.builder()
                .setMimeType(MimeType.JSON)
                .setJSONContent(loadErrorLogs())
                .build();
    }

    private List<ErrorFile> loadErrorLogs() {
        File[] files = this.files.getLogsFolder().listFiles();
        if (files == null || files.length == 0) return Collections.emptyList();

        List<ErrorFile> errorFiles = new ArrayList<>();
        for (File file : files) {
            errorFiles.add(new ErrorFile(file.getName(), read(file)));
        }

        return errorFiles;
    }

    private List<String> read(File file) {
        try (Stream<String> lines = Files.lines(file.toPath())) {
            return lines.collect(Collectors.toList());
        } catch (IOException e) {
            return Collections.singletonList("Failed to read " + file.getAbsolutePath() + ": " + e.toString());
        }
    }

    private static class ErrorFile {
        private final String fileName;
        private final List<String> contents;

        public ErrorFile(String fileName, List<String> contents) {
            this.fileName = fileName;
            this.contents = contents;
        }

        public String getFileName() {
            return fileName;
        }

        public List<String> getContents() {
            return contents;
        }
    }
}
