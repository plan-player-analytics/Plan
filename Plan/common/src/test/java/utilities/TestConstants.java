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

import java.util.UUID;

/**
 * Test Constants go here.
 *
 * @author AuroraLS3
 */
public class TestConstants {

    private TestConstants() {
        /* Static variable class */
    }

    public static final String SERVER_NAME = "ServerName";
    public static final String SERVER_TWO_NAME = "ServerName2";
    public static final UUID SERVER_UUID = UUID.fromString("e4ec2edd-e0ed-3c58-a87d-8a9021899479");
    public static final UUID SERVER_TWO_UUID = UUID.fromString("c4ec2edd-e0ed-3c58-a87d-8a9024791899");
    public static final UUID PLAYER_ONE_UUID = UUID.fromString("45b0dfdb-f71d-4cf3-8c21-27c9d4c651db");
    public static final UUID PLAYER_TWO_UUID = UUID.fromString("ec94a954-1fa1-445b-b09b-9b698519af80");
    public static final UUID PLAYER_THREE_UUID = UUID.randomUUID();

    public static final String PLAYER_ONE_NAME = "Test_Player_one";
    public static final String PLAYER_TWO_NAME = "Test_Player_two";
    public static final String PLAYER_THREE_NAME = RandomData.randomString(16);

    public static final String PLAYER_HOSTNAME = "play.example.com";

    public static final String WORLD_ONE_NAME = "World One";
    public static final Long REGISTER_TIME = RandomData.randomTime();

    public static final int SERVER_MAX_PLAYERS = 20;
    public static final int BUNGEE_MAX_PLAYERS = 100;

}
