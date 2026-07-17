import {isNumber} from "../isNumber.js";
import {useCallback, useMemo} from "react";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";
import {useDecimalFormatter} from "./useDecimalFormatter.js";

export const usePingFormatter = () => {
    const locale = useI18nFriendlyLanguage();
    const {formatDecimals} = useDecimalFormatter();
    const formatter = useMemo(() => {
        return new Intl.DurationFormat(locale, {style: 'narrow'})
    }, [locale]);

    const formatPing = useCallback((value) => {
        if (isNumber(value) && !String(value).includes("ms")) {
            return formatter.format({milliseconds: 1}).replaceAll("1", formatDecimals(value))
        }
        return value;
    }, [formatter]);
    return useMemo(() => {
        return {formatPing}
    }, [formatPing]);
}