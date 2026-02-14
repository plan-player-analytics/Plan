// https://stackoverflow.com/a/1421988/20825073
import {isString} from "highcharts";

export function isNumber(n) {
    return !isNaN(parseFloat(n))
        || isString(n) && !isNaN(parseFloat(n.replace(',', '.')))
        && !isNaN(n - 0)
}