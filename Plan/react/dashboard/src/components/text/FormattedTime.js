import React from 'react';
import {usePreferences} from "../../hooks/preferencesHook";
import {formatTimeAmount} from "../../util/format/TimeAmountFormat";

const FormattedTime = ({timeMs}) => {
    const {preferencesLoaded, timeFormat} = usePreferences();

    if (!preferencesLoaded) return <></>

    const options = {
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
    const formatted = formatTimeAmount(options, timeMs);

    return (
        <>{formatted}</>
    )
};

export default FormattedTime