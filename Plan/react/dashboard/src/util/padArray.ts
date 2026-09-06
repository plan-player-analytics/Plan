export function padArray<T>(arr: T[], length: number, value: T) {
    return arr.concat(new Array(Math.max(0, length - arr.length)).fill(value));
}