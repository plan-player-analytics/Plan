import {ExtensionGraph} from "../../model/extension/ExtensionGraph";
import {useMemo} from "react";
import {tooltip, translateLinegraphButtons} from "../../../util/graphs";
import {useTranslation} from "react-i18next";

export const useExtensionGraphAsOptions = (graph: ExtensionGraph) => {
    const {t} = useTranslation();
    const {
        columnCount, dataPoints, seriesColors, seriesLabels, unitNames, valueFormats,
        displayName, xAxisSoftMin, xAxisSoftMax, yAxisSoftMin, yAxisSoftMax, xAxisType, supportsStacking
    } = graph;

    const {yAxis, yAxisIndexes} = useMemo(() => {
        unitNames.fill("", unitNames.length, columnCount);
        const unitNamesAndIndexes = unitNames.map((unit, i) => unitNames.indexOf(unit) === i ? {unit, i} : undefined)
            .filter(u => u !== undefined);
        const unitNamesToAxis: { [key: string]: number } = {};
        for (const unitNamesAndIndex of unitNamesAndIndexes) {
            if (unitNamesAndIndex.unit === null) continue;
            unitNamesToAxis[unitNamesAndIndex.unit] = unitNamesAndIndex.i;
        }
        return {
            yAxis: unitNamesAndIndexes.map(unitAndIndex => ({
                title: {text: unitAndIndex.unit},
                // labels: {
                //     formatter: function (): string {
                //         return this.value + (unitAndIndex.unit || '');
                //     }
                // },
                softMax: yAxisSoftMax,
                softMin: yAxisSoftMin
            })),
            yAxisIndexes: unitNames.map(unit => unitNamesToAxis[unit || ""])
        }
    }, [unitNames]);

    const actuallySupportsStacking = useMemo(() => supportsStacking && yAxis.length <= 1, [supportsStacking, yAxis])

    let fallbackColorIndex = 0;
    const defaultColors = [
        "var(--color-plugin-cyan)",
        "var(--color-plugin-light-blue)",
        "var(--color-plugin-blue)",
        "var(--color-plugin-teal)",
        "var(--color-plugin-green)",
        "var(--color-plugin-light-green)",
        "var(--color-plugin-lime)",
        "var(--color-plugin-amber)",
        "var(--color-plugin-orange)",
        "var(--color-plugin-deep-orange)",
        "var(--color-plugin-red)",
        "var(--color-plugin-pink)",
        "var(--color-plugin-purple)",
        "var(--color-plugin-deep-purple)",
        "var(--color-plugin-indigo)",
    ];

    const series = useMemo(() => {
        const ser = [];
        for (let i = 0; i < columnCount; i++) {
            const label = seriesLabels.length > i && seriesLabels[i] ? seriesLabels[i] : `series #${i + 1}`;
            let color = defaultColors[fallbackColorIndex];
            if (seriesColors.length > i && seriesColors[i]) {
                color = seriesColors[i]!;
            } else {
                fallbackColorIndex++;
            }
            const yAxis = yAxisIndexes[i];
            // TODO This may be computationally expensive, should be done in a worker.
            const data = dataPoints.map(point => {
                point.fill(null, point.length, columnCount);
                return [point[0], point[i + 1]]
            })
            ser.push({
                name: label,
                type: actuallySupportsStacking ? 'areaspline' : 'spline',
                tooltip: {
                    ...tooltip.twoDecimals,
                    valueSuffix: ` ${unitNames[i] || ''}`
                },
                yAxis,
                data,
                color
            });
        }
        return ser;
    }, [columnCount, seriesLabels, seriesColors, yAxisIndexes, dataPoints]);

    return useMemo(() => ({
        title: {
            text: displayName,
            floating: true,
            y: 16, // Adjust for the reduced padding inside card section
            style: {
                fontFamily: '"Nunito", system-ui, -apple-system, "Segoe UI", Roboto, "Helvetica Neue", Arial, "Noto Sans", "Liberation Sans", sans-serif, "Apple Color Emoji", "Segoe UI Emoji", "Segoe UI Symbol", "Noto Color Emoji"'
            }
        },
        rangeSelector: {
            selected: 2,
            buttons: translateLinegraphButtons(t)
        },
        legend: {
            enabled: columnCount > 1,
        },
        plotOptions: {
            areaspline: {
                fillOpacity: 0.4
            },
            series: {
                animation: false,
                stacking: actuallySupportsStacking ? "normal" : undefined
            }
        },
        xAxis: {
            zoomEnabled: true,
            title: {
                text: ""
            },
            softMin: xAxisSoftMin,
            softMax: xAxisSoftMax
        },
        yAxis,
        tooltip: {
            enabled: true,
            valueDecimals: 2
        },
        series: series
    }), [yAxis, series]);
}