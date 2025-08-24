import {useTheme} from "../../hooks/themeHook";
import React, {useEffect} from "react";
import Highcharts from "highcharts/esm/highstock";
import "highcharts/esm/modules/accessibility";
import "highcharts/esm/highcharts-more";
import "highcharts/esm/modules/dumbbell";
import "highcharts/esm/modules/no-data-to-display";
import {useTranslation} from "react-i18next";
import {localeService} from "../../service/localeService.js";

const Graph = ({
                   id,
                   options,
                   tall,
               }) => {
    const {t} = useTranslation()
    const {graphTheming, nightModeEnabled} = useTheme();

    useEffect(() => {
        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        })
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