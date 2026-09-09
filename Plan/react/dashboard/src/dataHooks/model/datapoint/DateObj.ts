export type DateObj = {
    date: number;
    value: number;
}

export function isDateObj(dateObj: any) {
    return typeof dateObj === 'object' && 'date' in dateObj && typeof dateObj.date === 'number'
        && 'value' in dateObj && typeof dateObj.value === 'number';
}