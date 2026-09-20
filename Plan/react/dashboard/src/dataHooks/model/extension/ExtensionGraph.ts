import {FormatType} from "../datapoint/Datapoint";

export type XAxisType = 'VALUE' | 'DATE_MILLIS' | 'TIME_AMOUNT_MILLIS';

export type ExtensionGraph = {
    providerId: number;
    graphTableName: string;
    displayName: string;
    xAxisType: XAxisType;
    xAxisSoftMin: number;
    xAxisSoftMax: number;
    yAxisSoftMin: number;
    yAxisSoftMax: number;
    columnCount: number;
    seriesLabels: string[];
    unitNames: string[];
    valueFormats: FormatType[];
    seriesColors: string[];
    supportsStacking: boolean;
    dataPoints: number[][];
};
