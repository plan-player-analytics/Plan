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
package com.djrapitops.plan.extension;

/**
 * Exception to throw inside DataExtension if a method is not ready to be called (Data is not available etc).
 * <p>
 * This Exception will not cause Plan to "yell" about the exception.
 * <p>
 * Requires Capability#DATA_EXTENSION_NOT_READY_EXCEPTION.
 *
 * @author AuroraLS3
 */
public class NotReadyException extends IllegalStateException {

    /**
     * Construct the exception.
     * <p>
     * The Exception is not logged (Fails silently) so no message is available.
     */
    public NotReadyException() {
        // Constructor is present for javadoc comment.
    }
}