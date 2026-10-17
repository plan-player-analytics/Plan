import {usePreferences} from "./preferencesHook";
import {useMemo} from "react";

export const useTimePreferences = () => {
    const {preferencesLoaded, timeFormat} = usePreferences();

    return useMemo(() => {
        if (!preferencesLoaded || !timeFormat) return undefined;
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
