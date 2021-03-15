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
package com.djrapitops.plan;

import net.playeranalytics.plugin.dependencies.DependencyLoader;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.IOException;

public class DependencyStartup {

    private static final String REPOSITORY_MAVEN_CENTRAL = "https://repo1.maven.org/maven2/";
    private final PluginLogger logger;
    private final DependencyLoader dependencyLoader;

    public DependencyStartup(PluginLogger logger, DependencyLoader dependencyLoader) {
        this.logger = logger;
        this.dependencyLoader = dependencyLoader;
    }

    public void loadDependencies() throws IOException {
        logger.info("Resolving runtime dependencies..");
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "org.apache.httpcomponents", "httpclient", "4.5.13"
        );
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "com.googlecode.htmlcompressor", "htmlcompressor", "1.5.2"
        );
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "com.h2database", "h2", "1.4.199"
        );
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "mysql", "mysql-connector-java", "8.0.23"
        );
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "org.xerial", "sqlite-jdbc", "3.34.0"
        );
        logger.info("Loading runtime dependencies..");
        dependencyLoader.load();

        dependencyLoader.executeWithDependencyClassloaderContext(() -> {
            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                logger.error("Could not load SQLite driver");
            }
            try {
                Class.forName("org.mysql.jdbc.Driver");
            } catch (ClassNotFoundException e) {
                logger.error("Could not load MySQL driver");
            }
        });
    }

}
