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
package com.djrapitops.plan.utilities;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Supplier;

/**
 * Allows using ReentrantLock with lambdas.
 *
 * @author AuroraLS3
 */
public class ReentrantLockHelper {

    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final ReentrantReadWriteLock.ReadLock readLock;

    public ReentrantLockHelper() {
        var lock = new ReentrantReadWriteLock();
        writeLock = lock.writeLock();
        readLock = lock.readLock();
    }

    public void performWriteOperation(Runnable runnable) {
        boolean interrupted = false;
        try {
            writeLock.lockInterruptibly();
            runnable.run();
        } catch (InterruptedException e) {
            interrupted = true;
            Thread.currentThread().interrupt();
        } finally {
            if (!interrupted) {
                writeLock.unlock();
            }
        }
    }

    public <T> T performReadOperation(Supplier<T> supplier) {
        boolean interrupted = false;
        try {
            readLock.lockInterruptibly();
            return supplier.get();
        } catch (InterruptedException e) {
            interrupted = true;
            Thread.currentThread().interrupt();
            throw new IllegalStateException("Thread continued after being interrupted, this error should never happen.", e);
        } finally {
            if (!interrupted) {
                readLock.unlock();
            }
        }
    }
}
