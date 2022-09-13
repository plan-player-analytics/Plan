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
package utilities.mocks.objects;

import com.djrapitops.plan.gathering.domain.PlatformPlayerData;

import java.net.InetAddress;
import java.util.Optional;
import java.util.UUID;

public class TestPlayerData implements PlatformPlayerData {

    private final UUID uuid;
    private final String name;
    private String displayName;
    private Boolean banned;
    private Boolean operator;
    private String joinAddress;
    private String currentWorld;
    private String currentGameMode;
    private Long registerDate;
    private InetAddress ip;

    public TestPlayerData(UUID uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public Optional<String> getDisplayName() {
        return Optional.ofNullable(displayName);
    }

    public TestPlayerData setDisplayName(String displayName) {
        this.displayName = displayName;
        return this;
    }

    @Override
    public Optional<Boolean> isBanned() {
        return Optional.ofNullable(banned);
    }

    @Override
    public Optional<Boolean> isOperator() {
        return Optional.ofNullable(operator);
    }

    @Override
    public Optional<String> getJoinAddress() {
        return Optional.ofNullable(joinAddress);
    }

    public TestPlayerData setJoinAddress(String joinAddress) {
        this.joinAddress = joinAddress;
        return this;
    }

    @Override
    public Optional<String> getCurrentWorld() {
        return Optional.ofNullable(currentWorld);
    }

    public TestPlayerData setCurrentWorld(String currentWorld) {
        this.currentWorld = currentWorld;
        return this;
    }

    @Override
    public Optional<String> getCurrentGameMode() {
        return Optional.ofNullable(currentGameMode);
    }

    public TestPlayerData setCurrentGameMode(String currentGameMode) {
        this.currentGameMode = currentGameMode;
        return this;
    }

    @Override
    public Optional<Long> getRegisterDate() {
        return Optional.ofNullable(registerDate);
    }

    public TestPlayerData setRegisterDate(Long registerDate) {
        this.registerDate = registerDate;
        return this;
    }

    @Override
    public Optional<InetAddress> getIPAddress() {
        return Optional.ofNullable(ip);
    }

    public TestPlayerData setBanned(Boolean banned) {
        this.banned = banned;
        return this;
    }

    public TestPlayerData setOperator(Boolean operator) {
        this.operator = operator;
        return this;
    }

    public TestPlayerData setIp(InetAddress ip) {
        this.ip = ip;
        return this;
    }
}
