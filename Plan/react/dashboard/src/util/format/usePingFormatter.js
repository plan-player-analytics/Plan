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
            const split = isNumber(value) ? [value] : value.split('.');
            const decimals = split.length > 1 ? split[1] : value % 1;
            const parts = formatter.formatToParts({milliseconds: Math.floor(Number(split[0]))});

            const ints = parts.map((part, i) => part.type === 'integer' ? i : -1)
                .filter((i) => i >= 0);
            const lastIndex = ints[ints.length - 1];
            return parts.map((part, i) => {
                if (part.type === 'group') {
                    return '';
                }
                if (lastIndex === i && split.length > 1) {
                    return isNumber(decimals) ? Number(part.value) + decimals : part.value + '.' + decimals;
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