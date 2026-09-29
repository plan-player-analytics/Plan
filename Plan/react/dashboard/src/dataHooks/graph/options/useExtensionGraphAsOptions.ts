import {ExtensionGraph, GraphFormatType, XAxisType} from "../../model/extension/ExtensionGraph";
import {useCallback, useMemo} from "react";
import {tooltip, translateLinegraphButtons} from "../../../util/graphs";
import {useTranslation} from "react-i18next";
import {useTimeAmountFormatter} from "../../../util/format/useTimeAmountFormatter";
import {useDecimalFormatter} from "../../../util/format/useDecimalFormatter";
import {useByteSizeFormatter} from "../../../util/format/useByteSizeFormatter";
import {usePingFormatter} from "../../../util/format/usePingFormatter";
import {useDateFormatter} from "../../../util/format/useDateFormatter";
import {Point} from "highcharts";
import {useMetadata} from "../../../hooks/metadataHook";
import {removeUndefined} from "../../../util/removeUndefined";

export const useExtensionGraphAsOptions = (graph: ExtensionGraph) => {
    const {t} = useTranslation();
    const {formatDate} = useDateFormatter();
    const {formatTime} = useTimeAmountFormatter();
    const {formatDecimals} = useDecimalFormatter();
    const {formatByteSize} = useByteSizeFormatter();
    const {formatPing} = usePingFormatter();
    const metadata = useMetadata();
    const {
        columnCount, dataPoints, seriesColors, seriesLabels, unitNames, valueFormats,
        displayName, xAxisSoftMin, xAxisSoftMax, yAxisSoftMin, yAxisSoftMax, xAxisType, supportsStacking
    } = graph;

    const formatX = useCallback((value: number, formatType: XAxisType | null) => {
        switch (formatType) {
            case "DATE_MILLIS":
                return formatDate(value);
            case "TIME_AMOUNT_MILLIS":
                return formatTime(value as number);
            case "VALUE":
            default:
                return value;
        }
    }, []);

    const formatValue = useCallback((value: number | undefined, formatType: GraphFormatType | null) => {
        if (value === undefined) return value;
        switch (formatType) {
            case "TIME_AMOUNT":
                return formatTime(value);
            case "MILLISECONDS":
                return formatPing(value as number);
            case "PERCENTAGE":
                return formatDecimals(value as number * 100) + '%';
            case "BYTES":
                return formatByteSize(value as number);
            case "INTEGER":
                return value.toFixed(0);
            case "NONE":
            default:
                return value;
        }
    }, []);

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
            yAxis: unitNamesAndIndexes.map((unitAndIndex, i) => ({
                title: {text: unitAndIndex.unit},
                zoomEnabled: xAxisType !== "DATE_MILLIS",
                softMax: yAxisSoftMax,
                softMin: yAxisSoftMin,
                labels: {
                    formatter: function () {
                        if ('value' in this) {
                            return formatValue(this.value as number, valueFormats[i]) + ' ' + (unitAndIndex.unit || '')
                        }
                    }
                }
            })),
            yAxisIndexes: unitNames.map(unit => unitNamesToAxis[unit || ""])
        }
    }, [unitNames, valueFormats]);

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

    return useMemo(() => removeUndefined({
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
        chart: xAxisType !== "DATE_MILLIS" ? {
            zooming: {
                type: 'x'
            }
        } : undefined,
        xAxis: {
            zoomEnabled: xAxisType !== "DATE_MILLIS",
            title: {
                text: ""
            },
            softMin: xAxisSoftMin,
            softMax: xAxisSoftMax,
            labels: xAxisType !== "DATE_MILLIS" ? {
                formatter: function () {
                    if ('value' in this) {
                        return formatX(this.value as number, xAxisType)
                    }
                }
            } : undefined
        },
        yAxis,
        time: {
            timezoneOffset: metadata.loaded && metadata.timeZoneOffsetMinutes || 0
        },
        tooltip: {
            enabled: true,
            valueDecimals: 2,
            formatter: function (): string | string[] {
                const ctx = this as unknown as Point;
                if (ctx.points) {
                    return [formatX(ctx.x, xAxisType), ...ctx.points.map((point: Point) => `<span style="color:${point.color}">●</span> ${point.series.name}: <b>${formatValue(point.y, valueFormats[point.series.index as number])}</b>`)]
                } else {
                    return `${formatX(ctx.x, xAxisType)}<br><br><span style="color:${ctx.color}">●</span> ${ctx.series.name}: <b>${formatValue(ctx.y, valueFormats[ctx.series.index])}</b>`
                }
            }
        },
        series: series
    }), [yAxis, series, xAxisType]);
}