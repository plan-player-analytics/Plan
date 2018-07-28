package utilities;

import com.djrapitops.plan.api.exceptions.database.DBInitException;
import com.djrapitops.plan.data.container.Session;
import com.djrapitops.plan.data.time.GMTimes;
import com.djrapitops.plan.data.time.WorldTimes;
import com.djrapitops.plan.system.database.databases.sql.SQLDB;
import com.djrapitops.plan.system.database.databases.sql.SQLiteDB;
import com.djrapitops.plan.system.info.server.Server;
import com.djrapitops.plugin.api.TimeAmount;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

public class TestDatabaseCreator {

    private static final List<UUID> SERVER_UUIDS = Arrays.stream(new String[]{
            "116aa03d-b9fd-4947-8983-f2ec3aa780e5",
            "a31b0867-aac0-4faf-b7b6-4eee6b66a51d",
            "b80b2e89-6274-4921-9546-e5d8185e0f4e",
            "459458ec-25da-4a46-b319-3ee11121f6a1",
            "14043fad-b743-4069-b260-97626631465d",
            "56520211-9b5c-460b-99ec-bdd77747d3c3",
            "facd2eb1-4eba-45e9-ab05-0947dca82bdd",
            "20988493-1da0-4a53-80b5-60d4a8936b50",
            "f9cc0853-73c1-44d1-8989-19c93977302d",
            "840b0c0e-a65c-4269-8d5c-d3e1de349557"
    }).map(UUID::fromString).collect(Collectors.toList());
    private static final String[] gms = GMTimes.getGMKeyArray();
    private final SQLDB db;
    private final Random r;
    private Map<UUID, List<String>> worlds;

    public TestDatabaseCreator() throws DBInitException {
        File testDB = new File("src/test/resources/testDB.db".replace("/", File.separator));

        boolean oldDB = testDB.exists();

        db = new SQLiteDB(testDB);
        db.init();

        r = new Random();

        if (oldDB) {
            return;
        }

        fillDatabase();
    }

    private void fillDatabase() {
        addServers();
        addWorlds();
        addUsers();
    }

    private void addWorlds() {
        worlds = new HashMap<>();

        for (UUID serverUuid : SERVER_UUIDS) {
            for (int i = 0; i < r.nextInt(10); i++) {
                List<String> worldNames = worlds.getOrDefault(serverUuid, new ArrayList<>());
                worldNames.add(RandomData.randomString(50));
                worlds.put(serverUuid, worldNames);
            }
        }

        for (Map.Entry<UUID, List<String>> entry : worlds.entrySet()) {
            db.getWorldTable().saveWorlds(entry.getValue(), entry.getKey());
        }
    }

    private void addUsers() {
        for (int i = 0; i < 100000; i++) {
            UUID uuid = UUID.randomUUID();
            long registered = Math.abs(r.nextLong());
            String name = uuid.toString().split("-", 2)[0];
            db.save().registerNewUser(uuid, registered, name);
        }
    }

    private void addSession(UUID uuid, long date) {
        UUID serverUUID = SERVER_UUIDS.get(r.nextInt(SERVER_UUIDS.size()));
        List<String> worldNames = worlds.get(serverUUID);
        String world = worldNames.get(r.nextInt(worldNames.size()));
        String gm = gms[r.nextInt(gms.length)];

        long end = date + (long) r.nextInt((int) TimeAmount.DAY.ms());

        Session session = new Session(-1, uuid, serverUUID,
                date, end,
                r.nextInt(100), // mobs
                r.nextInt(50), // deaths
                r.nextInt((int) (end - date)) // afk
        );

        WorldTimes worldTimes = new WorldTimes(new HashMap<>());

    }

    private void addServers() {
        for (UUID serverUuid : SERVER_UUIDS) {
            Server server = new Server(
                    -1,
                    serverUuid,
                    serverUuid.toString().split("-", 2)[0],
                    "address",
                    100
            );
            db.getServerTable().saveCurrentServerInfo(server);
        }
    }

}
