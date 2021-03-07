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

import java.util.logging.Level;
import java.util.logging.Logger;

public class TestErrorLogger implements ErrorLogger {

    @Override
    public void critical(Throwable throwable, ErrorContext context) {
        System.out.println("[CRITICAL] Exception occurred during test, context: " + context);
        Logger.getGlobal().log(Level.SEVERE, "The exception: " + throwable.getMessage(), throwable);
        throw new AssertionError(throwable);
    }

    @Override
    public void error(Throwable throwable, ErrorContext context) {
        System.out.println("[ERROR] Exception occurred during test, context: " + context);
        Logger.getGlobal().log(Level.SEVERE, "The exception: " + throwable.getMessage(), throwable);
        throw new AssertionError(throwable);
    }

    @Override
    public void warn(Throwable throwable, ErrorContext context) {
        System.out.println("[WARN] Exception occurred during test, context: " + context);
        Logger.getGlobal().log(Level.SEVERE, "The exception: " + throwable.getMessage(), throwable);
        throw new AssertionError(throwable);
    }
}
