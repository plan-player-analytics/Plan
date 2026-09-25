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
    seriesLabels: (string | null)[];
    unitNames: (string | null)[];
    valueFormats: (FormatType | null)[];
    seriesColors: (string | null)[];
    supportsStacking: boolean;
    dataPoints: (number | null)[][];
};
