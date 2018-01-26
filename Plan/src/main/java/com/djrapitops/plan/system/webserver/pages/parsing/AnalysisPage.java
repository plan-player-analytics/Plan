/*
 * Licence is provided in the jar as license.yml also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/license.yml
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.calculation.AnalysisData;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlUtils;

import java.io.IOException;

/**
 * Used for parsing a Html String out of AnalysisData and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPage extends Page {

    private final AnalysisData data;

    public AnalysisPage(AnalysisData analysisData) {
        this.data = analysisData;
    }

    @Override
    public String toHtml() throws ParseException {
        addValues(data.getReplaceMap());

        try {
            return HtmlUtils.replacePlaceholders(FileUtil.getStringFromResource("web/server.html"), placeHolders);
        } catch (IOException e) {
            throw new ParseException(e);
        }
    }
}