package test.java.utils;

import java.util.UUID;
import org.bukkit.World;
import org.mockito.Mockito;
import org.bukkit.entity.Player;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 * @author Rsl1122
 */
public class MockUtils {

    public static World mockWorld() {
        World mockWorld = Mockito.mock(World.class);
        Mockito.doReturn("World").when(mockWorld).toString();
        return mockWorld;
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

    public static Player mockBrokenPlayer() {
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
        return p;
    }
    
    public static CommandSender mockConsoleSender() {
        CommandSender s = PowerMockito.mock(CommandSender.class);
        return s;
    }
}
