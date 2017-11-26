package main.java.com.djrapitops.plan.utilities.analysis;

import com.djrapitops.plugin.StaticHolder;
import com.djrapitops.plugin.api.Benchmark;
import com.djrapitops.plugin.api.utility.log.Log;
import com.djrapitops.plugin.task.AbsRunnable;
import com.djrapitops.plugin.task.RunnableFactory;
import com.djrapitops.plugin.utilities.Verify;
import main.java.com.djrapitops.plan.Plan;
import main.java.com.djrapitops.plan.Settings;
import main.java.com.djrapitops.plan.data.AnalysisData;
import main.java.com.djrapitops.plan.data.ServerProfile;
import main.java.com.djrapitops.plan.data.additional.HookHandler;
import main.java.com.djrapitops.plan.data.additional.PluginData;
import main.java.com.djrapitops.plan.database.Database;
import main.java.com.djrapitops.plan.locale.Locale;
import main.java.com.djrapitops.plan.locale.Msg;
import main.java.com.djrapitops.plan.systems.info.BukkitInformationManager;
import main.java.com.djrapitops.plan.systems.info.InformationManager;
import main.java.com.djrapitops.plan.systems.webserver.response.ErrorResponse;
import main.java.com.djrapitops.plan.systems.webserver.response.InternalErrorResponse;
import main.java.com.djrapitops.plan.utilities.html.HtmlStructure;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Rsl1122
 */
public class Analysis {

    private final Plan plugin;
    private int taskId = -1;

    /**
     * Class Constructor.
     *
     * @param plugin Current instance of Plan
     */
    public Analysis(Plan plugin) {
        this.plugin = plugin;
    }

    /**
     * Analyzes the data of all offline players on the server.
     *
     * @param infoManager InformationManager of the plugin.
     */
    public void runAnalysis(InformationManager infoManager) {
        if (isAnalysisBeingRun()) {
            return;
        }

        Benchmark.start("Analysis");
        log(Locale.get(Msg.ANALYSIS_START).toString());
        // Async task for Analysis
        RunnableFactory.createNew(new AbsRunnable("AnalysisTask") {
            @Override
            public void run() {
                try {
                    ErrorResponse analysisRefreshPage = new ErrorResponse();
                    analysisRefreshPage.setTitle("Analysis is being refreshed..");
                    analysisRefreshPage.setParagraph("<meta http-equiv=\"refresh\" content=\"25\" /><i class=\"fa fa-refresh fa-spin\" aria-hidden=\"true\"></i> Analysis is being run, refresh the page after a few seconds.. (F5)");
                    analysisRefreshPage.replacePlaceholders();
                    ((BukkitInformationManager) plugin.getInfoManager()).cacheAnalysisHtml(analysisRefreshPage.getContent());
                    taskId = this.getTaskId();
                    analyze(infoManager, plugin.getDB());
                } catch (Exception e) {
                    Log.toLog(this.getClass().getName() + ":" + this.getTaskName(), e);
                } finally {
                    taskId = -1;
                    this.cancel();
                }

            }
        }).runTaskAsynchronously();
    }

    /**
     * Caches analyzed data of db to the provided cache analysisCache.
     *
     * @param infoManager InformationManager of the plugin.
     *                    method.
     * @param db          Database which data will be analyzed.
     * @return Whether or not analysis was successful.
     */
    public boolean analyze(InformationManager infoManager, Database db) {
        log(Locale.get(Msg.ANALYSIS_FETCH).toString());
        Benchmark.start("Fetch Phase");
        Log.logDebug("Database", "Analysis Fetch");
        Log.logDebug("Analysis", "Analysis Fetch Phase");


        return analyzeData(infoManager, db);
    }

