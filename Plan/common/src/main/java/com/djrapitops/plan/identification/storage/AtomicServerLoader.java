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
package com.djrapitops.plan.identification.storage;

import com.djrapitops.plan.identification.Server;
import com.djrapitops.plan.identification.ServerUUID;

import java.util.Optional;
import java.util.concurrent.locks.ReentrantLock;

public class AtomicServerLoader implements ServerLoader {

    private final ReentrantLock reentrantLock;
    private final ServerLoader original;

    public AtomicServerLoader(ServerLoader original) {
        this.original = original;
        this.reentrantLock = new ReentrantLock();
    }

    @Override
    public Optional<Server> load(ServerUUID serverUUID) {
        try {
            reentrantLock.lock();
            return original.load(serverUUID);
        } finally {
            reentrantLock.unlock();
        }
    }

    @Override
    public void save(Server information) {
        try {
            reentrantLock.lock();
            original.save(information);
        } finally {
            reentrantLock.unlock();
        }
    }
}
