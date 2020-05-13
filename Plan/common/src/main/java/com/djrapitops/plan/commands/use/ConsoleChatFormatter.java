package com.djrapitops.plan.commands.use;

import org.apache.commons.lang3.StringUtils;

public class ConsoleChatFormatter extends ChatFormatter {

    @Override
    public int getWidth(String part) {
        return part.length() - (StringUtils.countMatches(part, '§') * 2);
    }
}
