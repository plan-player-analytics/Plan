package test.java.utils;

import com.djrapitops.plugin.utilities.player.Fetch;
import com.djrapitops.plugin.utilities.player.IPlayer;
import com.sun.net.httpserver.HttpServer;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import static org.powermock.api.mockito.PowerMockito.when;

/**
 * @author Rsl1122
 */
public class MockUtils {

    public static World mockWorld() {
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.toString()).thenReturn("World");
        return mockWorld;
    }

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

    public static UUID getPlayerUUID() {
        return UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db");
    }

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

    public static UUID getPlayer2UUID() {
        return UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80");
    }

    public static Set<UUID> getUUIDs() {
        Set<UUID> uuids = new HashSet<>();
        uuids.add(getPlayerUUID());
        uuids.add(getPlayer2UUID());
        return uuids;
    }

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
