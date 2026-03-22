import {useTranslation} from "react-i18next";
import {useCallback, useMemo} from "react";
import {isNumber} from "../isNumber.js";
import {SimpleDateFormat} from "./SimpleDateFormat.js";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";
import {useMetadata} from "../../hooks/metadataHook.jsx";
import {usePreferences} from "../../hooks/preferencesHook.jsx";

const useDatePreferences = (includeSeconds) => {
    const {timeZoneOffsetHours} = useMetadata();
    const {preferencesLoaded, dateFormatFull, dateFormatNoSeconds, recentDaysInDateFormat} = usePreferences();

    return useMemo(() => {
        if (!preferencesLoaded) return {};

        const pattern = includeSeconds ? dateFormatFull : dateFormatNoSeconds;
        const recentDays = recentDaysInDateFormat;
        const recentDaysPattern = 'MMM d YYYY';

        const offset = timeZoneOffsetHours * 60 * 60 * 1000;

        return {pattern, recentDays, recentDaysPattern, offset};
    }, [preferencesLoaded, dateFormatFull, dateFormatNoSeconds, recentDaysInDateFormat]);
}

export const useDateFormatter = (includeSeconds, overrides = {}) => {
    const locale = useI18nFriendlyLanguage();
    const {t} = useTranslation();
    const preferences = useDatePreferences(includeSeconds);
    const {pattern, recentDays, recentDaysPattern, offset: configuredOffset} = {...preferences, ...overrides};

    const offset = overrides.noOffset ? 0 : configuredOffset;

    const formatDate = useCallback((date) => {
        if (!isNumber(date)) return date;
        if (date === 0) return '-'
        const dayMs = 24 * 60 * 60 * 1000;
        const timestamp = date - offset;
        const now = Date.now();
        const fromStartOfToday = (now - offset) % dayMs;

        let format = pattern;
        if (recentDays) {
            if (timestamp > now - offset - fromStartOfToday) {
                format = format.replace(recentDaysPattern, t('plugin.generic.today'));
            } else if (timestamp > now - offset - dayMs - fromStartOfToday) {
                format = format.replace(recentDaysPattern, t('plugin.generic.yesterday'));
            } else if (timestamp > now - offset - dayMs * 5) {
                format = format.replace(recentDaysPattern, "EEEE");
            }
        }

        return new SimpleDateFormat(format).format(timestamp, locale)
    }, [locale, t, pattern, recentDays, recentDaysPattern, offset]);

    return useMemo(() => {
        return {formatDate}
    }, [formatDate]);
}