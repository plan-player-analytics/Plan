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
package com.djrapitops.plan.commands.use;

import java.util.Optional;
import java.util.UUID;

public interface CMDSender {

    MessageBuilder buildMessage();

    Optional<String> getPlayerName();

    boolean hasPermission(String permission);

    default boolean hasAllPermissionsFor(Subcommand subcommand) {
        return !isMissingPermissionsFor(subcommand);
    }

    default boolean isMissingPermissionsFor(Subcommand subcommand) {
        for (String permission : subcommand.getRequiredPermissions()) {
            if (!hasPermission(permission)) {
                return true;
            }
        }
        return false;
    }

    default boolean isPlayer() {
        return getPlayerName().isPresent();
    }

    default boolean supportsChatEvents() {
        return false;
    }

    Optional<UUID> getUUID();

    void send(String message);

    default void send(String... messages) {
        for (String message : messages) {
            send(message);
        }
    }

    ChatFormatter getFormatter();

}
