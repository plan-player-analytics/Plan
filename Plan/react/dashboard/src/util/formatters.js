import {isString} from "highcharts";
import {useCallback} from "react";
import {useTranslation} from "react-i18next";

export const useOrUnavailable = () => {
    const {t} = useTranslation();
    const orUnavailable = useCallback((value) => {
        if (!t) return value;
        if (value === undefined || value === null || value === -1) return t('plugin.generic.unavailable');
        if (isString(value)) {
            return t(value);
        }
        return value;
    }, [t]);
    return {orUnavailable};
}