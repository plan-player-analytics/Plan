import {useCallback, useMemo} from "react";
import {useTimePreferences} from "../../components/text/FormattedTime.jsx";
import {useI18nFriendlyLanguage} from "../../service/localeService.js";


const ZERO_PH = "%zero%";

export const useTimeAmountFormatter = () => {
    const locale = useI18nFriendlyLanguage();
    const timePreferences = useTimePreferences();

    const formatTime = useCallback((ms) => {
        if (ms === null || ms < 0) {
            return "-";
        }
        let x = ms / 1000;
        let seconds = x % 60;
        x /= 60;
        let minutes = x % 60;
        x /= 60;
        let hours = x % 24;
        x /= 24;
        let days = x % 365;
        let months = (days - days % 30) / 30;
        days -= months * 30;
        x /= 365;
        let years = x;

        const format = {
            style: 'narrow',
            years: 'long',
            yearsDisplay: 'auto',
            months: 'long',
            monthsDisplay: 'auto',
            secondsDisplay: ms < 1000 ? 'always' : 'auto',
        };
        // Temporary support for old format, we can later move the whole thing to the preferences menu,
        // so that there's a dropdown of options that are same as above, but modified for each option.
        if (timePreferences.HOURS.includes(ZERO_PH) || timePreferences.MINUTES.includes(ZERO_PH)) {
            format.style = 'digital';
        }
        return new Intl.DurationFormat(locale, format).format({
            years: Math.floor(years),
            months: Math.floor(months),
            days: Math.floor(days),
            hours: Math.floor(hours),
            minutes: Math.floor(minutes),
            seconds: years >= 1 || months > 1 || days > 1 ? undefined : Math.floor(seconds)
        });
    }, [locale, timePreferences])

    return useMemo(() => {
        return {formatTime}
    }, [formatTime]);
}