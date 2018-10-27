package utilities.mocks.objects;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.mockito.Mockito;
import utilities.TestConstants;

import static org.mockito.Mockito.when;

public class MockPlayers {
    public static Player mockPlayer() {
        Player p = Mockito.mock(Player.class);
        when(p.getGameMode()).thenReturn(GameMode.SURVIVAL);
        when(p.getUniqueId()).thenReturn(TestConstants.PLAYER_ONE_UUID);
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
        Player p = Mockito.mock(Player.class);
        when(p.getGameMode()).thenReturn(GameMode.SPECTATOR);
        when(p.getUniqueId()).thenReturn(TestConstants.PLAYER_TWO_UUID);
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

    private static World mockWorld() {
        World mockWorld = Mockito.mock(World.class);
        when(mockWorld.toString()).thenReturn("World");
        return mockWorld;
    }
}
