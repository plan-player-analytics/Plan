import React from 'react';
import {usePreferences} from "../../hooks/preferencesHook";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat";
import {isNumber} from "../../util/isNumber.js";

export const useTimePreferences = () => {
    const {preferencesLoaded, timeFormat} = usePreferences();

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
}

const FormattedTime = ({timeMs}) => {
    const options = useTimePreferences()

    if (!options) return <></>;
    if (!isNumber(timeMs)) return timeMs;
    return formatTimeAmount(options, timeMs);
};

export const formatTimeFunction = time => {
    return (
        <FormattedTime timeMs={time}/>
    );
}

export default FormattedTime