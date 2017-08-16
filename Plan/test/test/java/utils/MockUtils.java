package test.java.utils;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IPlayer;
import com.sun.net.httpserver.HttpServer;
import main.java.com.djrapitops.plan.data.KillData;
import main.java.com.djrapitops.plan.data.SessionData;
import main.java.com.djrapitops.plan.data.UserData;
import main.java.com.djrapitops.plan.data.time.GMTimes;
import main.java.com.djrapitops.plan.utilities.NewPlayerCreator;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
public class MockUtils {

    /**
     * @return
     */
    public static World mockWorld() {
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.toString()).thenReturn("World");
        return mockWorld;
    }

    public static UserData mockUser() {
        return NewPlayerCreator.createNewPlayer(mockIPlayer());
    }

    public static UserData mockUserWithMoreData() {
        UserData mock = mockUser();
        try {
            mock.addIpAddress(InetAddress.getByName("247.183.163.155"));
        } catch (UnknownHostException ignored) {
            /* Ignored */
        }
        mock.addNickname("MoreNicks");
        mock.addPlayerKill(new KillData(getPlayer2UUID(), 1, "WEP", 126873643232L));
        mock.addSession(new SessionData(12345L, 23456L));
        GMTimes gmTimes = mock.getGmTimes();
        gmTimes.setAllGMTimes(1234, 2345, 4356, 4767);
        gmTimes.setState("ADVENTURE");
        mock.setDeaths(5);
        mock.setTimesKicked(5);
        mock.setPlayTime(34438926);
        mock.setGeolocation("Finland");
        mock.setLoginTimes(5);
        mock.setMobKills(5);
        return mock;
    }

    public static UserData mockUser2() {
        return NewPlayerCreator.createNewPlayer(mockIPlayer2());
    }

    /**
     * @return
     */
    public static IPlayer mockIPlayer() {
        return Fetch.wrapBukkit(mockPlayer());
    }

    public static Player mockPlayer() {
        Player p = PowerMockito.mock(Player.class);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(p.getUniqueId()).thenReturn(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"));
        when(p.getFirstPlayed()).thenReturn(1234567L);
        World mockWorld = mockWorld();
        when(p.getLocation()).thenReturn(new Location(mockWorld, 0, 0, 0));
        when(p.isOp()).thenReturn(true);
        when(p.isBanned()).thenReturn(true);
        when(p.isOnline()).thenReturn(true);
        when(p.getName()).thenReturn("TestName");
        when(p.hasPermission("plan.inspect.other")).thenReturn(true);
        return p;
    }

    /**
     * @return
     */
    public static UUID getPlayerUUID() {
        return UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db");
    }

    /**
     * @return
     */
    public static IPlayer mockIPlayer2() {
        return Fetch.wrapBukkit(mockPlayer2());
    }

    public static Player mockPlayer2() {
        Player p = PowerMockito.mock(Player.class);
        when(p.getGameMode()).thenReturn(GameMode.SPECTATOR);
        when(p.getUniqueId()).thenReturn(UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80"));
        when(p.getFirstPlayed()).thenReturn(3423434L);
        World mockWorld = mockWorld();
        when(p.getLocation()).thenReturn(new Location(mockWorld, 1, 0, 1));
        when(p.isOp()).thenReturn(false);
        when(p.isBanned()).thenReturn(false);
        when(p.isOnline()).thenReturn(false);
        when(p.hasPermission("plan.inspect.other")).thenReturn(false);
        when(p.getName()).thenReturn("TestName2");
        return p;
    }

    /**
     * @return
     */
    public static UUID getPlayer2UUID() {
        return UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80");
    }

    public static Set<UUID> getUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(getPlayerUUID());
        uuids.add(getPlayer2UUID());
        return uuids;
    }

    /**
     * @return
     */
    public static IPlayer mockBrokenPlayer() {
        Player p = PowerMockito.mock(Player.class);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(p.getUniqueId()).thenReturn(UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db"));
        when(p.getFirstPlayed()).thenReturn(1234567L);
        World mockWorld = mockWorld();
        when(p.getLocation()).thenReturn(new Location(mockWorld, 0, 0, 0));
        when(p.isOp()).thenReturn(true);
        when(p.isBanned()).thenThrow(Exception.class);
        when(p.isOnline()).thenReturn(true);
        when(p.getName()).thenReturn("TestName");
        return Fetch.wrapBukkit(p);
    }

    /**
     * @return
     */
    public static CommandSender mockConsoleSender() {
        return PowerMockito.mock(CommandSender.class);
    }

    public static HttpServer mockHTTPServer() {
        HttpServer httpServer = PowerMockito.mock(HttpServer.class);
        when(httpServer.getAddress()).thenReturn(new InetSocketAddress(80));
        when(httpServer.getExecutor()).thenReturn(command -> System.out.println("HTTP Server command received"));
        return httpServer;
    }
}
