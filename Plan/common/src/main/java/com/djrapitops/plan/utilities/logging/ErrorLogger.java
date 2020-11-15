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
package com.djrapitops.plan.utilities.logging;

import com.djrapitops.plan.exceptions.ExceptionWithContext;
import com.djrapitops.plugin.logging.L;
import com.djrapitops.plugin.logging.error.ErrorHandler;

public interface ErrorLogger extends ErrorHandler {
    default <T extends ExceptionWithContext> void log(L level, T throwable) {
        log(level, (Throwable) throwable, throwable.getContext().orElse(ErrorContext.builder().related("Missing Context").build()));
    }

    void log(L level, Throwable throwable, ErrorContext context);

    @Override
    @Deprecated
    default void log(L level, Class caughtBy, Throwable throwable) {
        log(level, throwable, ErrorContext.builder()
                .related("Caught by " + caughtBy.getName())
                .build());
    }
}
