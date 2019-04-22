package utilities.mocks;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.processing.Processing;
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