import moment from 'moment';
import { SEPARATOR } from './presetSeparator';

export type DateRangePreset =
    | 'allData'
    | 'custom'
    | 'last30days'
    | 'last7days'
    | 'last24h'
    | 'currentMonth'
    | 'previousMonth'
    | 'currentWeek'
    | 'previousWeek'
    | 'today'
    | 'yesterday'
    | 'currentYear'
    | 'previousYear';

export const DATE_RANGE_PRESETS: {
    name: DateRangePreset | typeof SEPARATOR;
    label: string;
}[] = [
    { name: 'allData', label: 'html.query.filter.preset.allData' },
    { name: 'custom', label: 'html.query.filter.preset.custom' },
    { name: SEPARATOR, label: '' },
    { name: 'last30days', label: 'html.query.filter.preset.last30days' },
    { name: 'last7days', label: 'html.query.filter.preset.last7days' },
    { name: 'last24h', label: 'html.query.filter.preset.last24h' },
    { name: SEPARATOR, label: '' },
    { name: 'yesterday', label: 'html.query.filter.preset.yesterday' },
    { name: 'today', label: 'html.query.filter.preset.today' },
    { name: 'previousWeek', label: 'html.query.filter.preset.previousWeek' },
    { name: 'currentWeek', label: 'html.query.filter.preset.currentWeek' },
    { name: 'previousMonth', label: 'html.query.filter.preset.previousMonth' },
    { name: 'currentMonth', label: 'html.query.filter.preset.currentMonth' },
    { name: 'previousYear', label: 'html.query.filter.preset.previousYear' },
    { name: 'currentYear', label: 'html.query.filter.preset.currentYear' },
];

export const BOUNDED_DATE_RANGE_PRESETS = DATE_RANGE_PRESETS.filter(
    (option) => option.name !== 'allData',
);

export type DateTimeRange = {
    after: [string, string];
    before: [string, string];
};

const toDateAndTime = (m: moment.Moment): [string, string] => [
    m.format('DD/MM/YYYY'),
    m.format('HH:mm'),
];

const startOfWeek = (m: moment.Moment, firstDay: number) => {
    const daysSinceWeekStart = (m.day() - firstDay + 7) % 7;
    return m.clone().startOf('day').subtract(daysSinceWeekStart, 'days');
};

export type ServerTimeZone = {
    timeZone?: string;
    timeZoneOffsetMs?: number;
};

const DAY_MS = 24 * 60 * 60 * 1000;

const formatters: { [timeZone: string]: Intl.DateTimeFormat } = {};

const getFormatter = (timeZone: string) => {
    if (!formatters[timeZone]) {
        formatters[timeZone] = new Intl.DateTimeFormat('en-US', {
            timeZone,
            hourCycle: 'h23',
            year: 'numeric',
            month: 'numeric',
            day: 'numeric',
            hour: 'numeric',
            minute: 'numeric',
        });
    }
    return formatters[timeZone];
};

export const toServerWallClock = (
    epochMs: number,
    zone: ServerTimeZone,
): moment.Moment => {
    if (zone.timeZone) {
        try {
            const parts: { [type: string]: number } = {};
            getFormatter(zone.timeZone)
                .formatToParts(epochMs)
                .forEach((part) => {
                    parts[part.type] = Number(part.value);
                });
            return moment.utc([
                parts.year,
                parts.month - 1,
                parts.day,
                parts.hour % 24,
                parts.minute,
            ]);
        } catch {
            delete formatters[zone.timeZone];
        }
    }
    return moment.utc(epochMs + (zone.timeZoneOffsetMs || 0)).startOf('minute');
};

const MINUTE_MS = 60 * 1000;

const offsetAt = (epochMs: number, zone: ServerTimeZone) =>
    toServerWallClock(epochMs, zone).valueOf() -
    (epochMs - (((epochMs % MINUTE_MS) + MINUTE_MS) % MINUTE_MS));

export const fromServerWallClock = (
    wallClock: moment.Moment,
    zone: ServerTimeZone,
): number => {
    const target = wallClock.valueOf();
    const offsetBefore = offsetAt(target - DAY_MS, zone);
    const offsetAfter = offsetAt(target + DAY_MS, zone);
    const candidates = [target - offsetBefore, target - offsetAfter].filter(
        (epochMs) => toServerWallClock(epochMs, zone).valueOf() === target,
    );
    return candidates.length ? Math.min(...candidates) : target - offsetBefore;
};

export type DateRange = {
    after: number;
    before: number;
};

export const calculatePresetRange = (
    preset: DateRangePreset,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number = 1,
): DateRange | undefined => {
    const last = (durationMs: number) => ({
        after: nowEpochMs - durationMs,
        before: nowEpochMs,
    });
    const between = (start: moment.Moment, nextStart: moment.Moment) => ({
        after: fromServerWallClock(start, zone),
        before: fromServerWallClock(nextStart, zone),
    });
    const now = toServerWallClock(nowEpochMs, zone);
    const today = now.clone().startOf('day');
    const weekStart = startOfWeek(now, firstDay);
    const monthStart = now.clone().startOf('month');
    const yearStart = now.clone().startOf('year');

    switch (preset) {
        case 'last30days':
            return last(30 * DAY_MS);
        case 'last7days':
            return last(7 * DAY_MS);
        case 'last24h':
            return last(DAY_MS);
        case 'today':
            return between(today, today.clone().add(1, 'day'));
        case 'yesterday':
            return between(today.clone().subtract(1, 'day'), today);
        case 'currentWeek':
            return between(weekStart, weekStart.clone().add(1, 'week'));
        case 'previousWeek':
            return between(weekStart.clone().subtract(1, 'week'), weekStart);
        case 'currentMonth':
            return between(monthStart, monthStart.clone().add(1, 'month'));
        case 'previousMonth':
            return between(monthStart.clone().subtract(1, 'month'), monthStart);
        case 'currentYear':
            return between(yearStart, yearStart.clone().add(1, 'year'));
        case 'previousYear':
            return between(yearStart.clone().subtract(1, 'year'), yearStart);
        default:
            return undefined;
    }
};

export const toDateTimeRange = (
    range: DateRange,
    zone: ServerTimeZone,
): DateTimeRange => ({
    after: toDateAndTime(toServerWallClock(range.after, zone)),
    before: toDateAndTime(toServerWallClock(range.before - 1, zone)),
});
