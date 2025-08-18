import React, {useEffect} from 'react';
import {useTranslation} from "react-i18next";
import {useTheme} from "../../hooks/themeHook";
import Highcharts from 'highcharts/esm/highmaps';
import topology from '@highcharts/map-collection/custom/world.topo.json';
import "highcharts/esm/modules/accessibility";
import "highcharts/esm/modules/no-data-to-display";
import {calculateCssHexColor} from "../../util/colors.js";
import {localeService} from "../../service/localeService.js";

export const ProjectionOptions = {
    MILLER: "html.label.geoProjection.miller",
    MERCATOR: "html.label.geoProjection.mercator",
    EQUAL_EARTH: "html.label.geoProjection.equalEarth"
    // ORTOGRAPHIC: "html.label.geoProjection.ortographic"
}

const getProjection = option => {
    switch (option) {
        case ProjectionOptions.MERCATOR:
            return {name: 'WebMercator'};
        case ProjectionOptions.EQUAL_EARTH:
            return {name: 'EqualEarth'};
        // Ortographic projection stops working after a while for some reason
        // case ProjectionOptions.ORTOGRAPHIC:
        //     return {name: 'Orthographic'};
        case ProjectionOptions.MILLER:
        default:
            return {name: 'Miller'};
    }
}

const GeolocationWorldMap = ({series, colors, projection, onClickCountry}) => {
    const {t} = useTranslation();
    const {nightModeEnabled, graphTheming} = useTheme();

    const minColor = calculateCssHexColor("var(--color-graphs-world-map-low)");
    const maxColor = calculateCssHexColor("var(--color-graphs-world-map-high)");
    useEffect(() => {
        const regions = new Intl.DisplayNames([localeService.getIntlFriendlyLocale()], {type: 'region'});
        const mapSeries = {
            name: t('html.label.players'),
            type: 'map',
            data: series,
            joinBy: ['iso-a3', 'code'],
            point: {
                events: {
                    click: onClickCountry
                }
            }
        };

        Highcharts.setOptions(graphTheming);
        Highcharts.setOptions({
            lang: {
                locale: localeService.getIntlFriendlyLocale(),
                noData: t('html.label.noDataToDisplay')
            }
        });
        Highcharts.mapChart('countryWorldMap', {
            chart: {
                noData: t('html.label.noDataToDisplay'),
                map: topology,
                animation: true
            },
            title: {text: ''},

            mapNavigation: {
                enabled: true,
                enableDoubleClickZoomTo: true,
                enableMouseWheelZoom: true,
                enableTouchZoom: true
            },

            mapView: {
                projection: getProjection(projection)
            },

            tooltip: {
                formatter: function () {
                    const translatedRegion = regions.of(this.properties['iso-a2']);
                    return `${this.series.name}<br><span style="color:${this.color}">●</span> ${translatedRegion}: ${this.value}`
                }
            },

            colorAxis: {
                min: 1,
                type: 'logarithmic',
                minColor,
                maxColor
            },
            series: [mapSeries]
        })
    }, [colors, series, graphTheming, nightModeEnabled, t, projection, onClickCountry]);

    return (<div id="countryWorldMap"/>);
};

export default GeolocationWorldMap