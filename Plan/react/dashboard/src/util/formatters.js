import {isString} from "highcharts";

export const formatDecimals = (value, formatPattern) => {
    if (!formatPattern || isNaN(value)) return value;
    const split = formatPattern.includes('.') ? formatPattern.split('.') : formatPattern.split(',');
    if (split.length <= 1) return value.toFixed(0);
    return value.toFixed(split[1].length);
}

export const orUnavailable = (value, t) => {
    if (!t) return value;
    if (!value) return t('plugin.generic.unavailable');
    if (isString(value)) {
        return t(value);
    }
    return value;
}