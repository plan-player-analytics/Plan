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

import com.djrapitops.plan.utilities.logging.ErrorContext;
import com.djrapitops.plan.utilities.logging.ErrorLogger;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TestErrorLogger implements ErrorLogger {

    private static final List<Throwable> caught = new ArrayList<>();
    private static boolean throwErrors = true;

    public static void throwErrors(boolean throwErrors) {
        caught.clear();
        TestErrorLogger.throwErrors = throwErrors;
    }

    public static List<Throwable> getCaught() {
        return caught;
    }

    public static Optional<Throwable> getLatest() {
        if (caught.isEmpty()) return Optional.empty();
        return Optional.of(caught.get(caught.size() - 1));
    }

    private static void logException(String level, Throwable throwable, ErrorContext context) {
        String type = throwErrors ? "Unexpected exception" : "Expected exception";
        System.out.println("[" + level + "] " + type + " occurred during test, context: " + context);
        Logger.getGlobal().log(Level.SEVERE, throwable, () -> type + ": " + throwable.getMessage());
    }

    @Override
    public void critical(Throwable throwable, ErrorContext context) {
        logException("CRITICAL", throwable, context);
        throwOrStore(throwable);
    }

    @Override
    public void error(Throwable throwable, ErrorContext context) {
        logException("ERROR", throwable, context);
        throwOrStore(throwable);
    }

    @Override
    public void warn(Throwable throwable, ErrorContext context) {
        logException("WARN", throwable, context);
        throwOrStore(throwable);
    }

    public void throwOrStore(Throwable throwable) {
        if (throwErrors) {
            throw new AssertionError(throwable);
        } else {
            caught.add(throwable);
        }
    }
}
