import React from 'react';
import {usePreferences} from "../../hooks/preferencesHook";
import {SimpleDateFormat} from "../../util/format/SimpleDateFormat";
import {useMetadata} from "../../hooks/metadataHook";
import {useTranslation} from "react-i18next";
import {isNumber} from "../../util/isNumber.js";

export const useDatePreferences = () => {
    const {t} = useTranslation();
    const {timeZoneOffsetHours} = useMetadata();
    const {preferencesLoaded, dateFormatNoSeconds, recentDaysInDateFormat} = usePreferences();

    if (!preferencesLoaded) return {};

    const pattern = dateFormatNoSeconds;
    const recentDays = recentDaysInDateFormat;
    const recentDaysPattern = 'MMM d YYYY';

    const offset = timeZoneOffsetHours * 60 * 60 * 1000;

    return {t, pattern, recentDays, recentDaysPattern, offset};
}

export function formatDate(date, offset, pattern, recentDays, recentDaysPattern, t) {
    if (!isNumber(date)) return date;
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

    return date !== 0 ? new SimpleDateFormat(format).format(timestamp) : '-'
}

export function formatDateWithPreferences(datePreferences, date) {
    const {t, pattern, recentDays, recentDaysPattern, offset} = datePreferences;
    return formatDate(date, offset, pattern, recentDays, recentDaysPattern, t);
}

const FormattedDate = ({date, react}) => {
    const {t} = useTranslation();

    const {pattern, recentDays, recentDaysPattern, offset} = useDatePreferences();

    if (!pattern || date === undefined || date === null) return <></>;
    if (!isNumber(date)) return date;

    if (react) {
        return <span title={formatDate(date, offset, pattern, false, null, t)}>
            {formatDate(date, offset, pattern, recentDays, recentDaysPattern, t)}
        </span>
    }

    return formatDate(date, offset, pattern, recentDays, recentDaysPattern, t);
};

export default FormattedDate