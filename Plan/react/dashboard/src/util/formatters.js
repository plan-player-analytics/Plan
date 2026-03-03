import {isString} from "highcharts";

export const orUnavailable = (value, t) => {
    if (!t) return value;
    if (!value) return t('plugin.generic.unavailable');
    if (isString(value)) {
        return t(value);
    }
    return value;
}