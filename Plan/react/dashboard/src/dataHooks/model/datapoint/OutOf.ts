import {FormatType} from "./Datapoint";

export type OutOf = {
    value: number;
    max: number;
    percentage: number;
    formatType: FormatType;
}

export const isOutOf = (outOf: any) => {
    return outOf && 'value' in outOf && 'max' in outOf && 'percentage' in outOf && 'formatType' in outOf;
}