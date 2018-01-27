package utilities;

import com.sun.net.httpserver.*;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.mockito.Mockito;
import org.powermock.api.mockito.PowerMockito;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.zip.GZIPInputStream;

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
        return PowerMockito.mock(CommandSender.class);
    }

    public static HttpServer mockHTTPServer() {
        HttpServer httpServer = PowerMockito.mock(HttpServer.class);
        when(httpServer.getAddress()).thenReturn(new InetSocketAddress(80));
        when(httpServer.getExecutor()).thenReturn(command -> System.out.println("HTTP Server command received"));
        return httpServer;
    }

    public static HttpExchange getHttpExchange(String requestMethod, String requestURI, String body, Map<String, List<String>> responseHeaders) {
        return new HttpExchange() {
            private ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            @Override
            public Headers getRequestHeaders() {
                Headers headers = new Headers();
                headers.put("Authorization", new ArrayList<>());
                return headers;
            }

            @Override
            public Headers getResponseHeaders() {
                Headers headers = new Headers();
                headers.putAll(responseHeaders);
                return headers;
            }

            @Override
            public URI getRequestURI() {
                try {
                    return new URI(requestURI);
                } catch (URISyntaxException e) {
                    return null;
                }
            }

            @Override
            public String getRequestMethod() {
                return requestMethod;
            }

            @Override
            public HttpContext getHttpContext() {
                return null;
            }

            @Override
            public void close() {

            }

            @Override
            public InputStream getRequestBody() {
                return new ByteArrayInputStream(body.getBytes(Charset.forName("UTF-8")));
            }

            @Override
            public OutputStream getResponseBody() {
                return outputStream;
            }

            @Override
            public InetSocketAddress getRemoteAddress() {
                return null;
            }

            @Override
            public InetSocketAddress getLocalAddress() {
                return null;
            }

            @Override
            public String getProtocol() {
                return null;
            }

            @Override
            public Object getAttribute(String name) {
                return null;
            }

            @Override
            public void sendResponseHeaders(int i, long l) {

            }

            @Override
            public int getResponseCode() {
                return 0;
            }

            @Override
            public void setAttribute(String s, Object o) {

            }

            @Override
            public void setStreams(InputStream inputStream, OutputStream outputStream) {

            }

            @Override
            public HttpPrincipal getPrincipal() {
                return null;
            }
        };
    }

    public static String getResponseStream(HttpExchange requestExchange) throws IOException {
        InputStream in = new GZIPInputStream(
                new ByteArrayInputStream((
                        (ByteArrayOutputStream) requestExchange.getResponseBody()
                ).toByteArray())
        );
        try (Scanner scanner = new Scanner(in)) {
            StringBuilder s = new StringBuilder();
            while (scanner.hasNextLine()) {
                s.append(scanner.nextLine()).append("\n");
            }
            return s.toString();
        }
    }
}
