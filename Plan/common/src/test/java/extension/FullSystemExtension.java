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
import com.djrapitops.plan.delivery.DeliveryUtilities;
import com.djrapitops.plan.delivery.export.Exporter;
import com.djrapitops.plan.delivery.formatting.Formatters;
import com.djrapitops.plan.delivery.webserver.Addresses;
import com.djrapitops.plan.identification.ServerUUID;
import com.djrapitops.plan.settings.ConfigSystem;
import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import com.djrapitops.plan.settings.locale.LocaleSystem;
import com.djrapitops.plan.storage.database.Database;
import com.djrapitops.plan.storage.file.PlanFiles;
import com.djrapitops.plan.storage.file.PublicHtmlFiles;
import com.djrapitops.plan.utilities.java.Maps;
import javassist.tools.web.Webserver;
import org.junit.jupiter.api.extension.*;
import utilities.RandomData;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.function.Supplier;

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

    private final Map<Class, Supplier> parameterResolvers;

    public FullSystemExtension() {
        // You can't use method references here because planSystem is null when this method is initialized.

        this.parameterResolvers = Maps.builder(Class.class, Supplier.class)
                .put(PlanSystem.class, () -> planSystem)
                .put(PlanFiles.class, () -> planSystem.getPlanFiles())
                .put(PlanConfig.class, () -> planSystem.getConfigSystem().getConfig())
                .put(ConfigSystem.class, () -> planSystem.getConfigSystem())
                .put(ServerUUID.class, () -> planSystem.getServerInfo().getServerUUID())
                .put(PlanPluginComponent.class, () -> {
                    try {
                        return component.getComponent();
                    } catch (Exception e) {
                        throw new ParameterResolutionException("Error getting " + PlanPluginComponent.class, e);
                    }
                })
                .put(PlanCommand.class, () -> {
                    try {
                        return component.getComponent().planCommand();
                    } catch (Exception e) {
                        throw new ParameterResolutionException("Error getting " + PlanCommand.class, e);
                    }
                })
                .put(Database.class, () -> planSystem.getDatabaseSystem().getDatabase())
                .put(DeliveryUtilities.class, () -> planSystem.getDeliveryUtilities())
                .put(Formatters.class, () -> planSystem.getDeliveryUtilities().getFormatters())
                .put(LocaleSystem.class, () -> planSystem.getLocaleSystem())
                .put(Addresses.class, () -> planSystem.getDeliveryUtilities().getAddresses())
                .put(PublicHtmlFiles.class, () -> planSystem.getDeliveryUtilities().getPublicHtmlFiles())
                .put(Webserver.class, () -> planSystem.getWebServerSystem().getWebServer())
                .put(Exporter.class, () -> planSystem.getExportSystem().getExporter())
                .build();
    }

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
        if (tempDir != null) deleteDirectory(tempDir);
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
        return parameterResolvers.containsKey(type);
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) {
        Class<?> type = parameterContext.getParameter().getType();
        Supplier<?> supplier = parameterResolvers.get(type);
        if (supplier != null) return supplier.get();
        throw new ParameterResolutionException("Unsupported parameter type " + type.getName());
    }
}
