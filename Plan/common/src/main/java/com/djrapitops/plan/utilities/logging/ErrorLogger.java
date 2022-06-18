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

public interface ErrorLogger {
    default <T extends ExceptionWithContext> void critical(T throwable) {
        critical((Throwable) throwable, throwable.getContext().orElseGet(this::createMissingContext));
    }

    void critical(Throwable throwable, ErrorContext context);

    default <T extends ExceptionWithContext> void error(T throwable) {
        error((Throwable) throwable, throwable.getContext().orElseGet(this::createMissingContext));
    }

    void error(Throwable throwable, ErrorContext context);

    default <T extends ExceptionWithContext> void warn(T throwable) {
        warn((Throwable) throwable, throwable.getContext().orElseGet(this::createMissingContext));
    }

    void warn(Throwable throwable, ErrorContext context);

    default ErrorContext createMissingContext() {
        return ErrorContext.builder().related("Missing Context").build();
    }
}
