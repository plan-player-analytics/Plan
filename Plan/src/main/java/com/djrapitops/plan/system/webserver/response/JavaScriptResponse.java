package com.djrapitops.plan.system.webserver.response;

import com.djrapitops.plan.system.locale.Locale;
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

    private final Locale locale;

    JavaScriptResponse(String fileName, Locale locale, Theme theme) {
        super(format(fileName));
        this.locale = locale;

        super.setType(ResponseType.JAVASCRIPT);
        Map<String, String> replace = new HashMap<>();
        replace.put("defaultTheme", theme.getValue(ThemeVal.THEME_DEFAULT));
        setContent(StringSubstitutor.replace(theme.replaceThemeColors(getContent()), replace));
    }

    @Override
    public String getContent() {
        return locale.replaceMatchingLanguage(super.getContent());
    }
}
