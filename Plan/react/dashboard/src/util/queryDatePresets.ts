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

export const getServerWallClockNow = (
    epochMs: number,
    timeZoneOffsetMinutes: number,
) => epochMs - (timeZoneOffsetMinutes || 0) * 60000;

export const calculatePresetRange = (
    preset: DateRangePreset,
    serverWallClockNowMs: number,
    firstDay: number = 1,
): DateTimeRange | undefined => {
    const now = moment.utc(serverWallClockNowMs).startOf('minute');
    const today = now.clone().startOf('day');
    const weekStart = startOfWeek(now, firstDay);
    const monthStart = now.clone().startOf('month');
    const yearStart = now.clone().startOf('year');

    switch (preset) {
        case 'last30days':
            return range(now.clone().subtract(30, 'days'), now);
        case 'last7days':
            return range(now.clone().subtract(7, 'days'), now);
        case 'last24h':
            return range(now.clone().subtract(24, 'hours'), now);
        case 'today':
            return range(today, today.clone().add(1, 'day'));
        case 'yesterday':
            return range(today.clone().subtract(1, 'day'), today);
        case 'currentWeek':
            return range(weekStart, weekStart.clone().add(1, 'week'));
        case 'previousWeek':
            return range(weekStart.clone().subtract(1, 'week'), weekStart);
        case 'currentMonth':
            return range(monthStart, monthStart.clone().add(1, 'month'));
        case 'previousMonth':
            return range(monthStart.clone().subtract(1, 'month'), monthStart);
        case 'currentYear':
            return range(yearStart, yearStart.clone().add(1, 'year'));
        case 'previousYear':
            return range(yearStart.clone().subtract(1, 'year'), yearStart);
        default:
            return undefined;
    }
};
