/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.response.errors;

import com.djrapitops.plan.system.file.PlanFiles;
import com.djrapitops.plan.system.webserver.response.pages.PageResponse;
import org.apache.commons.text.StringSubstitutor;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents generic HTTP Error response that has the page style in it.
 *
 * @author Rsl1122
 */
public class ErrorResponse extends PageResponse {

    private String title;
    private String paragraph;

    private String version;

    public ErrorResponse(String version, PlanFiles files) throws IOException {
        this.version = version;
        setContent(files.readCustomizableResourceFlat("web/error.html"));
    }

    public ErrorResponse(String message) {
        setContent(message);
    }

    public void replacePlaceholders() {
        Map<String, String> placeHolders = new HashMap<>();
        placeHolders.put("title", title);
        String[] split = title.split(">", 3);
        placeHolders.put("titleText", split.length == 3 ? split[2] : title);
        placeHolders.put("paragraph", paragraph);
        placeHolders.put("version", version);

        setContent(StringSubstitutor.replace(getContent(), placeHolders));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setParagraph(String paragraph) {
        this.paragraph = paragraph;
    }
}
