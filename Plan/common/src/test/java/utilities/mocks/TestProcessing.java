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
package utilities.mocks;

import com.djrapitops.plan.system.processing.Processing;
import com.djrapitops.plan.system.settings.locale.Locale;
import com.djrapitops.plugin.logging.console.PluginLogger;
import com.djrapitops.plugin.logging.error.ErrorHandler;
import com.google.common.util.concurrent.MoreExecutors;
import dagger.Lazy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;

/**
 * Directly executing version of Processing for Tests to avoid concurrency issues during tests.
 *
 * @author Rsl1122
 */
@Singleton
public class TestProcessing extends Processing {

    @Inject
    public TestProcessing(Lazy<Locale> locale, PluginLogger logger, ErrorHandler errorHandler) {
        super(locale, logger, errorHandler);
    }

    @Override
    protected ExecutorService createExecutor(int i, String s) {
        return MoreExecutors.newDirectExecutorService();
    }
}