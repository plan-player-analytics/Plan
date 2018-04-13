package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.settings.theme.Theme;
import com.djrapitops.plan.system.settings.theme.ThemeVal;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class JavaScriptResponse extends FileResponse {

    public JavaScriptResponse(String fileName) {
        super(format(fileName));
        super.setType(ResponseType.JAVASCRIPT);
        Map<String, String> replace = new HashMap<>();
        replace.put("defaultTheme", Theme.getValue(ThemeVal.THEME_DEFAULT));
        setContent(StringSubstitutor.replace(Theme.replaceColors(getContent()), replace));
    }
}
