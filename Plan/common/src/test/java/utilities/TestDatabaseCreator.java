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
package utilities;

import com.djrapitops.plan.gathering.domain.GMTimes;
import com.djrapitops.plan.storage.database.SQLDB;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.UUID;
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
    private final SQLDB db = null; // TODO
    private final Random r;

    public TestDatabaseCreator() {
        File testDB = new File("src/test/resources/testDB.db".replace("/", File.separator));

        boolean oldDB = testDB.exists();

        r = new Random();

        if (oldDB) {
            return;
        }

        // fillDatabase();
    }
}
