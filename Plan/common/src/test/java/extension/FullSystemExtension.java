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
package extension;

import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.commands.PlanCommand;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
import org.junit.jupiter.api.extension.*;
import utilities.RandomData;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * JUnit 5 extension to construct a full PlanSystem for a test.
 *
 * @author AuroraLS3
 */
public class FullSystemExtension implements ParameterResolver, BeforeAllCallback, AfterAllCallback {

    private static final int TEST_PORT_NUMBER = RandomData.randomInt(9005, 9500);
    public PluginMockComponent component;
    private Path tempDir;
    private PlanSystem planSystem;

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        tempDir = Files.createTempDirectory("plan-fullsystem-test");
        component = new PluginMockComponent(tempDir);
        planSystem = component.getPlanSystem();
        planSystem.getConfigSystem().getConfig()
                .set(WebserverSettings.PORT, TEST_PORT_NUMBER);
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        deleteDirectory(tempDir);
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.list(directory)
                .forEach(file -> {
                    try {
                        if (Files.isDirectory(file)) {
                            deleteDirectory(file);
                        } else {
                            Files.delete(file);
                        }
                    } catch (IOException e) {
                        throw new UncheckedIOException(e);
                    }
                });
        Files.delete(directory);
    }

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        return PlanSystem.class.equals(type) ||
                PlanFiles.class.equals(type) ||
                PlanConfig.class.equals(type) ||
                ServerUUID.class.equals(type) ||
                PlanPluginComponent.class.equals(type) ||
                PlanCommand.class.equals(type) ||
                Database.class.equals(type) ||
                Exporter.class.equals(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        if (PlanSystem.class.equals(type)) {
            return planSystem;
        }
        if (PlanFiles.class.equals(type)) {
            return planSystem.getPlanFiles();
        }
        if (PlanConfig.class.equals(type)) {
            return planSystem.getConfigSystem().getConfig();
        }
        if (ServerUUID.class.equals(type)) {
            return planSystem.getServerInfo().getServerUUID();
        }
        if (PlanPluginComponent.class.equals(type)) {
            try {
                return component.getComponent();
            } catch (Exception e) {
                throw new ParameterResolutionException("Error getting " + type.getName(), e);
            }
        }
        if (PlanCommand.class.equals(type)) {
            try {
                return component.getComponent().planCommand();
            } catch (Exception e) {
                throw new ParameterResolutionException("Error getting " + type.getName(), e);
            }
        }
        if (Database.class.equals(type)) {
            return planSystem.getDatabaseSystem().getDatabase();
        }
        if (Exporter.class.equals(type)) {
            return planSystem.getExportSystem().getExporter();
        }
        throw new ParameterResolutionException("Unsupported parameter type " + type.getName());
    }
}
