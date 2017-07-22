package main.java.com.djrapitops.plan.data.analysis;

import com.djrapitops.plugin.api.TimeAmount;
import java.util.List;
import main.java.com.djrapitops.plan.data.TPS;
import main.java.com.djrapitops.plan.ui.html.graphs.TPSGraphCreator;
import main.java.com.djrapitops.plan.utilities.FormatUtils;
import main.java.com.djrapitops.plan.utilities.MiscUtils;
import main.java.com.djrapitops.plan.utilities.analysis.MathUtils;

/**
 * Part responsible for all TPS related analysis.
 *
 * Ticks Per Second Graphs
 *
 * Placeholder values can be retrieved using the get method.
 *
 * Contains following place-holders: tpsscatterday, tpsscatterweek, averagetps,
 * averagetpsday
 *
 * @author Rsl1122
 * @since 3.5.2
 */
public class TPSPart extends RawData<TPSPart> {

    private final List<TPS> tpsData;

    public TPSPart(List<TPS> tpsData) {
        this.tpsData = tpsData;
    }

    @Override
    public void analyse() {
        long now = MiscUtils.getTime();
        List<TPS> week = TPSGraphCreator.filterTPS(tpsData, now - TimeAmount.WEEK.ms());
        List<TPS> day = TPSGraphCreator.filterTPS(tpsData, now - TimeAmount.DAY.ms());

        String tpsScatterDay = TPSGraphCreator.buildScatterDataStringTPS(day, TimeAmount.DAY.ms());
        String tpsScatterWeek = TPSGraphCreator.buildScatterDataStringTPS(week, TimeAmount.WEEK.ms());

        addValue("tpsscatterday", tpsScatterDay);
        addValue("tpsscatterweek", tpsScatterWeek);

        double averageTPSweek = MathUtils.averageDouble(week.stream().map(t -> t.getTps()));
        double averageTPSday = MathUtils.averageDouble(day.stream().map(t -> t.getTps()));

        addValue("averagetps", FormatUtils.cutDecimals(averageTPSweek));
        addValue("averagetpsday", FormatUtils.cutDecimals(averageTPSday));
    }

    public List<TPS> getTpsData() {
        return tpsData;
    }
}
