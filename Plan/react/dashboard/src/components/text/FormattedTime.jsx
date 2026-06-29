import React, {useMemo} from 'react';
import {usePreferences} from "../../hooks/preferencesHook";
import {isNumber} from "../../util/isNumber.js";
import {useTimeAmountFormatter} from "../../util/format/useTimeAmountFormatter.js";

export const useTimePreferences = () => {
    const {preferencesLoaded, timeFormat} = usePreferences();

    return useMemo(() => {
        if (!preferencesLoaded) return undefined;
        return {
            YEAR: timeFormat.year,
            YEARS: timeFormat.years,
            MONTH: timeFormat.month,
            MONTHS: timeFormat.months,
            DAY: timeFormat.day,
            DAYS: timeFormat.days,
            HOURS: timeFormat.hours,
            MINUTES: timeFormat.minutes,
            SECONDS: timeFormat.seconds,
            ZERO: timeFormat.zero
        }
    }, [preferencesLoaded, timeFormat])
}

const FormattedTime = ({timeMs}) => {
    const {formatTime} = useTimeAmountFormatter();
    if (!isNumber(timeMs)) return timeMs;
    return formatTime(timeMs);
};

export const formatTimeFunction = time => {
    return (
        <FormattedTime timeMs={time}/>
    );
}

export default FormattedTime