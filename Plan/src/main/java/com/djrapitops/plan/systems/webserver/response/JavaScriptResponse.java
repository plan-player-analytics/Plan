package com.djrapitops.plan.systems.webserver.response;

import com.djrapitops.plan.settings.theme.Theme;
import com.djrapitops.plan.settings.theme.ThemeVal;
import org.apache.commons.lang3.text.StrSubstitutor;

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
        setContent(StrSubstitutor.replace(Theme.replaceColors(getContent()), replace));
    }
}
