// https://stackoverflow.com/a/1421988/20825073
import {isString} from "highcharts";

export function isNumber(n) {
    const parseableFloat = !Number.isNaN(Number.parseFloat(n));
    const parseableFloatWithDelimeter = isString(n) && !Number.isNaN(Number.parseFloat(n.replace(',', '.')));
    const convertableNumber = !Number.isNaN(n - 0);
    return (parseableFloat || parseableFloatWithDelimeter) && convertableNumber
}