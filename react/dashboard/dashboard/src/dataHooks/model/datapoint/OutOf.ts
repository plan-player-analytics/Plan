import { FormatType } from './Datapoint';

export type OutOf = {
    value: number;
    max: number;
    percentage: number;
    formatType: FormatType;
};

export const isOutOf = (outOf: any) => {
    console.log(
        outOf,
        outOf &&
            typeof outOf === 'object' &&
            'value' in outOf &&
            'max' in outOf &&
            typeof outOf.max === 'number' &&
            'percentage' in outOf &&
            typeof outOf.percentage === 'number' &&
            'formatType' in outOf &&
            typeof outOf.formatType === 'string',
    );
    return (
        outOf &&
        typeof outOf === 'object' &&
        'value' in outOf &&
        'max' in outOf &&
        typeof outOf.max === 'number' &&
        'percentage' in outOf &&
        typeof outOf.percentage === 'number' &&
        'formatType' in outOf &&
        typeof outOf.formatType === 'string'
    );
};
