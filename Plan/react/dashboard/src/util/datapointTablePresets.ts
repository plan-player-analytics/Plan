import moment from 'moment';
import {
    fromServerWallClock,
    ServerTimeZone,
    toServerWallClock,
} from './queryDatePresets';

export type TimeframePreset =
    | 'default'
    | 'previousAndCurrentWeek'
    | 'previousAndCurrentMonth'
    | 'last6Hours'
    | 'last7Days'
    | 'currentMonthByWeek'
    | 'previousMonthByWeek'
    | 'last6Months'
    | 'custom';

export const TIMEFRAME_PRESETS: { name: TimeframePreset; label: string }[] = [
    { name: 'default', label: 'html.label.timeframe.default' },
    {
        name: 'previousAndCurrentWeek',
        label: 'html.label.timeframe.previousAndCurrentWeek',
    },
    {
        name: 'previousAndCurrentMonth',
        label: 'html.label.timeframe.previousAndCurrentMonth',
    },
    { name: 'last6Hours', label: 'html.label.timeframe.last6Hours' },
    { name: 'last7Days', label: 'html.label.timeframe.last7Days' },
    {
        name: 'currentMonthByWeek',
        label: 'html.label.timeframe.currentMonthByWeek',
    },
    {
        name: 'previousMonthByWeek',
        label: 'html.label.timeframe.previousMonthByWeek',
    },
    { name: 'last6Months', label: 'html.label.timeframe.last6Months' },
    { name: 'custom', label: 'html.label.timeframe.custom' },
];

export const ALL_TIMEFRAME_PRESETS: TimeframePreset[] = TIMEFRAME_PRESETS.map(
    (preset) => preset.name,
);

export const TWO_COLUMN_PRESETS: TimeframePreset[] = [
    'default',
    'previousAndCurrentWeek',
    'previousAndCurrentMonth',
    'custom',
];

export type PeriodUnit = 'hour' | 'day' | 'week' | 'month' | 'range';

export type TimeframeColumn = {
    key: string;
    filter: { after: number; before?: number };
    period: { start: number; end: number; unit: PeriodUnit };
};

export type CustomRange = { from?: string; to?: string };

const HOUR_MS = 60 * 60 * 1000;

const DAY_PATTERN = /^(\d{2})\/(\d{2})\/(\d{4,5})$/;

export const parseDay = (value?: string): moment.Moment | undefined => {
    if (!value) return undefined;
    const match = DAY_PATTERN.exec(value);
    if (!match) return undefined;
    const day = moment.utc([
        Number(match[3]),
        Number(match[2]) - 1,
        Number(match[1]),
    ]);
    return day.isValid() && day.date() === Number(match[1]) ? day : undefined;
};

export const formatDay = (day: moment.Moment) => day.format('DD/MM/YYYY');

const startOfWeek = (m: moment.Moment, firstDay: number) => {
    const daysSinceWeekStart = (m.day() - firstDay + 7) % 7;
    return m.clone().startOf('day').subtract(daysSinceWeekStart, 'days');
};

const toColumns = (
    boundaries: moment.Moment[],
    unit: PeriodUnit,
    nowEpochMs: number,
    zone: ServerTimeZone,
    includeFuture: boolean = false,
): TimeframeColumn[] => {
    const instants = boundaries.map((b) => fromServerWallClock(b, zone));
    const columns = boundaries.slice(0, -1).map((start, i) => {
        const after = instants[i];
        const nextStart = instants[i + 1];
        return {
            key: `${unit}-${after}`,
            filter:
                nextStart > nowEpochMs
                    ? { after }
                    : { after, before: nextStart },
            period: {
                start: start.valueOf(),
                end: boundaries[i + 1].valueOf(),
                unit,
            },
        };
    });
    return includeFuture
        ? columns
        : columns.filter((column) => column.filter.after <= nowEpochMs);
};

const consecutive = (
    first: moment.Moment,
    count: number,
    unit: 'day' | 'month',
) => Array.from({ length: count + 1 }, (_, i) => first.clone().add(i, unit));

