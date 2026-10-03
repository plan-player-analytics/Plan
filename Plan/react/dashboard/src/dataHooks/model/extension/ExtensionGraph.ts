export type XAxisType = 'VALUE' | 'DATE_MILLIS' | 'TIME_AMOUNT_MILLIS';

export enum GraphFormatType {
    NONE = "NONE",
    TIME_AMOUNT = "TIME_AMOUNT",
    MILLISECONDS = "MILLISECONDS",
    PERCENTAGE = "PERCENTAGE",
    BYTES = "BYTES",
    INTEGER = "INTEGER"
}

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
    valueFormats: (GraphFormatType | null)[];
    seriesColors: (string | null)[];
    supportsStacking: boolean;
    dataPoints: (number | null)[][];
};
