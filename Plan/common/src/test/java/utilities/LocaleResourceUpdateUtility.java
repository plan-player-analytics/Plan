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
package utilities;

import com.djrapitops.plan.settings.config.PlanConfig;
import com.djrapitops.plan.settings.config.paths.PluginSettings;
import com.djrapitops.plan.settings.config.paths.WebserverSettings;
import utilities.dagger.PlanPluginComponent;
import utilities.mocks.PluginMockComponent;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

/**
 * This utility allows updating all locale_{code}.yml files with newest language.
 *
 * @author AuroraLS3
 */
public class LocaleResourceUpdateUtility {

    public static void main(String[] args) throws Exception {
        PluginMockComponent mockComponent = new PluginMockComponent(Files.createTempDirectory("temp-plan-"));
        PlanPluginComponent component = mockComponent.getComponent();

        PlanConfig config = component.system().getConfigSystem().getConfig();
        config.set(WebserverSettings.DISABLED, true);
        config.set(PluginSettings.LOCALE, "write-all");

        try {
            component.system().enable();

            Path localeResourceDirectory = new File("").toPath().resolve("common/src/main/resources/assets/plan/locale");

            for (File file : Objects.requireNonNull(component.system().getPlanFiles().getDataFolder().listFiles())) {
                if (file.getName().contains("locale_") && file.getName().endsWith(".yml")) {
                    Path overriding = localeResourceDirectory.resolve(file.getName());
                    Files.move(file.toPath(), overriding, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        } finally {
            component.system().disable();
        }
    }

}
