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

        let builder = "";
        builder += appendYears(Math.floor(years));
        builder += appendMonths(Math.floor(months));
        builder += appendDays(Math.floor(days));

        const hourFormat = options.HOURS;
        const minuteFormat = options.MINUTES;
        const secondFormat = options.SECONDS;

        builder += appendHours(Math.floor(hours), hourFormat);
        builder += appendMinutes(Math.floor(minutes), Math.floor(hours), hourFormat, minuteFormat);
        builder += appendSeconds(Math.floor(seconds), Math.floor(minutes), Math.floor(hours), hourFormat, minuteFormat, secondFormat);

        const formattedTime = builder.replaceAll(ZERO_PH, '');
        if (formattedTime.length === 0) {
            return options.ZERO;
        }
        return formattedTime;
    }

    const appendHours = (hours, fHours) => {
        if (hours !== 0 || fHours.includes(ZERO_PH)) {
            let h = fHours.replace(HOURS_PH, String(hours));
            if (h.includes(ZERO_PH) && String(hours).length === 1) {
                h = '0' + h;
            }
            return h;
        }
        return '';
    }

    const appendMinutes = (minutes, hours, fHours, fMinutes) => {
        if (minutes !== 0 || fMinutes.includes(ZERO_PH)) {
            let m = fMinutes.replace(MINUTES_PH, String(minutes));
            if (hours === 0 && m.includes(HOURS_PH)) {
                m = fHours.replace(ZERO_PH, "0").replace(HOURS_PH, "0") + m;
                m = m.replace(HOURS_PH, "");
            }
            m = m.replace(HOURS_PH, "");
            if (m.includes(ZERO_PH) && String(minutes).length === 1) {
                m = '0' + m;
            }
            return m;
        }
        return '';
    }

    const appendSeconds = (seconds, minutes, hours, fHours, fMinutes, fSeconds) => {
        if (seconds !== 0 || fSeconds.includes(ZERO_PH)) {
            let s = fSeconds.replace(SECONDS_PH, String(seconds));
            if (minutes === 0 && s.includes(MINUTES_PH)) {
                if (hours === 0 && fMinutes.includes(HOURS_PH)) {
                    s = fHours.replace(ZERO_PH, "0").replace(HOURS_PH, "0") + s;
                }
                s = fMinutes.replace(HOURS_PH, "").replace(ZERO_PH, "0").replace(MINUTES_PH, "0") + s;
            }
            s = s.replace(MINUTES_PH, "");
            if (s.includes(ZERO_PH) && String(seconds).length === 1) {
                s = '0' + s;
            }
            return s;
        }
        return '';
    }

    const appendDays = (days) => {
        const singular = options.DAY;
        const plural = options.DAYS;
        return appendValue(days, singular, plural, DAYS_PH);
    }

    const appendMonths = (months) => {
        const singular = options.MONTH;
        const plural = options.MONTHS;

        return appendValue(months, singular, plural, MONTHS_PH);
    }

    const appendYears = (years) => {
        const singular = options.YEAR;
        const plural = options.YEARS;

        return appendValue(years, singular, plural, YEARS_PH);
    }

    const appendValue = (amount, singular, plural, replace) => {
        if (amount !== 0) {
            if (amount === 1) {
                return singular;
            } else {
                return plural.replace(replace, String(amount));
            }
        }
        return '';
    }

    return apply(timeMs);
}