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
import net.kyori.adventure.text.TextComponent;
import org.mockito.Mockito;
import org.spongepowered.api.Game;
import org.spongepowered.api.MinecraftVersion;
import org.spongepowered.api.Platform;
import org.spongepowered.api.Server;
import org.spongepowered.api.entity.living.player.server.ServerPlayer;

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

    public PlanPlugin getPlanMock() {
        if (planMock == null) {
            planMock = PlanPluginMocker.setUp()
                    .withLogging()
                    .withDataFolder(tempDir.toFile())
                    .getPlanMock();
        }
        return planMock;
    }

    public PlanSystem getPlanSystem() {
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
        doReturn(platform).when(game).platform();
        doReturn(server).when(game).server();

        return game;
    }

    private Platform mockPlatform() {
        Platform platform = Mockito.mock(Platform.class);

        MinecraftVersion version = Mockito.mock(MinecraftVersion.class);
        doReturn("1.12").when(version).name();
        doReturn(version).when(platform).minecraftVersion();

        return platform;
    }

    private Server mockServer() {
        Server server = Mockito.mock(Server.class);

        TextComponent motd = Mockito.mock(TextComponent.class);
        doReturn("Motd").when(motd).content();
        Optional<InetSocketAddress> ip = Optional.of(new InetSocketAddress(25565));
        int maxPlayers = 20;
        List<ServerPlayer> online = new ArrayList<>();

        doReturn(motd).when(server).motd();
        doReturn(ip).when(server).boundAddress();
        doReturn(maxPlayers).when(server).maxPlayers();
        doReturn(online).when(server).onlinePlayers();

        return server;
    }
}