const weeksOfMonth = (monthStart: moment.Moment, firstDay: number) => {
    const nextMonth = monthStart.clone().add(1, 'month');
    const boundaries = [monthStart.clone()];
    let next = startOfWeek(monthStart, firstDay).add(1, 'week');
    while (next.isBefore(nextMonth)) {
        boundaries.push(next.clone());
        next = next.add(1, 'week');
    }
    boundaries.push(nextMonth);
    return boundaries;
};

const lastHours = (
    count: number,
    nowEpochMs: number,
    zone: ServerTimeZone,
): TimeframeColumn[] => {
    const currentHour = fromServerWallClock(
        toServerWallClock(nowEpochMs, zone).startOf('hour'),
        zone,
    );
    return Array.from({ length: count }, (_, i) => {
        const after = currentHour - (count - 1 - i) * HOUR_MS;
        const nextStart = after + HOUR_MS;
        const start = toServerWallClock(after, zone).valueOf();
        return {
            key: `hour-${after}`,
            filter:
                nextStart > nowEpochMs
                    ? { after }
                    : { after, before: nextStart },
            period: { start, end: start + HOUR_MS, unit: 'hour' },
        };
    });
};

const customColumns = (
    ranges: CustomRange[],
    nowEpochMs: number,
    zone: ServerTimeZone,
): TimeframeColumn[] | undefined => {
    if (!ranges.length) return undefined;
    const columns: TimeframeColumn[] = [];
    for (const range of ranges) {
        const from = parseDay(range.from);
        const to = parseDay(range.to);
        if (!from || !to || to.isBefore(from)) return undefined;
        const [column] = toColumns(
            [from, to.clone().add(1, 'day')],
            'range',
            nowEpochMs,
            zone,
            true,
        );
        columns.push({
            ...column,
            key: `range-${columns.length}-${column.key}`,
        });
    }
    return columns;
};

export const defaultCustomRanges = (
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number = 1,
): CustomRange[] => {
    const weekStart = startOfWeek(
        toServerWallClock(nowEpochMs, zone),
        firstDay,
    );
    const previousWeekStart = weekStart.clone().subtract(1, 'week');
    return [
        {
            from: formatDay(previousWeekStart),
            to: formatDay(weekStart.clone().subtract(1, 'day')),
        },
        {
            from: formatDay(weekStart),
            to: formatDay(weekStart.clone().add(6, 'days')),
        },
    ];
};

export const calculateTimeframeColumns = (
    preset: TimeframePreset,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number = 1,
    customRanges: CustomRange[] = [],
): TimeframeColumn[] | undefined => {
    const now = toServerWallClock(nowEpochMs, zone);
    const today = now.clone().startOf('day');
    const weekStart = startOfWeek(now, firstDay);
    const monthStart = now.clone().startOf('month');

    switch (preset) {
        case 'previousAndCurrentWeek':
            return toColumns(
                [
                    weekStart.clone().subtract(1, 'week'),
                    weekStart,
                    weekStart.clone().add(1, 'week'),
                ],
                'week',
                nowEpochMs,
                zone,
            );
        case 'previousAndCurrentMonth':
            return toColumns(
                consecutive(
                    monthStart.clone().subtract(1, 'month'),
                    2,
                    'month',
                ),
                'month',
                nowEpochMs,
                zone,
            );
        case 'last6Hours':
            return lastHours(6, nowEpochMs, zone);
        case 'last7Days':
            return toColumns(
                consecutive(today.clone().subtract(6, 'days'), 7, 'day'),
                'day',
                nowEpochMs,
                zone,
            );
        case 'currentMonthByWeek':
            return toColumns(
                weeksOfMonth(monthStart, firstDay),
                'week',
                nowEpochMs,
                zone,
            );
        case 'previousMonthByWeek':
            return toColumns(
                weeksOfMonth(monthStart.clone().subtract(1, 'month'), firstDay),
                'week',
                nowEpochMs,
                zone,
            );
        case 'last6Months':
            return toColumns(
                consecutive(
                    monthStart.clone().subtract(5, 'months'),
                    6,
                    'month',
                ),
                'month',
                nowEpochMs,
                zone,
            );
        case 'custom':
            return customColumns(customRanges, nowEpochMs, zone);
        default:
            return undefined;
    }
};
