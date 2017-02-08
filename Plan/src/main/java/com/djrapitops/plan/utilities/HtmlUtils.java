
package main.java.com.djrapitops.plan.utilities;

import com.djrapitops.plan.Plan;
import java.util.HashMap;
import java.util.Scanner;
import static org.bukkit.plugin.java.JavaPlugin.getPlugin;

/**
 *
 * @author Rsl1122
 */
public class HtmlUtils {
    
    public static String getHtmlStringFromResource(String fileName) {
        Plan plugin = getPlugin(Plan.class);
        Scanner scanner = new Scanner(plugin.getResource(fileName));
        String html = "";
        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            html += line + "\r\n";
        }
        return html;
    }
    
    public static String replacePlaceholders(String html, HashMap<String, String> replaceMap) {
        for (String key : replaceMap.keySet()) {
            html = html.replaceAll(key, replaceMap.get(key));
        }
        return html;
    }
}
