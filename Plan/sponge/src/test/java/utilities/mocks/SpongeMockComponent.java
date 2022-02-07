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

import com.djrapitops.plan.DaggerPlanSpongeComponent;
import com.djrapitops.plan.PlanPlugin;
import com.djrapitops.plan.PlanSpongeComponent;
import com.djrapitops.plan.PlanSystem;
import com.djrapitops.plan.storage.database.SQLDB;
import org.mockito.Mockito;
import org.spongepowered.api.Game;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.text.Text;

import java.net.InetSocketAddress;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.doReturn;

/**
 * Test utility for creating a dagger PlanComponent using a mocked PlanSponge.
 *
 * @author AuroraLS3
 */
public class SpongeMockComponent {

    private final Path tempDir;

    private PlanPlugin planMock;
    private PlanSpongeComponent component;

    public SpongeMockComponent(Path tempDir) {
        this.tempDir = tempDir;
        SQLDB.setDownloadDriver(false);
    }

    public PlanPlugin getPlanMock() throws Exception {
        if (planMock == null) {
            planMock = PlanPluginMocker.setUp()
                    .withLogging()
                    .withDataFolder(tempDir.toFile())
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() throws Exception {
        if (component == null) {
            PlanPlugin planMock = getPlanMock();
            component = DaggerPlanSpongeComponent.builder()
                    .plan(planMock)
                    .abstractionLayer(new TestPlatformAbstractionLayer(planMock))
                    .game(mockGame())
                    .build();
        }
        return component.system();
    }

    private Game mockGame() {
        Game game = Mockito.mock(Game.class);

        Platform platform = mockPlatform();
        Server server = mockServer();
        doReturn(platform).when(game).getPlatform();
        doReturn(server).when(game).getServer();

        return game;
    }

    private Platform mockPlatform() {
        Platform platform = Mockito.mock(Platform.class);

        MinecraftVersion version = Mockito.mock(MinecraftVersion.class);
        doReturn("1.12").when(version).getName();
        doReturn(version).when(platform).getMinecraftVersion();

        return platform;
    }

    private Server mockServer() {
        Server server = Mockito.mock(Server.class);

        Text motd = Mockito.mock(Text.class);
        doReturn("Motd").when(motd).toPlain();
        Optional<InetSocketAddress> ip = Optional.of(new InetSocketAddress(25565));
        int maxPlayers = 20;
        List<Player> online = new ArrayList<>();

        doReturn(motd).when(server).getMotd();
        doReturn(ip).when(server).getBoundAddress();
        doReturn(maxPlayers).when(server).getMaxPlayers();
        doReturn(online).when(server).getOnlinePlayers();

        return server;
    }
}