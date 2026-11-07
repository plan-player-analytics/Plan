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

type PresetBounds =
    { start: moment.Moment; nextStart: moment.Moment } | { durationMs: number };

const presetBounds = (
    preset: DateRangePreset,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number,
): PresetBounds | undefined => {
    const now = toServerWallClock(nowEpochMs, zone);
    const today = now.clone().startOf('day');
    const weekStart = startOfWeek(now, firstDay);
    const monthStart = now.clone().startOf('month');
    const yearStart = now.clone().startOf('year');

    switch (preset) {
        case 'last30days':
            return { durationMs: 30 * DAY_MS };
        case 'last7days':
            return { durationMs: 7 * DAY_MS };
        case 'last24h':
            return { durationMs: DAY_MS };
        case 'today':
            return { start: today, nextStart: today.clone().add(1, 'day') };
        case 'yesterday':
            return {
                start: today.clone().subtract(1, 'day'),
                nextStart: today,
            };
        case 'currentWeek':
            return {
                start: weekStart,
                nextStart: weekStart.clone().add(1, 'week'),
            };
        case 'previousWeek':
            return {
                start: weekStart.clone().subtract(1, 'week'),
                nextStart: weekStart,
            };
        case 'currentMonth':
            return {
                start: monthStart,
                nextStart: monthStart.clone().add(1, 'month'),
            };
        case 'previousMonth':
            return {
                start: monthStart.clone().subtract(1, 'month'),
                nextStart: monthStart,
            };
        case 'currentYear':
            return {
                start: yearStart,
                nextStart: yearStart.clone().add(1, 'year'),
            };
        case 'previousYear':
            return {
                start: yearStart.clone().subtract(1, 'year'),
                nextStart: yearStart,
            };
        default:
            return undefined;
    }
};

export const calculatePresetRange = (
    preset: DateRangePreset,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number = 1,
): DateTimeRange | undefined => {
    const bounds = presetBounds(preset, nowEpochMs, zone, firstDay);
    if (!bounds) return undefined;
    if ('durationMs' in bounds) {
        return range(
            toServerWallClock(nowEpochMs - bounds.durationMs, zone),
            toServerWallClock(nowEpochMs, zone),
        );
    }
    return range(
        bounds.start,
        toServerWallClock(
            fromServerWallClock(bounds.nextStart, zone) - MINUTE_MS,
            zone,
        ),
    );
};

export const calculatePresetInterval = (
    preset: DateRangePreset,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number = 1,
): { after: number; before: number } | undefined => {
    const bounds = presetBounds(preset, nowEpochMs, zone, firstDay);
    if (!bounds) return undefined;
    if ('durationMs' in bounds) {
        return { after: nowEpochMs - bounds.durationMs, before: nowEpochMs };
    }
    return {
        after: fromServerWallClock(bounds.start, zone),
        before: fromServerWallClock(bounds.nextStart, zone),
    };
};
