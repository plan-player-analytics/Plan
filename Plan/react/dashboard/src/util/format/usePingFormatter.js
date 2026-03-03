import {isNumber} from "../isNumber.js";
import {useCallback, useMemo} from "react";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";

export const usePingFormatter = () => {
    const locale = useI18nFriendlyLanguage();
    const formatter = useMemo(() => {
        return new Intl.DurationFormat(locale, {style: 'narrow'})
    }, [locale]);

    const formatPing = useCallback((value) => {
        if (isNumber(value) && !String(value).includes("ms")) {
            const parts = formatter.formatToParts({milliseconds: value});

            return parts.map((part) => {
                if (part.type === 'group') {
                    return '';
                }
                return part.value;
            }).join('');
        }
        return value;
    }, [formatter]);
    return useMemo(() => {
        return {formatPing}
    }, [formatPing]);
}