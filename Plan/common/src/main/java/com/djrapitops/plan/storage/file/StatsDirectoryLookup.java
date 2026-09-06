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
package com.djrapitops.plan.storage.file;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;

/**
 * Utility for finding stats directory.
 *
 * @author AuroraLS3
 */
public class StatsDirectoryLookup {

    private final Path serverRoot;

    public StatsDirectoryLookup(Path serverRoot) {this.serverRoot = serverRoot;}

    public Optional<Path> lookupStatsDirectory() throws IOException {
        // The stats folder is in one world folder
        // Either
        // - world/stats (old versions)
        // - world/players/stats (26.1 onwards)
        Set<String> skipList = Set.of(
                ".fabric",
                ".mixin.out",
                "DIM-1",
                "DIM1",
                "advancements",
                "bundler",
                "cache",
                "config",
                "crash-reports",
                "data",
                "datapacks",
                "defaultconfigs",
                "delete",
                "dimensions",
                "entities",
                "lang",
                "libraries",
                "logs",
                "mods",
                "modules",
                "plugins",
                "poi",
                "region",
                "resource_packs",
                "versions"
        );

        AtomicReference<Path> found = new AtomicReference<>();

        Files.walkFileTree(serverRoot, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                String name = dir.getFileName().toString();

                if (!dir.equals(serverRoot) && skipList.contains(name) || name.startsWith(".")) {
                    return FileVisitResult.SKIP_SUBTREE;
                }

                if ("stats".equals(name)) {
                    try (Stream<Path> files = Files.list(dir)) {
                        if (files.anyMatch(file -> file.toFile().getName().endsWith(".json"))) {
                            found.set(dir);
                            return FileVisitResult.TERMINATE;
                        }
                    }
                }

                return FileVisitResult.CONTINUE;
            }
        });
        return Optional.ofNullable(found.get());
    }

}
