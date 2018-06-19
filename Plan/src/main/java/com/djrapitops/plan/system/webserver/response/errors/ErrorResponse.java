/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.webserver.response.Response;
import com.djrapitops.plan.utilities.MiscUtils;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plugin.api.utility.log.Log;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
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
            setContent(Theme.replaceColors(FileUtil.getStringFromResource("web/error.html")));
        } catch (IOException e) {
            Log.toLog(this.getClass(), e);
        }
    }

    public void replacePlaceholders() {
        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("title", title);
        String[] split = title.split(">", 3);
        placeHolders.put("titleText", split.length == 3 ? split[2] : title);
        placeHolders.put("paragraph", paragraph);
        placeHolders.put("version", MiscUtils.getPlanVersion());

        setContent(StringSubstitutor.replace(getContent(), placeHolders));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}
