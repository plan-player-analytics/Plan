package net.playeranalytics.plugin;

import net.playeranalytics.plugin.scheduling.RunnableFactory;
import net.playeranalytics.plugin.server.JavaUtilPluginLogger;
import net.playeranalytics.plugin.server.Listeners;
import net.playeranalytics.plugin.server.PluginLogger;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.logging.Logger;

public class StandalonePlatformAbstractionLayer implements PlatformAbstractionLayer {

    private final PluginLogger logger;

    public StandalonePlatformAbstractionLayer(Logger logger) {this.logger = new JavaUtilPluginLogger(logger);}

    @Override
    public PluginLogger getPluginLogger() {
        return logger;
    }

    @Override
    public Listeners getListeners() {
        return new Listeners() {
            @Override
            public void registerListener(Object o) {
            }

            @Override
            public void unregisterListener(Object o) {
            }

            @Override
            public void unregisterListeners() {
            }
        };
    }

    @Override
    public RunnableFactory getRunnableFactory() {
        return null;
    }

    @Override
    public PluginInformation getPluginInformation() {
        return new PluginInformation() {
            @Override
            public InputStream getResourceFromJar(String s) {
                return getClass().getResourceAsStream(s);
            }

            @Override
            public File getDataFolder() {
                return new File("Plan");
            }

            @Override
            public String getVersion() {
                try {
                    return readVersionFromPluginYml();
                } catch (IOException | URISyntaxException e) {
                    return e.toString();
                }
            }

            private String readVersionFromPluginYml() throws IOException, URISyntaxException {
                String pluginYmlContents = new String(Files.readAllBytes(new File(getClass().getResource("plugin.yml").toURI()).toPath()));
                String versionHalf = StringUtils.split(pluginYmlContents, "version:")[1];
                return StringUtils.split(versionHalf, "\n")[0];
            }
        };
    }
}
