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

import com.djrapitops.plan.PlanVelocity;
import com.djrapitops.plan.commands.use.ColorScheme;
import com.velocitypowered.api.proxy.ProxyServer;
import org.mockito.Mockito;

import java.io.File;
import java.net.InetSocketAddress;
import java.util.ArrayList;

import static org.mockito.Mockito.doReturn;

/**
 * Mocking Utility for Velocity version of Plan (PlanVelocity).
 *
 * @author AuroraLS3
 */
public class PlanVelocityMocker {

    private PlanVelocity planMock;

    private PlanVelocityMocker() {
    }

    public static PlanVelocityMocker setUp() {
        return new PlanVelocityMocker().mockPlugin();
    }

    private PlanVelocityMocker mockPlugin() {
        planMock = Mockito.mock(PlanVelocity.class);

        doReturn(new ColorScheme("§1", "§2", "§3")).when(planMock).getColorScheme();

        return this;
    }

    public PlanVelocityMocker withDataFolder(File tempFolder) {
        return this;
    }

    public PlanVelocityMocker withProxy() {
        ProxyServer server = Mockito.mock(ProxyServer.class);

        InetSocketAddress ip = new InetSocketAddress(25565);

        doReturn(new ArrayList<>()).when(server).getAllServers();
        doReturn(ip).when(server).getBoundAddress();

        doReturn(server).when(planMock).getProxy();
        return this;
    }

    public PlanVelocity getPlanMock() {
        return planMock;
    }
}
