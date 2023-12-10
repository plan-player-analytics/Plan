import {useTheme} from "../../hooks/themeHook";
import React, {useEffect} from "react";
import Highcharts from "highcharts/highstock";
import HighchartsMore from "highcharts/highcharts-more";
import Dumbbell from "highcharts/modules/dumbbell";
import NoDataDisplay from "highcharts/modules/no-data-to-display"
import Accessibility from "highcharts/modules/accessibility"
import {useTranslation} from "react-i18next";

const Graph = ({
                       id,
                       options,
                       tall,
                   }) => {
    const {t} = useTranslation()
    const {graphTheming, nightModeEnabled} = useTheme();

    useEffect(() => {
        NoDataDisplay(Highcharts);
        Accessibility(Highcharts);
        HighchartsMore(Highcharts);
        Dumbbell(Highcharts);
        Highcharts.setOptions({lang: {noData: t('html.label.noDataToDisplay')}})
        Highcharts.setOptions(graphTheming);
        Highcharts.chart(id, options);
    }, [options, id, t,
        graphTheming, nightModeEnabled]);

    const style = tall ? {height: "450px"} : undefined;

    return (
        <div className="chart-area" style={style} id={id}>
            <span className="loader"/>
        </div>
    )
}

export default Graph