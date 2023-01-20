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
package net.playeranalytics.plan.gathering;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

public class FabricPlayerPositionTracker {
    private static final ReentrantLock WRITE_LOCK = new ReentrantLock();
    private static final Map<UUID, double[]> POSITIONS = new HashMap<>();

    private static final double[] EMPTY_POSITION = new double[5];

    private FabricPlayerPositionTracker() {
        // Static method class
    }

    public static void removePlayer(UUID playerUUID) {
        try {
            WRITE_LOCK.lock();
            POSITIONS.remove(playerUUID);
        } finally {
            WRITE_LOCK.unlock();
        }
    }

    public static double[] getPosition(UUID playerUUID) {
        return POSITIONS.getOrDefault(playerUUID, EMPTY_POSITION);
    }

    public static boolean moved(UUID playerUUID, double x, double y, double z, float yaw, float pitch) {
        double[] previous = POSITIONS.get(playerUUID);
        if (isDifferent(previous, x, y, z, yaw, pitch)) {
            writeNewPosition(playerUUID, x, y, z, yaw, pitch, previous);
            return true;
        }
        return false;
    }

    private static void writeNewPosition(UUID playerUUID, double x, double y, double z, float yaw, float pitch, double[] previous) {
        try {
            WRITE_LOCK.lock();
            if (previous == null) {
                previous = new double[5];
                POSITIONS.put(playerUUID, previous);
            }
            previous[0] = x;
            previous[1] = y;
            previous[2] = z;
            previous[3] = yaw;
            previous[4] = pitch;

        } finally {
            WRITE_LOCK.unlock();
        }
    }

    private static boolean isDifferent(double[] previous, double x, double y, double z, float yaw, float pitch) {
        return previous == null
                || Double.compare(previous[0], x) != 0
                || Double.compare(previous[1], y) != 0
                || Double.compare(previous[2], z) != 0
                || Double.compare(previous[3], yaw) != 0
                || Double.compare(previous[4], pitch) != 0;
    }
}
