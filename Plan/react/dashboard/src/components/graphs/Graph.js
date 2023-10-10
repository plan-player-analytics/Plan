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
                   className,
                   style,
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

    const tallStyle = tall ? {height: "450px"} : undefined;
    const givenStyle = style ? style : tallStyle;

    return (
        <div className={className || "chart-area"} style={givenStyle} id={id}>
            <span className="loader"/>
        </div>
    )
}

export default Graph