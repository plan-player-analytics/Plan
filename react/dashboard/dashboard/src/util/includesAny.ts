export function includesAny(str: string, ...values: string[]): boolean {
    return values.some(value => str.includes(value));
}