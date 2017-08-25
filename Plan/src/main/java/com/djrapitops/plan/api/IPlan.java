/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.api;

import com.djrapitops.plugin.IPlugin;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.info.server.ServerInfoManager;
import main.java.com.djrapitops.plan.systems.processing.Processor;
import main.java.com.djrapitops.plan.systems.queue.ProcessingQueue;
import main.java.com.djrapitops.plan.systems.webserver.WebServer;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.File;
import java.io.InputStream;

/**
 * //TODO Class Javadoc Comment
 *
 * @author Rsl1122
 */
public interface IPlan extends IPlugin {
    public Database getDB();

    public ServerVariableHolder getVariable();

    public ServerInfoManager getServerInfoManager();

    public InformationManager getInfoManager();

    public WebServer getWebServer();

    public File getDataFolder();

    public ProcessingQueue getProcessingQueue();

    public void addToProcessQueue(Processor... processors);

    public InputStream getResource(String resource);

    public FileConfiguration getConfig(); // TODO Abstract Config to APF
}