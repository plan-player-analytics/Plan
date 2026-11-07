import moment from 'moment';
import { GenericFilter } from '../dataHooks/model/GenericFilter';
import { MS_24H, MS_MONTH, MS_WEEK } from './format/useDateFormatter';
import {
    calculatePresetInterval,
    DATE_RANGE_PRESETS,
    DateRangePreset,
    fromServerWallClock,
    ServerTimeZone,
    toServerWallClock,
} from './queryDatePresets';

export type TimeframePreset =
    | 'weekComparison'
    | 'monthWeekDay'
    | 'monthByWeeks'
    | 'previousAndCurrentWeek'
    | 'previousAndCurrentMonth'
    | 'last6Hours'
    | 'last7Days'
    | 'currentMonthByWeek'
    | 'previousMonthByWeek'
    | 'last6Months'
    | 'custom';

export const ALL_TIMEFRAME_PRESETS: TimeframePreset[] = [
    'weekComparison',
    'monthWeekDay',
    'monthByWeeks',
    'previousAndCurrentWeek',
    'previousAndCurrentMonth',
    'last6Hours',
    'last7Days',
    'currentMonthByWeek',
    'previousMonthByWeek',
    'last6Months',
    'custom',
];

export const TWO_COLUMN_PRESETS: TimeframePreset[] = [
    'weekComparison',
    'previousAndCurrentWeek',
    'previousAndCurrentMonth',
    'custom',
];

export type PeriodUnit = 'hour' | 'day' | 'week' | 'month' | 'range';

export type TimeframePeriod = {
    start: number;
    end: number;
    unit: PeriodUnit;
    untilNow?: boolean;
};

export type TimeframeColumn = {
    key: string;
    filter: GenericFilter;
    period?: TimeframePeriod;
    label?: { key: string; number?: number; detailKey?: string };
};

export type CustomRange = {
    from?: string;
    to?: string;
    preset?: DateRangePreset;
};

export const CUSTOM_RANGE_PRESETS = DATE_RANGE_PRESETS.filter(
    (option) => option.name !== 'allData',
);

const HOUR_MS = 60 * 60 * 1000;

const RELATIVE_PRESETS: Partial<Record<TimeframePreset, TimeframeColumn[]>> = {
    weekComparison: [
        {
            key: 'previous',
            filter: { afterMillisAgo: MS_WEEK * 2, beforeMillisAgo: MS_WEEK },
        },
        {
            key: 'next',
            filter: { afterMillisAgo: MS_WEEK, beforeMillisAgo: 0 },
        },
    ],
    monthWeekDay: [
        { key: '30d', filter: { afterMillisAgo: MS_MONTH } },
        { key: '7d', filter: { afterMillisAgo: MS_WEEK } },
        { key: '1d', filter: { afterMillisAgo: MS_24H } },
    ],
    monthByWeeks: [
        { key: '30d', filter: { afterMillisAgo: MS_MONTH } },
        {
            key: '7d-1',
            filter: {
                afterMillisAgo: MS_WEEK * 4,
                beforeMillisAgo: MS_WEEK * 3,
            },
            label: { key: 'html.label.time.week', number: 1 },
        },
        {
            key: '7d-2',
            filter: {
                afterMillisAgo: MS_WEEK * 3,
                beforeMillisAgo: MS_WEEK * 2,
            },
            label: { key: 'html.label.time.week', number: 2 },
        },
        {
            key: '7d-3',
            filter: { afterMillisAgo: MS_WEEK * 2, beforeMillisAgo: MS_WEEK },
            label: { key: 'html.label.time.week', number: 3 },
        },
        {
            key: '7d-4',
            filter: { afterMillisAgo: MS_WEEK },
            label: {
                key: 'html.label.time.week',
                number: 4,
                detailKey: 'html.label.last7days',
            },
        },
        { key: '1d', filter: { afterMillisAgo: MS_24H } },
    ],
};

const DAY_PATTERN = /^(\d{2})\/(\d{2})\/(\d{4})$/;

const MIN_YEAR = 1970;

export const parseDay = (value?: string): moment.Moment | undefined => {
    if (!value) return undefined;
    const match = DAY_PATTERN.exec(value);
    if (!match) return undefined;
    const day = moment.utc([
        Number(match[3]),
        Number(match[2]) - 1,
        Number(match[1]),
    ]);
    return day.isValid() &&
        day.date() === Number(match[1]) &&
        day.year() >= MIN_YEAR
        ? day
        : undefined;
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
    return boundaries
        .slice(0, -1)
        .map((start, i) => ({
            key: `${unit}-${instants[i]}`,
            filter: { after: instants[i], before: instants[i + 1] },
            period: {
                start: start.valueOf(),
                end: boundaries[i + 1].valueOf(),
                unit,
            },
        }))
        .filter((_, i) => includeFuture || instants[i] <= nowEpochMs);
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
): TimeframeColumn[] =>
    Array.from({ length: count }, (_, i) => {
        const after = nowEpochMs - (count - i) * HOUR_MS;
        const before = after + HOUR_MS;
        return {
            key: `hour-${i}-${after}`,
            filter: { after, before },
            period: {
                start: toServerWallClock(after, zone).valueOf(),
                end: toServerWallClock(before, zone).valueOf(),
                unit: 'hour',
                untilNow: i === count - 1,
            },
        };
    });

export const resolveCustomRange = (
    range: CustomRange,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number,
): CustomRange => {
    if (!range.preset || range.preset === 'custom') return range;
    const interval = calculatePresetInterval(
        range.preset,
        nowEpochMs,
        zone,
        firstDay,
    );
    if (!interval) return range;
    return {
        preset: range.preset,
        from: formatDay(toServerWallClock(interval.after, zone)),
        to: formatDay(toServerWallClock(interval.before - 1, zone)),
    };
};

const presetColumn = (
    preset: DateRangePreset,
    index: number,
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number,
): TimeframeColumn | undefined => {
    const interval = calculatePresetInterval(
        preset,
        nowEpochMs,
        zone,
        firstDay,
    );
    if (!interval) return undefined;
    return {
        key: `range-${index}-${preset}-${interval.after}`,
        filter: interval,
        label: { key: `html.query.filter.preset.${preset}` },
    };
};

const customColumns = (
    ranges: CustomRange[],
    nowEpochMs: number,
    zone: ServerTimeZone,
    firstDay: number,
): TimeframeColumn[] | undefined => {
    if (!ranges.length) return undefined;
    const columns: TimeframeColumn[] = [];
    for (const range of ranges) {
        if (range.preset && range.preset !== 'custom') {
            const column = presetColumn(
                range.preset,
                columns.length,
                nowEpochMs,
                zone,
                firstDay,
            );
            if (!column) return undefined;
            columns.push(column);
            continue;
        }
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
    zone: ServerTimeZone | undefined,
    firstDay: number = 1,
    customRanges: CustomRange[] = [],
): TimeframeColumn[] | undefined => {
    const relative = RELATIVE_PRESETS[preset];
    if (relative) return relative;
    if (!zone) return undefined;
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
            return customColumns(customRanges, nowEpochMs, zone, firstDay);
        default:
            return undefined;
    }
};
