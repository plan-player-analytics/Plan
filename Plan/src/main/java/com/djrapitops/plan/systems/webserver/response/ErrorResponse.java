/* 
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package main.java.com.djrapitops.plan.systems.webserver.response;

import com.djrapitops.plugin.api.utility.log.Log;
import main.java.com.djrapitops.plan.systems.webserver.theme.Theme;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.file.FileUtil;
import org.apache.commons.lang3.text.StrSubstitutor;

import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents generic HTTP Error response that has the page style in it.
 *
 * @author Rsl1122
 */
public class ErrorResponse extends Response {

    private String title;
    private String paragraph;

    public ErrorResponse() {
        try {
            setContent(Theme.replaceColors(FileUtil.getStringFromResource("error.html")));
        } catch (FileNotFoundException e) {
            Log.toLog(this.getClass().getName(), e);
        }
    }

    public void replacePlaceholders() {
        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("title", title);
        placeHolders.put("paragraph", paragraph);
        placeHolders.put("version", MiscUtils.getPlanVersion());

        setContent(StrSubstitutor.replace(getContent(), placeHolders));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}