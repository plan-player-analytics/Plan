// https://stackoverflow.com/a/1421988/20825073
import {isString} from "highcharts";

export function isNumber(n) {
    return !Number.isNaN(Number.parseFloat(n))
        || isString(n) && !Number.isNaN(Number.parseFloat(n.replace(',', '.')))
        && !Number.isNaN(n - 0)
}