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

import com.djrapitops.plan.utilities.java.Lists;
import net.playeranalytics.plugin.dependencies.DependencyLoader;
import net.playeranalytics.plugin.me.lucko.jarrelocator.Relocation;
import net.playeranalytics.plugin.server.PluginLogger;

import java.io.IOException;
import java.util.Collections;

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
                "com.h2database", "h2", "1.4.199",
                Lists.builder(Relocation.class)
                        .add(new Relocation(
                                new String(new char[]{'o', 'r', 'g', '.', 'h', '2'}),
                                "plan.org.h2"
                        )).build()
        );
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "mysql", "mysql-connector-java", "8.0.23",
                Lists.builder(Relocation.class)
                        .add(new Relocation(
                                new String(new char[]{'c', 'o', 'm', '.', 'm', 'y', 's', 'q', 'l'}),
                                "plan.com.mysql"
                        )).build()
        );
        dependencyLoader.addDependency(REPOSITORY_MAVEN_CENTRAL,
                "org.xerial", "sqlite-jdbc", "3.34.0",
                Collections.emptyList()
//                Lists.builder(Relocation.class)
//                        .add(new Relocation(
//                                new String(new char[]{'o', 'r', 'g', '.', 's', 'q', 'l', 'i', 't', 'e'}),
//                                "plan.org.sqlite"
//                        )).build()
        );
        logger.info("Loading runtime dependencies..");
        dependencyLoader.load();

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            logger.error("Could not load SQLite driver");
        }
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            logger.error("Could not load MySQL driver");
        }
    }

}
