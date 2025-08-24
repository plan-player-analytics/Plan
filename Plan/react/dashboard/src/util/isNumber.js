// https://stackoverflow.com/a/1421988/20825073
export function isNumber(n) {
    return !isNaN(parseFloat(n)) && !isNaN(n - 0)
}