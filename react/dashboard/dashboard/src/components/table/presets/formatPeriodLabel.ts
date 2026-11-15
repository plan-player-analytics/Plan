import {
    PeriodUnit,
    TimeframePeriod,
} from '../../../util/datapointTablePresets';

const DAY_MS = 24 * 60 * 60 * 1000;

const FORMAT_OPTIONS: Record<PeriodUnit, Intl.DateTimeFormatOptions> = {
    hour: { hour: '2-digit', minute: '2-digit', hourCycle: 'h23' },
    day: { weekday: 'short', day: 'numeric', month: 'short' },
    week: { day: 'numeric', month: 'short' },
    month: { month: 'long', year: 'numeric' },
    year: { year: 'numeric' },
    range: { day: 'numeric', month: 'short', year: 'numeric' },
};

export const formatPeriodLabel = (
    period: TimeframePeriod,
    locale: string,
    nowText: string,
) => {
    const formatter = new Intl.DateTimeFormat(locale, {
        ...FORMAT_OPTIONS[period.unit],
        timeZone: 'UTC',
    });
    if (period.unit === 'hour') {
        return (
            formatter.format(period.start) +
            '–' +
            (period.untilNow ? nowText : formatter.format(period.end))
        );
    }
    const lastDay = period.end - DAY_MS;
    if (
        (period.unit === 'week' || period.unit === 'range') &&
        lastDay > period.start
    ) {
        return formatter.formatRange(period.start, lastDay);
    }
    return formatter.format(period.start);
};
