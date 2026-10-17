import {useTranslation} from "react-i18next";
import {useCallback, useMemo} from "react";
import {isNumber} from "../isNumber";
import {SimpleDateFormat} from "./SimpleDateFormat.js";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";
import {useMetadata} from "../../hooks/metadataHook";
import {usePreferences} from "../../hooks/preferences/preferencesHook";
import {Preferences} from "../../hooks/model/Preferences";

export const MS_24H = 24 * 60 * 60 * 1000;
export const MS_WEEK = 7 * MS_24H;
export const MS_MONTH = 30 * MS_24H;
export const MS_YEAR = 365 * MS_24H;

const useDatePreferences = (includeSeconds?: boolean) => {
    const metadata = useMetadata();
    const {preferencesLoaded, dateFormatFull, dateFormatNoSeconds, recentDaysInDateFormat} = usePreferences();

    return useMemo(() => {
        if (!metadata.loaded || !preferencesLoaded) return {};
        const {timeZoneOffsetHours} = metadata;

        const pattern = includeSeconds ? dateFormatFull : dateFormatNoSeconds;
        const recentDays = recentDaysInDateFormat;
        const recentDaysPattern = 'MMM d YYYY';

        const offset = timeZoneOffsetHours * 60 * 60 * 1000;

        return {pattern, recentDays, recentDaysPattern, offset};
    }, [preferencesLoaded, dateFormatFull, dateFormatNoSeconds, recentDaysInDateFormat]);
}

type Overrides = {
    pattern?: string;
    recentDaysPattern?: string;
    noOffset?: boolean;
} & Partial<Preferences>

export const useDateFormatter = (includeSeconds?: boolean, overrides: Overrides = {}) => {
    const locale = useI18nFriendlyLanguage();
    const {t} = useTranslation();
    const preferences = useDatePreferences(includeSeconds);
    const {pattern, recentDays, recentDaysPattern, offset: configuredOffset} = {...preferences, ...overrides};

    const offset = overrides.noOffset ? 0 : configuredOffset;

    const formatDate = useCallback((date: any) => {
        if (!offset || !pattern) return date;
        if (!isNumber(date)) return date;
        if (date === 0) return '-'
        const dayMs = 24 * 60 * 60 * 1000;
        const timestamp = date - offset;
        const now = Date.now() - offset;
        const fromStartOfToday = now % dayMs;
        const today = now - fromStartOfToday;
        const yesterday = today - dayMs;
        const tomorrow = today + dayMs;
        const fiveDaysAgo = today - dayMs * 5;

        let format = pattern;
        if (recentDays) {
            if (timestamp >= today && timestamp < tomorrow) {
                format = format.replace(recentDaysPattern, t('plugin.generic.today'));
            } else if (timestamp >= yesterday && timestamp < today) {
                format = format.replace(recentDaysPattern, t('plugin.generic.yesterday'));
            } else if (timestamp >= fiveDaysAgo && timestamp < yesterday) {
                format = format.replace(recentDaysPattern, "EEEE");
            }
        }

        return new SimpleDateFormat(format).format(timestamp, locale)
    }, [locale, t, pattern, recentDays, recentDaysPattern, offset]);

    return useMemo(() => {
        return {formatDate}
    }, [formatDate]);
}