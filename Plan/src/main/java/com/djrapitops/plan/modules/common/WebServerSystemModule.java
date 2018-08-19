package com.djrapitops.plan.modules.common;

import com.djrapitops.plan.system.locale.Locale;
import com.djrapitops.plan.system.webserver.WebServer;
import com.djrapitops.plan.system.webserver.WebServerSystem;
import dagger.Module;
import dagger.Provides;

/**
 * Dagger module for WebServerSystem.
 *
 * @author Rsl1122
 */
@Module
public class WebServerSystemModule {

    @Provides
    WebServerSystem provideWebServerSystem(Locale locale) {
        return new WebServerSystem(() -> locale);
    }

    @Provides
    WebServer provideWebServer(WebServerSystem webServerSystem) {
        return webServerSystem.getWebServer();
    }

}