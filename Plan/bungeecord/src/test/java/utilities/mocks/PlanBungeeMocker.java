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
package utilities.mocks;

import com.djrapitops.plan.PlanBungee;
import com.djrapitops.plan.commands.use.ColorScheme;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.ProxyConfig;
import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.plugin.PluginDescription;
import net.md_5.bungee.api.plugin.PluginManager;
import org.mockito.Mockito;
import utilities.TestConstants;
import utilities.mocks.objects.TestLogger;

import java.io.File;
import java.util.HashSet;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

/**
 * Mocking Utility for Bungee version of Plan (PlanBungee).
 *
 * @author AuroraLS3
 */
public class PlanBungeeMocker extends Mocker {

    private PlanBungee planMock;

    private PlanBungeeMocker() {
    }

    public static PlanBungeeMocker setUp() {
        return new PlanBungeeMocker().mockPlugin();
    }

    private PlanBungeeMocker mockPlugin() {
        planMock = Mockito.mock(PlanBungee.class);
        super.planMock = planMock;

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();

        TestLogger testLogger = new TestLogger();

        doReturn(testLogger).when(planMock).getLogger();

        return this;
    }

    PlanBungeeMocker withDataFolder(File tempFolder) {
        when(planMock.getDataFolder()).thenReturn(tempFolder);
        return this;
    }

    PlanBungeeMocker withResourceFetchingFromJar() {
        return this;
    }

    @SuppressWarnings("deprecation")
    PlanBungeeMocker withProxy() {
        ProxyServer proxyMock = Mockito.mock(ProxyServer.class);
        doReturn("1.12.2").when(proxyMock).getVersion();

        CommandSender console = Mockito.mock(CommandSender.class);
        doReturn(console).when(proxyMock).getConsole();

        ProxyConfig proxyConfig = Mockito.mock(ProxyConfig.class);
        doReturn(TestConstants.BUNGEE_MAX_PLAYERS).when(proxyConfig).getPlayerLimit();
        doReturn(proxyConfig).when(proxyMock).getConfig();

        PluginManager pm = Mockito.mock(PluginManager.class);
        doReturn(pm).when(proxyMock).getPluginManager();

        doReturn(proxyMock).when(planMock).getProxy();
        return this;
    }

    PlanBungeeMocker withPluginDescription() {
        File pluginYml = getFile("/bungee.yml");
        HashSet<String> empty = new HashSet<>();
        PluginDescription pluginDescription = new PluginDescription("Plan", "", "9.9.9", "AuroraLS3", empty, empty, pluginYml, "");
        when(planMock.getDescription()).thenReturn(pluginDescription);
        return this;
    }

    PlanBungee getPlanMock() {
        return planMock;
    }
}
