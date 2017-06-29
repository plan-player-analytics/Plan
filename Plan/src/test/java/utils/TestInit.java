/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package test.java.utils;

import com.djrapitops.javaplugin.status.ProcessStatus;
import com.djrapitops.javaplugin.utilities.BenchmarkUtil;
import com.djrapitops.javaplugin.utilities.log.BukkitLog;
import java.io.File;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.util.logging.Logger;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.ServerVariableHolder;
import main.java.com.djrapitops.plan.Settings;
import org.bukkit.Server;
import org.bukkit.configuration.file.YamlConfiguration;
import org.powermock.api.mockito.PowerMockito;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 *
 * @author Rsl1122
 */
public class TestInit {

    private Plan planMock;

    /**
     *
     */
    public TestInit() {
    }

    /**
     *
     * @return
     */
    public boolean setUp() {
        try {
            planMock = PowerMockito.mock(Plan.class);
            File configfile = new File(getClass().getResource("/config.yml").getPath());
            YamlConfiguration configuration = new YamlConfiguration();
            configuration.load(configfile.getAbsolutePath());
            when(planMock.getConfig()).thenReturn(configuration);
            File testFolder = new File("temporaryTestFolder");
            if (testFolder.exists()) {
                for (File f : testFolder.listFiles()) {
                    Files.deleteIfExists(f.toPath());
                }
            }
            Files.deleteIfExists(new File("temporaryTestFolder").toPath());
            testFolder = new File("temporaryTestFolder");
            testFolder.mkdir();
//
            when(planMock.getDataFolder()).thenReturn(testFolder);
            File analysis = new File(getClass().getResource("/analysis.html").getPath());
            when(planMock.getResource("analysis.html")).thenReturn(new FileInputStream(analysis));
            File player = new File(getClass().getResource("/player.html").getPath());
            when(planMock.getResource("player.html")).thenReturn(new FileInputStream(player));

            Server mockServer = PowerMockito.mock(Server.class);
            when(mockServer.getIp()).thenReturn("0.0.0.0");
            when(mockServer.getMaxPlayers()).thenReturn(20);
//            Mockito.doReturn("0.0.0.0").when(mockServer).getIp();
            when(planMock.getServer()).thenReturn(mockServer);
            when(planMock.getLogger()).thenReturn(Logger.getGlobal());
            ServerVariableHolder serverVariableHolder = new ServerVariableHolder(mockServer);
            when(planMock.getVariable()).thenReturn(serverVariableHolder);
            BukkitLog<Plan> log = new BukkitLog(planMock, "console", "");
            when(planMock.getPluginLogger()).thenReturn(log);
            ProcessStatus<Plan> process = new ProcessStatus(planMock);
            when(planMock.processStatus()).thenReturn(process);
            BenchmarkUtil bench = new BenchmarkUtil();
            when(planMock.benchmark()).thenReturn(bench);
            Plan.setInstance(planMock);
//            Mockito.doReturn("0.0.0.0").when(planMock).getServer().getIp();      
            Settings.DEBUG.setValue(true);
            return true;
        } catch (Exception ex) {
            System.out.println(ex);
            StackTraceElement[] stackTrace = ex.getStackTrace();
            for (StackTraceElement stackTraceElement : stackTrace) {
                System.out.println(stackTraceElement);
            }
            return false;
        }
    }

    /**
     *
     * @return
     */
    public Plan getPlanMock() {
        return planMock;
    }
}
