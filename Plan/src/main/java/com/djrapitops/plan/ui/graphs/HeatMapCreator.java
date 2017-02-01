
package main.java.com.djrapitops.plan.ui.graphs;

import com.djrapitops.plan.Plan;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bukkit.Location;
import org.tc33.jheatchart.HeatChart;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class HeatMapCreator {
    
    public static String createLocationHeatmap(List<Location> locations) {
        double[][] data = new double[1000][1000];
        for (int i = 0; i < 1000; i++) {
            for (int j = 0; j < 1000; j++) {
                data[i][j] = 0;
            }
        }
        for (Location loc : locations) {
            int x = loc.getBlockX()+500;
            int z = loc.getBlockZ()+500;
            if (x < 0 || x > 1000) {
                continue;
            }
            if (z < 0 || z > 1000) {
                continue;
            }
            data[x][z] = data[x][z] + 1;
        }
        HeatChart map = new HeatChart(data);
        
        map.setTitle("Location heatmap.");
        map.setXAxisLabel("X");
        map.setYAxisLabel("Z");
        try {
            String folder = getPlugin(Plan.class).getDataFolder().getAbsolutePath()+File.separator+"Heatmaps";
            map.saveToFile(new File(folder + File.separator+"java-heat-chart.png"));
        } catch (IOException ex) {
            Logger.getLogger(HeatMapCreator.class.getName()).log(Level.SEVERE, null, ex);
        }
        return "";
    }
    
}
