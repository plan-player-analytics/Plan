import moment from 'moment';

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

export const DATE_RANGE_PRESETS: { name: DateRangePreset; label: string }[] = [
    { name: 'allData', label: 'html.query.filter.preset.allData' },
    { name: 'custom', label: 'html.query.filter.preset.custom' },
    { name: 'last30days', label: 'html.query.filter.preset.last30days' },
    { name: 'last7days', label: 'html.query.filter.preset.last7days' },
    { name: 'last24h', label: 'html.query.filter.preset.last24h' },
    { name: 'currentMonth', label: 'html.query.filter.preset.currentMonth' },
    { name: 'previousMonth', label: 'html.query.filter.preset.previousMonth' },
    { name: 'currentWeek', label: 'html.query.filter.preset.currentWeek' },
    { name: 'previousWeek', label: 'html.query.filter.preset.previousWeek' },
    { name: 'today', label: 'html.query.filter.preset.today' },
    { name: 'yesterday', label: 'html.query.filter.preset.yesterday' },
    { name: 'currentYear', label: 'html.query.filter.preset.currentYear' },
    { name: 'previousYear', label: 'html.query.filter.preset.previousYear' },
];

export type DateTimeRange = {
    after: [string, string];
    before: [string, string];
};

const toDateAndTime = (m: moment.Moment): [string, string] => [
    m.format('DD/MM/YYYY'),
    m.format('HH:mm'),
];

const range = (after: moment.Moment, before: moment.Moment): DateTimeRange => ({
    after: toDateAndTime(after),
    before: toDateAndTime(before),
});

const period = (start: moment.Moment, nextStart: moment.Moment) =>
    range(start, nextStart.clone().subtract(1, 'minute'));

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

const elapsed = (nowMs: number, durationMs: number, zone: ServerTimeZone) =>
    range(
        toServerWallClock(nowMs - durationMs, zone),
        toServerWallClock(nowMs, zone),
    );

export const calculatePresetRange = (
    preset: DateRangePreset,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number = 1,
): DateTimeRange | undefined => {
    const now = toServerWallClock(nowEpochMs, zone);
    const today = now.clone().startOf('day');
    const weekStart = startOfWeek(now, firstDay);
    const monthStart = now.clone().startOf('month');
    const yearStart = now.clone().startOf('year');

    switch (preset) {
        case 'last30days':
            return elapsed(nowEpochMs, 30 * DAY_MS, zone);
        case 'last7days':
            return elapsed(nowEpochMs, 7 * DAY_MS, zone);
        case 'last24h':
            return elapsed(nowEpochMs, DAY_MS, zone);
        case 'today':
            return period(today, today.clone().add(1, 'day'));
        case 'yesterday':
            return period(today.clone().subtract(1, 'day'), today);
        case 'currentWeek':
            return period(weekStart, weekStart.clone().add(1, 'week'));
        case 'previousWeek':
            return period(weekStart.clone().subtract(1, 'week'), weekStart);
        case 'currentMonth':
            return period(monthStart, monthStart.clone().add(1, 'month'));
        case 'previousMonth':
            return period(monthStart.clone().subtract(1, 'month'), monthStart);
        case 'currentYear':
            return period(yearStart, yearStart.clone().add(1, 'year'));
        case 'previousYear':
            return period(yearStart.clone().subtract(1, 'year'), yearStart);
        default:
            return undefined;
    }
};
