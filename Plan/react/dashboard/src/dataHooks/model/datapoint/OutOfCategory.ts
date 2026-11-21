export type OutOfCategory = {
    values: { [key: string]: number };
    max: number;
    percentage?: number;
    category?: string;
};

export const isOutOfCategory = (outOfCategory: any) =>
    typeof outOfCategory === 'object' &&
    'values' in outOfCategory &&
    typeof outOfCategory.values === 'object' &&
    'max' in outOfCategory &&
    typeof outOfCategory.max === 'number';
