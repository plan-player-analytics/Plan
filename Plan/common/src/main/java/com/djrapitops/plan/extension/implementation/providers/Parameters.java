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
package com.djrapitops.plan.extension.implementation.providers;

import com.djrapitops.plan.extension.DataExtension;
import com.djrapitops.plan.extension.Group;
import com.djrapitops.plan.extension.implementation.MethodType;
import com.djrapitops.plan.identification.ServerUUID;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public interface Parameters {
    static Parameters player(ServerUUID serverUUID, UUID playerUUID, String playerName) {
        return new PlayerParameters(serverUUID, playerUUID, playerName);
    }

    static Parameters server(ServerUUID serverUUID) {
        return new ServerParameters(serverUUID);
    }

    static Parameters group(ServerUUID serverUUID, String groupName) {
        return new GroupParameters(serverUUID, groupName);
    }

    Object usingOn(DataExtension extension, Method method) throws InvocationTargetException, IllegalAccessException;

    MethodType getMethodType();

    ServerUUID getServerUUID();

    default UUID getPlayerUUID() {
        return null;
    }

    record ServerParameters(ServerUUID serverUUID) implements Parameters {
        @Override
        public ServerUUID getServerUUID() {
            return serverUUID;
        }

        @Override
        public Object usingOn(DataExtension extension, Method method) throws InvocationTargetException, IllegalAccessException {
            return method.invoke(extension);
        }

        @Override
        public MethodType getMethodType() {
            return MethodType.SERVER;
        }
    }

    record PlayerParameters(ServerUUID serverUUID, UUID playerUUID, String playerName) implements Parameters {
        @Override
        public ServerUUID getServerUUID() {
            return serverUUID;
        }

        @Override
        public UUID getPlayerUUID() {
            return playerUUID;
        }

        public String getPlayerName() {
            return playerName;
        }

        @Override
        public Object usingOn(DataExtension extension, Method method) throws InvocationTargetException, IllegalAccessException {
            Class<?> parameterType = method.getParameterTypes()[0];
            if (UUID.class.equals(parameterType)) {
                return method.invoke(extension, playerUUID);
            } else {
                return method.invoke(extension, playerName);
            }
        }

        @Override
        public MethodType getMethodType() {
            return MethodType.PLAYER;
        }
    }

    record GroupParameters(ServerUUID serverUUID, String groupName) implements Parameters {
        @Override
        public ServerUUID getServerUUID() {
            return serverUUID;
        }

        @Override
        public Object usingOn(DataExtension extension, Method method) throws InvocationTargetException, IllegalAccessException {
            Group group = this::getGroupName;
            return method.invoke(extension, group);
        }

        public String getGroupName() {
            return groupName;
        }

        @Override
        public MethodType getMethodType() {
            return MethodType.GROUP;
        }
    }
}
