import {localeService} from "../../service/localeService.js";

const ZERO_PH = "%zero%";
const SECONDS_PH = "%seconds%";
const MINUTES_PH = "%minutes%";
const HOURS_PH = "%hours%";
const DAYS_PH = "%days%";
const MONTHS_PH = "%months%";
const YEARS_PH = "%years%";

/*
* Based on the Java equivalent TimeAmountFormatter.java
*/
export function formatTimeAmount(options, timeMs) {
    const apply = (ms) => {
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
        };
        if (options.HOURS.includes('%zero%')) {
            format.style = 'digital';
        }
        return new Intl.DurationFormat(localeService.getIntlFriendlyLocale(), format).format({
            years: Math.floor(years),
            months: Math.floor(months),
            days: Math.floor(days),
            hours: Math.floor(hours),
            minutes: Math.floor(minutes),
            seconds: years >= 1 ? undefined : Math.floor(seconds)
        });
    }

    return apply(timeMs);
}