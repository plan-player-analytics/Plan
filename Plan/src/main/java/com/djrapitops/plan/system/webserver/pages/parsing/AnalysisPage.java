/*
 * License is provided in the jar as LICENSE also here:
 * https://github.com/Rsl1122/Plan-PlayerAnalytics/blob/master/Plan/src/main/resources/LICENSE
 */
package com.djrapitops.plan.system.webserver.pages.parsing;

import com.djrapitops.plan.api.exceptions.ParseException;
import com.djrapitops.plan.data.calculation.AnalysisData;
import com.djrapitops.plan.system.webserver.response.errors.ErrorResponse;
import com.djrapitops.plan.utilities.file.FileUtil;
import com.djrapitops.plan.utilities.html.HtmlUtils;
import com.djrapitops.plugin.utilities.Verify;

import java.io.IOException;

/**
 * Used for parsing a Html String out of AnalysisData and the html file.
 *
 * @author Rsl1122
 */
public class AnalysisPage extends Page {

    private final AnalysisData data;

    public AnalysisPage(AnalysisData analysisData) {
        Verify.nullCheck(analysisData, () -> new IllegalArgumentException("Analysis failed, data object was null"));
        this.data = analysisData;
    }

    public static String getRefreshingHtml() {
        ErrorResponse refreshPage = new ErrorResponse();
        refreshPage.setTitle("Analysis is being refreshed..");
        refreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"5\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
        refreshPage.replacePlaceholders();
        return refreshPage.getContent();
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