    /**
     * Analyze data in the db about this server.
     *
     * @param infoManager InformationManager of the plugin.
     * @return Success?
     */
    public boolean analyzeData(InformationManager infoManager, Database db) {
        try {
            Benchmark.start("Create Empty dataset");

            AnalysisData analysisData = new AnalysisData();
            List<PluginData> thirdPartyPlugins = plugin.getHookHandler().getAdditionalDataSources();
            analysisData.setPluginsTabLayout(HtmlStructure.createAnalysisPluginsTabLayout(thirdPartyPlugins));

            Benchmark.stop("Analysis", "Create Empty dataset");
            Benchmark.start("Fetch Phase");
            ServerProfile profile = db.getServerProfile(Plan.getServerUUID());
            long fetchPhaseLength = Benchmark.stop("Analysis", "Fetch Phase");

            Benchmark.start("Analysis Phase");
            Log.logDebug("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_PHASE_START).parse(profile.getPlayerCount(), fetchPhaseLength));

            analysisData.analyze(profile);

            Benchmark.stop("Analysis", "Analysis Phase");

            log(Locale.get(Msg.ANALYSIS_3RD_PARTY).toString());
            Log.logDebug("Analysis", "Analyzing additional data sources (3rd party)");
            analysisData.setAdditionalDataReplaceMap(analyzeAdditionalPluginData(profile.getUuids()));
            ((BukkitInformationManager) infoManager).cacheAnalysisData(analysisData);

            // TODO Export
//            ExportUtility.export(analysisData, rawData);
        } catch (Exception e) {
            Log.toLog(this.getClass().getName(), e);
            ((BukkitInformationManager) plugin.getInfoManager()).cacheAnalysisHtml(new InternalErrorResponse(e, "Analysis").getContent());
            Log.logDebug("Analysis", "Error: " + e);
            return false;
        } finally {
            long time = Benchmark.stop("Analysis", "Analysis");
            Log.logDebug("Analysis");
            Log.info(Locale.get(Msg.ANALYSIS_FINISHED).parse(time, ""));
        }
        return true;
    }

    private void log(String msg) {
        if (Settings.ANALYSIS_LOG.isTrue()) {
            Log.info(msg);
        }
    }

    private Map<String, Serializable> analyzeAdditionalPluginData(Set<UUID> uuids) {
        Benchmark.start("Analysis", "3rd party Analysis");
        final Map<String, Serializable> replaceMap = new HashMap<>();
        final HookHandler hookHandler = plugin.getHookHandler();
        final List<PluginData> sources = hookHandler.getAdditionalDataSources().stream()
                .filter(p -> !p.isBanData())
                .filter(p -> !p.getAnalysisTypes().isEmpty())
                .collect(Collectors.toList());
        final AnalysisType[] totalTypes = new AnalysisType[]{
                AnalysisType.INT_TOTAL, AnalysisType.LONG_TOTAL, AnalysisType.LONG_TIME_MS_TOTAL, AnalysisType.DOUBLE_TOTAL
        };
        final AnalysisType[] avgTypes = new AnalysisType[]{
                AnalysisType.INT_AVG, AnalysisType.LONG_AVG, AnalysisType.LONG_TIME_MS_AVG, AnalysisType.LONG_EPOCH_MS_MINUS_NOW_AVG, AnalysisType.DOUBLE_AVG
        };
        final AnalysisType bool = AnalysisType.BOOLEAN_PERCENTAGE;
        final AnalysisType boolTot = AnalysisType.BOOLEAN_TOTAL;
        Log.logDebug("Analysis", "Additional Sources: " + sources.size());
        sources.parallelStream().filter(Verify::notNull).forEach(source -> {
            StaticHolder.saveInstance(this.getClass(), plugin.getClass());
            try {
                Benchmark.start("Analysis", "Source " + source.getPlaceholder());
                final List<AnalysisType> analysisTypes = source.getAnalysisTypes();
                if (analysisTypes.isEmpty()) {
                    return;
                }
                if (analysisTypes.contains(AnalysisType.HTML)) {
                    String html = source.getHtmlReplaceValue(AnalysisType.HTML.getModifier(), UUID.randomUUID());
                    String placeholderName = source.getPlaceholderName(AnalysisType.HTML.getPlaceholderModifier());
                    int length = html.length();
                    if (length < 20000) {
                        replaceMap.put(placeholderName, html);
                    } else {
                        replaceMap.put(placeholderName, "<p>Html was removed because it contained too many characters to be responsive (" + length + "/20000)</p>");
                    }
                    return;
                }
                for (AnalysisType type : totalTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholderName(type.getPlaceholderModifier()), AnalysisUtils.getTotal(type, source, uuids));
                    }
                }
                for (AnalysisType type : avgTypes) {
                    if (analysisTypes.contains(type)) {
                        replaceMap.put(source.getPlaceholderName(type.getPlaceholderModifier()), AnalysisUtils.getAverage(type, source, uuids));
                    }
                }
                if (analysisTypes.contains(bool)) {
                    replaceMap.put(source.getPlaceholderName(bool.getPlaceholderModifier()), AnalysisUtils.getBooleanPercentage(bool, source, uuids));
                }
                if (analysisTypes.contains(boolTot)) {
                    replaceMap.put(source.getPlaceholderName(boolTot.getPlaceholderModifier()), AnalysisUtils.getBooleanTotal(boolTot, source, uuids));
                }
            } catch (Exception | NoClassDefFoundError | NoSuchFieldError | NoSuchMethodError e) {
                Log.error("A PluginData-source caused an exception: " + StringUtils.remove(source.getPlaceholder(), '%'));

                Log.toLog(this.getClass().getName(), e);
            } finally {
                Benchmark.stop("Analysis", "Source " + source.getPlaceholder());
            }
        });
        Benchmark.stop("Analysis", "3rd party Analysis");
        return replaceMap;
    }

    /**
     * Condition whether or not analysis is being run.
     *
     * @return true / false (state)
     */
    public boolean isAnalysisBeingRun() {
        return taskId != -1;
    }

    public void setTaskId(int id) {
        taskId = id;
    }
}
