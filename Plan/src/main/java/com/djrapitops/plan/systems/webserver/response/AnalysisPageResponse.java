package com.djrapitops.plan.systems.webserver.response;

import com.djrapitops.plan.Plan;
import com.djrapitops.plan.data.AnalysisData;
import com.djrapitops.plan.systems.info.BukkitInformationManager;
import com.djrapitops.plan.systems.info.InformationManager;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;

/**
 * @author Rsl1122
 * @since 3.5.2
 */
public class AnalysisPageResponse extends Response {

    /**
     * Constructor.
     *
     * @param informationManager InformationManager to use for getting the Html
     * @throws NullPointerException if AnalysisData has not been cached after 1 second.
     */
    public AnalysisPageResponse(InformationManager informationManager) {
        super.setHeader("HTTP/1.1 200 OK");

        if (informationManager instanceof BukkitInformationManager) {
            AnalysisData analysisData = ((BukkitInformationManager) informationManager).getAnalysisData();
            if (analysisData == null) {
                RunnableFactory.createNew("OnRequestAnalysisRefreshTask", new AbsRunnable() {
                    @Override
                    public void run() {
                        informationManager.refreshAnalysis(Plan.getServerUUID());
                    }
                }).runTaskAsynchronously();

                ErrorResponse analysisRefreshPage = new ErrorResponse();
                analysisRefreshPage.setTitle("Analysis is being refreshed..");
                analysisRefreshPage.setParagraph("<i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
                analysisRefreshPage.replacePlaceholders();
                super.setContent(analysisRefreshPage.getContent());

                return;
            }
        }
        super.setContent(informationManager.getAnalysisHtml());
    }

    public AnalysisPageResponse(String html) {
        super.setHeader("HTTP/1.1 200 OK");
        super.setContent(html);
    }
}
