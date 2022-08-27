import React, {useEffect} from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import {withReducedSaturation} from "../../util/colors";
import Highcharts from 'highcharts/highmaps.js';
import map from '@highcharts/map-collection/custom/world.geo.json';
import Accessibility from "highcharts/modules/accessibility";

const GeolocationWorldMap = ({series, colors}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();

    useEffect(() => {
        const mapSeries = {
            name: t('html.label.players'),
            type: 'map',
            mapData: map,
            data: series,
            joinBy: ['iso-a3', 'code']
        };

        Accessibility(Highcharts);
        Highcharts.setOptions(graphTheming);
        Highcharts.mapChart('countryWorldMap', {
            chart: {
                animation: true
            },
            title: {text: ''},

            mapNavigation: {
                enabled: true,
                enableDoubleClickZoomTo: true
            },

            colorAxis: {
                min: 1,
                type: 'logarithmic',
                minColor: nightModeEnabled ? withReducedSaturation(colors.low) : colors.low,
                maxColor: nightModeEnabled ? withReducedSaturation(colors.high) : colors.high
            },
            series: [mapSeries]
        })
    }, [colors, series, graphTheming, nightModeEnabled, t]);

    return (<div id="countryWorldMap"/>);
};

export default GeolocationWorldMap