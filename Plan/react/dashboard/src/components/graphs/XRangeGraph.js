import React, {useEffect} from "react";
import Highcharts from 'highcharts';
import XRange from "highcharts/modules/xrange";
import {useTheme} from "../../hooks/themeHook";
import {useTranslation} from "react-i18next";
import Accessibility from "highcharts/modules/accessibility";
import {withReducedSaturation} from "../../util/colors";

const XRangeGraph = ({id, pointsByAxis, height}) => {
    const {t} = useTranslation();
    const {graphTheming, nightModeEnabled} = useTheme();
    useEffect(() => {
        const data = [];
        const categories = [];
        for (let i = 0; i < pointsByAxis.length; i++) {
            const axis = pointsByAxis[i];
            categories.push(axis.name);
            data.push(...axis.points.map(point => {
                return {x: point.x, x2: point.x2, y: i, color: point.color};
            }));
        }
        const startOfDay = pointsByAxis[0].points[0].x - pointsByAxis[0].points[0].x % (24 * 60 * 60 * 1000);
        const endOfDay = startOfDay + (24 * 60 * 60 * 1000);

        const series = {
            name: t('html.label.sessions'),
            color: nightModeEnabled ? withReducedSaturation('#222') : '#222',
            data: data,
            animation: false,
            pointWidth: 20,
            colorByPoint: true
        };
        Accessibility(Highcharts);
        XRange(Highcharts);
        Highcharts.setOptions(graphTheming);
        setTimeout(() => Highcharts.chart(id, {
            chart: {
                backgroundColor: 'transparent',
                plotBackgroundColor: 'transparent',
                type: 'xrange'
            },
            title: {text: ''},
            xAxis: {
                type: 'datetime',
                min: startOfDay,
                max: endOfDay
            },
            time: {
                timezoneOffset: 0
            },
            legend: {
                enabled: false
            },
            yAxis: {
                title: {
                    text: ''
                },
                categories: categories,
                reversed: true,
                visible: false
            },
            series: [series]
        }), 25)
    }, [pointsByAxis, graphTheming, t, nightModeEnabled, id])

    return (
        <div style={{height}} id={id}>
            <span className="loader"/>
        </div>
    )
}

export default XRangeGraph